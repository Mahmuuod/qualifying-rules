// File: src/main/scala/discountengine/OrderParser.scala
package discountengine

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.time.Instant
import scala.io.Source

object OrderParser {
  /**
   * Reads orders from a CSV file and transforms them into tuples with extra fields.
   * Each order is enriched with:
   * - daysBetween (expiry - transaction date)
   * - wineCheese category (first word of product name)
   * - total amount (quantity * unitPrice)
   *
   * @param path Path to the CSV file
   * @return List of enriched order tuples
   */
  def loadOrders(path: String): List[(Timestamp, String, Date, Int, Double, Int, String, Double, String, String)] = {
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd")

    val lines = Source.fromFile(path).getLines().toList.tail

    val orders = lines.map(x => {
      val y = x.split(",")

      //converting to time stamp
      val instant = Instant.parse(y(0))
      val timestamp = Timestamp.from(instant)

      val product_name = y(1)
      val expiry_date: Date = dateFormat.parse(y(2))
      val quantity = y(3).toInt
      val unit_price = y(4).toDouble
      val transaction_date: Date = new Date(timestamp.getTime)
      val daysBetween = (expiry_date.getTime - transaction_date.getTime) / (1000 * 60 * 60 * 24)
      val wine_cheese = y(1).split(" ")(0)
      val channel = y(5)
      val payment_method = y(6)
      val total = quantity.toDouble * unit_price

      (timestamp, product_name, expiry_date, quantity, unit_price, daysBetween.toInt, wine_cheese, total, channel, payment_method)
    })

    orders
  }
}
