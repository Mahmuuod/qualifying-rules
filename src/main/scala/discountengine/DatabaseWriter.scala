// File: src/main/scala/discountengine/DatabaseWriter.scala
package discountengine

import java.sql.{Connection, DriverManager, PreparedStatement}

object DatabaseWriter {

  /**
   * Writes the result data to PostgreSQL database.
   * - Connects to DB
   * - Creates table if it doesn't exist
   * - Performs batch insertion
   * - Uses logger for status updates and error tracking
   *
   * @param result List of (product_name, original_price, final_price, discount)
   * @param logger Logger instance to log events
   */
  def writeToPostgres(result: List[(String, Double, Double, Double)], logger: SimpleLogger): Unit = {

    // DB credentials and URL
    val url = "jdbc:postgresql://localhost:5432/postgres"
    val username = "postgres"
    val password = "123"

    var connection: Connection = null

    try {
      // Step 1: Connect to the database
      connection = DriverManager.getConnection(url, username, password)
      println("Connected to PostgreSQL!")

      // Step 2: Create table if not exists
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

      // Step 3: Prepare insert SQL
      val insertSQL =
        "INSERT INTO product_discounts (product_name, original_price, discount, final_price) VALUES (?, ?, ?, ?)"
      val pstmt: PreparedStatement = connection.prepareStatement(insertSQL)

      // Step 4: Add each product row to batch
      for ((product, original, discount, finalPrice) <- result) {
        pstmt.setString(1, product)
        pstmt.setDouble(2, original)
        pstmt.setDouble(3, discount)
        pstmt.setDouble(4, finalPrice)
        pstmt.addBatch()
      }

      // Step 5: Execute batch insert
      pstmt.executeBatch()
      logger.info("The Data Has Been Written to DB Successfully")

    } catch {
      case e: Exception =>
        logger.error("An error occurred on Writing to DB: " + e.getMessage)
        e.printStackTrace()
    } finally {
      // Step 6: Close DB connection if open
      if (connection != null) connection.close()
    }
  }
}
