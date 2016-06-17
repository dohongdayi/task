package me.dohongdayi.task.supermarket

import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue}

import me.dohongdayi.task.supermarket.Good._
import me.dohongdayi.task.supermarket.SupermarketState._
import me.dohongdayi.task.supermarket.util.Logging
import java.lang.System.currentTimeMillis
import scala.collection._
import scala.collection.immutable.HashMap

/**
  * Supermarket which open and close in one go
  * call waitForClose() to wait for all goods be sold out and supermarket close
  * @param stockNum initial stock count for each good in supermarket
  */
class Supermarket(val stockNum: Int = 15) extends Logging {

  private var state: SupermarketState = INITIALIZED

  private var openTime: Long = -1
  private var allSoldOutTime: Long = -1

  private val stock: mutable.Map[Good, Int] =
    mutable.HashMap(APPLE -> stockNum, MACBOOK -> stockNum, COOKIE -> stockNum)     // mutable.HashMap(Good.values.map((_, stockNum)).toSeq: _*)

  private val cashiers: Seq[Cashier] =
    Seq(Cashier(this, 1), Cashier(this, 2), Cashier(this, 3))     // (1 to 3).map(Cashier(this, _)).toSeq

  private val waitingLine: BlockingQueue[Customer] = new LinkedBlockingQueue[Customer](100)

  private val customers: mutable.ArrayBuffer[Customer] = new mutable.ArrayBuffer[Customer]

  private val soldTimeMap: mutable.Map[Good, mutable.ArrayBuffer[Long]] =
    mutable.HashMap(                                              // mutable.HashMap(Good.values.map((_, new mutable.ArrayBuffer[Long])).toSeq: _*)
      APPLE -> new mutable.ArrayBuffer[Long],
      MACBOOK -> new mutable.ArrayBuffer[Long],
      COOKIE -> new mutable.ArrayBuffer[Long]
    )

  private val closedLock = new ReentrantLock()
  private val closed = closedLock.newCondition()

  private[supermarket] def sellOut(good: Good): Unit = synchronized {
    soldTimeMap(good) += currentTimeMillis()
    if (soldTimeMap.values.flatten.size == (stockNum * Good.values.size)) {
      allSoldOutTime = currentTimeMillis()
      close()
    }
  }

  private[supermarket] def offerWaitingLine(customer: Customer): Boolean = synchronized {
    stock(customer.good) match {
      case 0 =>
        return false
      case num =>
        stock.put(customer.good, num - 1)
        customers += customer
        waitingLine.offer(customer)
        return true
    }
  }

  private[supermarket] def pollWaitingLine: Customer = synchronized {
    waitingLine.poll()
  }

  private[supermarket] def describeWaitingLine: String = synchronized {
    waitingLine.toString
  }

  def getState: SupermarketState = synchronized {
    state
  }

  def getStock: Map[Good, Int] = synchronized {
    HashMap(stock.toSeq: _*)
  }

  def hasStock(good: Good): Boolean = synchronized {
    stock(good) > 0
  }

  def getCashiers: Seq[Cashier] = synchronized {
    cashiers
  }

  def getCustomers: Seq[Customer] = synchronized {
    Seq(customers: _*)
  }

  /**
    * Open supermarket and welcome customers
    */
  def open(): Unit = synchronized {
    logInfo("open supermarket")
    state match {
      case INITIALIZED =>
        state = OPENED
        openTime = currentTimeMillis()
        cashiers.foreach(_.startWork())
        Customer.crowdInto(this)
      case OPENED =>
        logWarning("Supermarket has already been opened")
      case CLOSED =>
        throw new IllegalStateException("Supermarket has already been closed")
    }
  }

  /**
    * Close supermarket whenever goods have been sold out
    */
  private[supermarket] def close(): Unit = synchronized {
    logInfo("close supermarket")
    state match {
      case INITIALIZED =>
        throw new IllegalStateException("Supermarket hasn't been opened")
      case OPENED =>
        waitingLine.clear()
        logDebug("waitingLine: " + waitingLine)
        logDebug("stock: " + stock)
        closedLock.lock()
        try {
          state = CLOSED
          closed.signalAll()
        } finally {
          closedLock.unlock()
        }
      case CLOSED =>
        logWarning("Supermarket has already been closed")
    }
  }

  /**
    * Wait for all goods be sold out and supermarket close
    */
  def waitForClose(): Unit = {
    closedLock.lock()
    try {
      while (CLOSED != state) {
        closed.await()
      }
    } finally {
      closedLock.unlock()
    }
  }

  /**
    * Report supermarket statistics after close
    * @return supermarket report
    */
  def report(): SupermarketReport = synchronized {
    if (CLOSED != state) {
      throw new IllegalStateException("Supermarket hasn't been closed")
    }

    val averageCustomerWaitMillis =
      customers.filter(_.waitEndTime > 0).map(one => one.waitEndTime - one.waitBeginTime).sum.toInt / customers.size
    val averageGoodSoldMillis =
      soldTimeMap.values.flatten.map(_ - openTime).sum.toInt / (stockNum * Good.values.size)
    val allSoldOutMillis =
      (allSoldOutTime - openTime).toInt
    val cashierCustomerNums =
      HashMap(cashiers.map(cashier => (cashier, cashier.customerNum.get())): _*)

    new SupermarketReport(
      averageCustomerWaitMillis,
      averageGoodSoldMillis,
      allSoldOutMillis,
      cashierCustomerNums)
  }

}




