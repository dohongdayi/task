package me.dohongdayi.task.supermarket

import me.dohongdayi.task.supermarket.SupermarketState._
import me.dohongdayi.task.supermarket.base.UnitSpec

class SupermarketSpec extends UnitSpec {

  "A Supermarket" should "in INITIALIZED state after creation" in {
    val supermarket = new Supermarket
    assert(INITIALIZED == supermarket.getState)
  }

  it should "has 15 stock for each good after creation" in {
    val supermarket = new Supermarket
    val stock = supermarket.getStock
    Good.values.foreach(
      good => assert(15 == stock(good))
    )
  }

  it should "has 3 cashiers after creation" in {
    val supermarket = new Supermarket
    val cashiers = supermarket.getCashiers
    assert(3 == cashiers.size)
    cashiers.indices.foreach(
      index => assert(index + 1 == cashiers(index).id)
    )
  }

  it should "in OPENED state after open()" in {
    val supermarket = new Supermarket
    supermarket.open()
    assert(OPENED == supermarket.getState)
    supermarket.close()
  }

  it should "has at least 1 customer after 4 seconds since open()" in {
    val supermarket = new Supermarket
    supermarket.open()
    Thread.sleep(4 * 1000L)
    assert(supermarket.getCustomers.nonEmpty)
    supermarket.close()
  }

  it should "has sold some goods after 11 seconds since open()" in {
    val supermarket = new Supermarket
    supermarket.open()
    Thread.sleep(11 * 1000L)
    val customerNum = supermarket.getCashiers.map(_.customerNum.get()).sum
    assert(customerNum > 0)
    supermarket.close()
  }

  it should "in CLOSED state after close()" in {
    val supermarket = new Supermarket
    supermarket.open()
    Thread.sleep(10 * 1000L)
    supermarket.close()
    assert(CLOSED == supermarket.getState)
  }

  it should "not sell any good after close()" in {
    val supermarket = new Supermarket
    supermarket.open()
    Thread.sleep(10 * 1000L)
    supermarket.close()
    val stock1 = supermarket.getStock
    Thread.sleep(10 * 1000L)
    val stock2 = supermarket.getStock
    assert(stock1 == stock2)
  }

  it should "in CLOSED state after waitForClose()" in {
    val supermarket = new Supermarket(stockNum = 2)
    supermarket.open()
    supermarket.waitForClose()
    assert(CLOSED == supermarket.getState)
  }

  it should "sold out all goods after waitForClose()" in {
    val supermarket = new Supermarket(stockNum = 2)
    supermarket.open()
    supermarket.waitForClose()
    val stock = supermarket.getStock
    assert(!stock.values.exists(_ > 0))
  }

  it should "has report after waitForClose()" in {
    val supermarket = new Supermarket
    supermarket.open()
    supermarket.waitForClose()

    val report = supermarket.report()
    assert(null != report)

    println(s"Every customer waited ${report.averageCustomerWaitMillis} milliseconds in average.")
    println(s"Every good took ${report.averageGoodSoldMillis} milliseconds to be sold in average.")
    println(s"It took ${report.allSoldOutMillis} milliseconds to sold out all goods.")
    report.cashierCustomerNums.keys.foreach(
      cashier => println(s"Cashier${cashier.id} received ${report.cashierCustomerNums(cashier)} customers.")
    )
  }

}
