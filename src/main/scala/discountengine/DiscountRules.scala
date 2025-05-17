// File: src/main/scala/discountengine/DiscountRules.scala
package discountengine

import java.sql.Timestamp
import java.util.Date
import java.time.{MonthDay, ZoneId}

object DiscountRules {
  type Order = (Timestamp, String, Date, Int, Double, Int, String, Double, String, String)

  val expirationQualifier: Order => Boolean = o => o._6 > 0 && o._6 < 30
  val expirationCalculate: Order => Double = o => (30 - o._6) / 100.0

  val cheeseWineQualifier: Order => Boolean = o => o._7 == "Wine" || o._7 == "Cheese"
  val cheeseWineCalculate: Order => Double = {
    case o if o._7 == "Cheese" => 0.1
    case o if o._7 == "Wine"   => 0.05
    case _                      => 0.0
  }

  val merchQualifier: Order => Boolean = o => {
    val transactionDate = o._1.toInstant.atZone(ZoneId.systemDefault()).toLocalDate
    val targetDate = MonthDay.parse("--03-23")
    MonthDay.from(transactionDate) == targetDate
  }
  val merchCalculate: Order => Double = _ => 0.5

  val quantityQualifier: Order => Boolean = o => o._4 > 5
  val quantityCalculate: Order => Double = o => {
    val q = o._4
    if (q >= 6 && q <= 9) 0.05
    else if (q >= 10 && q <= 14) 0.07
    else if (q >= 15) 0.1
    else 0.0
  }

  val channelQualifier: Order => Boolean = o => o._9 == "App"
  val channelCalculate: Order => Double = o => {
    val q = o._4.toDouble
    (Math.ceil(q / 5) * 5 * 0.01).min(0.5)
  }

  val paymentQualifier: Order => Boolean = o => o._10 == "Visa"
  val paymentCalculate: Order => Double = _ => 0.05

  val all: List[(Order => Boolean, Order => Double)] = List(
    (expirationQualifier, expirationCalculate),
    (quantityQualifier, quantityCalculate),
    (merchQualifier, merchCalculate),
    (cheeseWineQualifier, cheeseWineCalculate),
    (channelQualifier, channelCalculate),
    (paymentQualifier, paymentCalculate)
  )
}
