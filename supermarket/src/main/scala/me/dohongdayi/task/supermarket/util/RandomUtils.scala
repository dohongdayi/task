package me.dohongdayi.task.supermarket.util

import scala.util.Random

object RandomUtils {

  private val random: Random = new Random

  /**
    * @param min inclusive
    * @param max inclusive
    * @return random integer between min and max
    */
  def randomInt(min: Int, max: Int): Int = {
    min + random.nextInt(max - min + 1)
  }

}
