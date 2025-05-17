// File: src/main/scala/discountengine/DatabaseWriter.scala
package discountengine

import java.sql.{Connection, DriverManager, PreparedStatement}

object DatabaseWriter {
  def writeToPostgres(data: List[(String, Double, Double, Double)], logger: SimpleLogger): Unit = {
    val url = "jdbc:postgresql://localhost:5432/postgres"
    val username = "postgres"
    val password = "123"

    var connection: Connection = null

    try {
      connection = DriverManager.getConnection(url, username, password)
      logger.info("Connected to PostgreSQL!")

      val createTableSQL =
        """
          |CREATE TABLE IF NOT EXISTS product_discounts (
          |  id SERIAL PRIMARY KEY,
          |  product_name VARCHAR(255),
          |  original_price DOUBLE PRECISION,
          |  discount DOUBLE PRECISION,
          |  final_price DOUBLE PRECISION
          |);
        """.stripMargin

      val stmt = connection.createStatement()
      stmt.execute(createTableSQL)

      val insertSQL =
        "INSERT INTO product_discounts (product_name, original_price, discount, final_price) VALUES (?, ?, ?, ?)"
      val pstmt: PreparedStatement = connection.prepareStatement(insertSQL)

      for ((product, original, discount, finalPrice) <- data) {
        pstmt.setString(1, product)
        pstmt.setDouble(2, original)
        pstmt.setDouble(3, discount)
        pstmt.setDouble(4, finalPrice)
        pstmt.addBatch()
      }

      pstmt.executeBatch()
      logger.info("The Data Has Been Written to DB Successfully")
    } catch {
      case e: Exception =>
        logger.error("An error occurred on Writing to DB: " + e.getMessage)
        e.printStackTrace()
    } finally {
      if (connection != null) connection.close()
    }
  }
}