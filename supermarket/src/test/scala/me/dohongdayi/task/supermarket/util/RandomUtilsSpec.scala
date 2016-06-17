package me.dohongdayi.task.supermarket.util

import me.dohongdayi.task.supermarket.util.RandomUtils._
import me.dohongdayi.task.supermarket.base.UnitSpec

class RandomUtilsSpec extends UnitSpec {

  "RandomUtils.randomInt()" should "return different integers for each invocation" in {
    val result1 = randomInt(100, 1000)
    val result2 = randomInt(100, 1000)
    println(result1)
    println(result2)
    assert(result1 != result2)
  }

}
