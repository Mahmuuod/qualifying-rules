// File: src/main/scala/discountengine/DiscountCalculator.scala
package discountengine

object DiscountCalculator {
  // Alias type for clarity
  type Order = (java.sql.Timestamp, String, java.util.Date, Int, Double, Int, String, Double, String, String)

  /**
   * Applies a list of qualifying rules and returns the final discount for an order.
   * - If no rules apply → discount = 0.0
   * - If one rule applies → use that discount
   * - If multiple rules apply → take top 2 discounts and average them
   *
   * @param order A single order tuple
   * @param rules A list of (qualifier, calculator) rule pairs
   * @return The calculated discount as a Double
   */
  def calculate(order: Order, rules: List[(Order => Boolean, Order => Double)]): Double = {

    // Collect all discounts where the order qualifies
    val discount = rules.map(rule => {
      if (rule._1(order)) rule._2(order) else 0.0
    }).filter(_ > 0)

    // Determine discount based on how many rules matched
    if (discount.isEmpty)
      0.0
    else if (discount.size == 1)
      discount(0)
    else
      discount.toVector.sorted.reverse.take(2).sum / 2
  }
}
