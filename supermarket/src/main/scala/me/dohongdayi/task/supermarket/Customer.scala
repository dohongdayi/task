package me.dohongdayi.task.supermarket

import java.util.{TimerTask, Timer}
import me.dohongdayi.task.supermarket.SupermarketState._
import me.dohongdayi.task.supermarket.Good.Good
import java.lang.System.currentTimeMillis
import me.dohongdayi.task.supermarket.util.Logging
import me.dohongdayi.task.supermarket.util.RandomUtils._
import scala.util.Random.shuffle

/**
  * Customer, who want to purchase good from supermarket
 */
case class Customer(
      val good: Good,
      val waitBeginTime: Long,
      var waitEndTime: Long = -1,
      var cashed: Boolean = false
      )

/**
  * In every 1-3 seconds, 1 new customer go into supermarket and pick one good then wait for purchase
  */
object Customer extends Logging {

  private[supermarket] val timer: Timer = new Timer("Timer-Customers")

  private[supermarket] def crowdInto(supermarket: Supermarket): Unit = {
    scheduleNewCustomer(supermarket)
  }

  private def scheduleNewCustomer(supermarket: Supermarket): Unit = {
    val task = new TimerTask() {
      def run(): Unit = {
        newCustomer(supermarket)
      }
    }
    timer.schedule(task, randomInt(1000, 3000))
  }

  private def newCustomer(supermarket: Supermarket): Unit = {
    if (CLOSED == supermarket.getState) {
      logInfo("supermarket is closed, Customers stop")
      return
    }
    logInfo("new customer")
    shuffle(Good.values.toSeq).exists(
      good => supermarket.offerWaitingLine(Customer(good, currentTimeMillis()))
    )
    logDebug("waitingLine: " + supermarket.describeWaitingLine)
    logDebug("stock: " + supermarket.getStock)
    scheduleNewCustomer(supermarket)
  }

}