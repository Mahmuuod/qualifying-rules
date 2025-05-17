// File: src/main/scala/discountengine/OrderParser.scala
package discountengine

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.time.Instant
import scala.io.Source

object OrderParser {
  def loadOrders(path: String): List[(Timestamp, String, Date, Int, Double, Int, String, Double, String, String)] = {
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd")

    val lines = Source.fromFile(path).getLines().toList.tail

    lines.map { line =>
      val y = line.split(",")
      val instant = Instant.parse(y(0))
      val timestamp = Timestamp.from(instant)
      val productName = y(1)
      val expiryDate = dateFormat.parse(y(2))
      val quantity = y(3).toInt
      val unitPrice = y(4).toDouble
      val channel = y(5)
      val paymentMethod = y(6)
      val transactionDate = new Date(timestamp.getTime)
      val daysBetween = ((expiryDate.getTime - transactionDate.getTime) / (1000 * 60 * 60 * 24)).toInt
      val wineCheese = productName.split(" ")(0)
      val total = quantity * unitPrice

      (timestamp, productName, expiryDate, quantity, unitPrice, daysBetween, wineCheese, total, channel, paymentMethod)
    }
  }
}
