package me.dohongdayi.task.supermarket

/**
  * Supermarket report after close
  *
  * @param averageCustomerWaitMillis How many milliseconds every customer waited in average to cash after purchased a good
  * @param averageGoodSoldMillis How many milliseconds for selling a good in average
  * @param allSoldOutMillis How many milliseconds it took from opened supermarket and sold out all goods
  * @param cashierCustomerNums How many customers each cashier received
  */
case class SupermarketReport(
    val averageCustomerWaitMillis: Int,
    val averageGoodSoldMillis: Int,
    val allSoldOutMillis: Int,
    val cashierCustomerNums: Map[Cashier, Int]
  )
