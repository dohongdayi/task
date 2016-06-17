package me.dohongdayi.task.supermarket

import java.util.concurrent.atomic.AtomicInteger
import java.util.{Timer, TimerTask}
import me.dohongdayi.task.supermarket.SupermarketState._
import me.dohongdayi.task.supermarket.util.Logging
import me.dohongdayi.task.supermarket.util.RandomUtils._
import java.lang.System.currentTimeMillis

/**
  * Cashier in supermarket, who receive 1 customer from waiting line in every 5-10 seconds
  * @param supermarket supermarket which the cashier belong to
  * @param id cashier id
  */
class Cashier(val supermarket: Supermarket,
              val id: Int
  ) extends Logging {

  private[supermarket] val timer: Timer = new Timer("Timer-Cashier" + id)

  private[supermarket] var customerNum: AtomicInteger = new AtomicInteger(0)

  private[supermarket] def startWork(): Unit = {
    scheduleNextCash()
  }

  private def scheduleNextCash(): Unit = {
    val task = new TimerTask() {
      def run(): Unit = {
        cash()
      }
    }
    timer.schedule(task, randomInt(5000, 10000))
  }

  private def cash(): Unit = {
    if (CLOSED == supermarket.getState) {
      logInfo("supermarket is closed, Cashier" + id + " stop")
      return
    }
    logInfo("Cashier" + id + " is cashing")
    val customer = supermarket.pollWaitingLine
    logDebug("waitingLine: " + supermarket.describeWaitingLine)
    logDebug("stock: " + supermarket.getStock)
    if (null != customer) {
      supermarket.sellOut(customer.good)
      customer.cashed = true
      customer.waitEndTime = currentTimeMillis()
      customerNum.incrementAndGet()
    }
    scheduleNextCash()
  }

}

object Cashier {

  def apply(supermarket: Supermarket, id: Int) = new Cashier(supermarket, id)
}




