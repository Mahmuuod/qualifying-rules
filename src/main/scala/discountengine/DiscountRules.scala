// File: src/main/scala/discountengine/DiscountRules.scala
package discountengine

import java.sql.Timestamp
import java.util.Date
import java.time.{MonthDay, ZoneId}

object DiscountRules {

  type Order = (Timestamp, String, Date, Int, Double, Int, String, Double, String, String)

  // rule : expire less than 30 days
  val expirationQualifier: Order => Boolean = o => o._6 > 0 && o._6 < 30
  val expirationCalculate: Order => Double = o => (30 - o._6) / 100.0

  // rule : cheese and wine
  val cheeseWineQualifier: Order => Boolean = o => o._7.equals("Wine") || o._7.equals("Cheese")
  val cheeseWineCalculate: Order => Double = o => {
    val cheeseWine = o._7
    val discount = if (cheeseWine.equals("Wine")) {
      0.05
    }
    else if (cheeseWine.equals("Cheese")) {
      0.1
    }
    else
      0.0

    discount
  }

  // rule : merch 23 march
  val merchQualifier: Order => Boolean = o => {
    val transactionDate = o._1.toInstant.atZone(ZoneId.systemDefault()).toLocalDate
    val targetMonthDay = MonthDay.parse("--03-23")
    val transactionMonthDay = MonthDay.from(transactionDate)
    transactionMonthDay == targetMonthDay
  }
  val merchCalculate: Order => Double = _ => 0.5

  // rule : quantity based
  val quantityQualifier: Order => Boolean = o => o._4 > 5
  val quantityCalculate: Order => Double = o => {
    val quantity = o._4
    val discount = if (quantity >= 6 && quantity <= 9) 0.05
    else if (quantity > 9 && quantity <= 14) 0.07
    else if (quantity >= 15) 0.1
    else 0.0

    discount
  }

  // rule : app channel
  val channelQualifier: Order => Boolean = o => o._9.equals("App")
  val channelCalculate: Order => Double = o => {
    val quantity = o._4
    val discount = (Math.ceil(quantity.toDouble / 5) * 5 * 0.01).min(0.5)
    discount
  }

  // rule : visa payment
  val paymentQualifier: Order => Boolean = o => o._10.equals("Visa")
  val paymentCalculate: Order => Double = _ => 0.05

  // all rules in one list
  val all: List[(Order => Boolean, Order => Double)] = List(
    (expirationQualifier, expirationCalculate),
    (quantityQualifier, quantityCalculate),
    (merchQualifier, merchCalculate),
    (cheeseWineQualifier, cheeseWineCalculate),
    (channelQualifier, channelCalculate),
    (paymentQualifier, paymentCalculate)
  )

}
