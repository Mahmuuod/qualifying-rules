// File: src/main/scala/discountengine/DiscountCalculator.scala
package discountengine

object DiscountCalculator {
  type Order = (java.sql.Timestamp, String, java.util.Date, Int, Double, Int, String, Double, String, String)

  def calculate(order: Order, rules: List[(Order => Boolean, Order => Double)]): Double = {
    val discounts = rules.collect {
      case (qualifier, calculator) if qualifier(order) => calculator(order)
    }

    discounts match {
      case Nil          => 0.0
      case single :: Nil => single
      case _ => discounts.sorted.reverse.take(2).sum / 2
    }
  }
}
