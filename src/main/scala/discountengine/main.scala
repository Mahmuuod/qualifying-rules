// File: src/main/scala/Main.scala
package discountengine


object main {
  def main(args: Array[String]): Unit = {
    // create logger instance with log file path
    val logger = SimpleLogger("./logs/application.log")
 
    // load and preprocess orders from CSV file
    val orders = OrderParser.loadOrders("D:/study/iti/Scala/scala-iti45/Project/TRX1000.csv")

    // get all discount rules (qualifier and calculation pairs)
    val rules = DiscountRules.all

    // calculate result using rules and input data
    val result: List[(String, Double, Double, Double)] = try {

      // calculate discount for each order
      val final_discount = (orders).map(order => DiscountCalculator.calculate(order, rules))

      // calculate original price (unit price * quantity)
      val product_prices = (orders).map(x => BigDecimal(x._5 * x._4).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble)

      // apply discount and calculate final price
      val final_price = product_prices.zip(final_discount).
        map { case (a, b) => BigDecimal(a - (a * b)).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble } // a*b discount amount , a the original price

      // extract product names
      val products = (orders).map(x => x._2)

      // combine product name, original price, discount, and final price into one result list
      val products_prices = products.zip(product_prices).zip(final_discount).zip(final_price).map { case (((product, fPrice), tPrice), discount) =>
        (product, fPrice.toDouble, tPrice.toDouble, discount.toDouble)
      }

      // log success
      logger.info("The Data Has Been Calculated Successfully")

      // return result
      products_prices
    } catch {
      case e: Exception =>
        // log error
        logger.error("An error occurred on Data Processing: " + e.getMessage)
        // return empty list if failure
        List.empty[(String, Double, Double, Double)]
    }

    // write the result to database
    DatabaseWriter.writeToPostgres(result, logger)
  }
}
