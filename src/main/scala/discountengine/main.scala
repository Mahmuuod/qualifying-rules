// File: src/main/scala/Main.scala
package discountengine

import java.io.{FileWriter, PrintWriter}
import java.sql.{Connection, DriverManager, PreparedStatement, Timestamp}
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, MonthDay, ZoneId}
import java.util.Date
import scala.io.Source

object main {
  def main(args: Array[String]): Unit = {
    val logger = SimpleLogger("./logs/application.log")

    val orders = OrderParser.loadOrders("D:/study/iti/Scala/scala-iti45/Project/TRX1000.csv")

    val rules = DiscountRules.all

    val result: List[(String, Double, Double, Double)] = try {
      val finalDiscounts = orders.map(order => DiscountCalculator.calculate(order, rules))
      val originalPrices = orders.map(o => BigDecimal(o._5 * o._4).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble)
      val finalPrices = originalPrices.zip(finalDiscounts).map {
        case (price, disc) => BigDecimal(price - (price * disc)).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
      }
      val products = orders.map(_._2)

      val productsWithDiscounts = products.zip(originalPrices).zip(finalDiscounts).zip(finalPrices).map {
        case (((product, original), discount), finalPrice) => (product, original, finalPrice, discount)
      }

      logger.info("The Data Has Been Calculated Successfully")
      productsWithDiscounts
    } catch {
      case e: Exception =>
        logger.error("An error occurred on Data Processing: " + e.getMessage)
        List.empty
    }

    DatabaseWriter.writeToPostgres(result, logger)
  }
}
