import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import scala.io.Source
import java.time.Instant
import java.time.{MonthDay ,ZoneId}
import java.sql.{Connection, DriverManager, ResultSet,PreparedStatement}
import java.io.{FileWriter, PrintWriter}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
object main {
  def main(args: Array[String]): Unit = {

    //logger object
    object SimpleLogger {
      private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
      private val logFilePath = "./logs/application.log" // Log file name

      private def writeToLog(level: String, message: String): Unit = {
        val timestamp = LocalDateTime.now().format(formatter)
        val logLine = f"$timestamp%-20s $level%-8s $message"

        // Print to console
        println(logLine)

        // Append to file
        val writer = new PrintWriter(new FileWriter(logFilePath, true))
        try {
          writer.println(logLine)
        } finally {
          writer.close()
        }
      }

      def info(message: String): Unit = writeToLog("INFO", message)

      def debug(message: String): Unit = writeToLog("DEBUG", message)

      def warn(message: String): Unit = writeToLog("WARN", message)

      def error(message: String): Unit = writeToLog("ERROR", message)
    }

    //timestamp,product_name,expiry_date,quantity,unit_price,channel,payment_method
    //timestamp,product_name,expiry_date,quantity,unit_price,daysBetween
    val lines = Source.fromFile("D:\\study\\iti\\Scala\\scala-iti45\\Project\\TRX1000.csv").getLines().toList.tail

    //gets the data in suitable format with some calculations
    val orders = (lines).map(x => {
      val y = x.split(",")
      //converting to time stamp
      val instant = Instant.parse(y(0))
      val timestamp = Timestamp.from(instant)

      val product_name = y(1)
      val dateFormat = new SimpleDateFormat("yyyy-MM-dd") // adjust format as per your date string
      val expiry_date: Date = dateFormat.parse(y(2))
      val quantity = y(3).toInt
      val unit_price = y(4).toDouble
      val transaction_date: Date = new Date(timestamp.getTime)
      val daysBetween = (expiry_date.getTime - transaction_date.getTime) / (1000 * 60 * 60 * 24) //get time returns milli secs
      val wine_cheese = y(1).split(" ")(0)
      val channel = y(5)
      val payment_method = y(6)
      val total = quantity.toDouble * unit_price
      (timestamp, product_name, expiry_date, quantity, unit_price, daysBetween.toInt, wine_cheese, total, channel, payment_method)
    })


    def expirationQualifier(order: (Timestamp, String, Date, Int, Double, Int, String, Double, String, String)): Boolean = {
      val daysBetween = order._6
      val result = if (daysBetween > 0 && daysBetween < 30) {
        true
      }
      else {
        false
      }

      result
    }

    def expirationCalculate(order: (Timestamp, String, Date, Int, Double, Int, String, Double, String, String)): Double = {
      val daysBetween = order._6
      val discount = ((30 - daysBetween) / 100.0)

      discount
    }

    def cheeseWineQualifier(order: (Timestamp, String, Date, Int, Double, Int, String, Double, String, String)): Boolean = {
      val cheese_wine: Boolean = if (order._7.equals("Wine") || order._7.equals("Cheese")) true else false
      cheese_wine
    }

    def cheeseWineCalculate(order: (Timestamp, String, Date, Int, Double, Int, String, Double, String, String)): Double = {
      val cheeseWine: String = order._7
      val discount: Double = if (cheeseWine.equals("Wine")) {
        0.05
      }
      else if (cheeseWine.equals("Cheese")) {
        0.1
      }
      else
        0.0

      discount
    }


    def MerchQualifier(order: (Timestamp, String, Date, Int, Double, Int, String, Double, String, String)): Boolean = {
      val transactionDate = order._1.toInstant.atZone(ZoneId.systemDefault()).toLocalDate

      val targetMonthDay = MonthDay.parse("--03-23") // Note the -- prefix for MonthDay

      val transactionMonthDay = MonthDay.from(transactionDate)

      transactionMonthDay == targetMonthDay
    }

    def MerchCalculate(order: (Timestamp, String, Date, Int, Double, Int, String, Double, String, String)): Double = {
      0.5
    }


    def quantityQualifier(order: (Timestamp, String, Date, Int, Double, Int, String, Double, String, String)): Boolean = {
      val quantity = order._4
      val result = if (quantity > 5) true else false
      result

    }

    def quantityCalculate(order: (Timestamp, String, Date, Int, Double, Int, String, Double, String, String)): Double = {
      val unitPrice = order._5
      val quantity = order._4.toDouble

      val discount: Double = if (quantity >= 6 && quantity <= 9) 0.05
      else if (quantity > 9 && quantity <= 14) 0.07
      else if (quantity >= 15) 0.1
      else 0.0

      discount

    }

    def channelQualifier(order: (Timestamp, String, Date, Int, Double, Int, String, Double, String, String)): Boolean = {
      val channel = order._9
      val result = if (channel.equals("App")) true else false
      result

    }

    def channelCalculate(order: (Timestamp, String, Date, Int, Double, Int, String, Double, String, String)): Double = {
      val quantity = order._4
      val discount: Double = if (quantity < 6) 0.05
      else if (quantity < 10) 0.1
      else if (quantity < 15) 0.15
      else 0.2

      discount

    }

    def paymentQualifier(order: (Timestamp, String, Date, Int, Double, Int, String, Double, String, String)): Boolean = {
      val payment_method = order._10
      val result = if (payment_method.equals("Visa")) true else false
      result

    }

    def paymentCalculate(order: (Timestamp, String, Date, Int, Double, Int, String, Double, String, String)): Double = {
      0.05
    }

    // all rules paired in tuples
    val expirationRule = (expirationQualifier _, expirationCalculate _)
    val quantityRule = (quantityQualifier _, quantityCalculate _)
    val MerchRule = (MerchQualifier _, MerchCalculate _)
    val cheeseWineRule = (cheeseWineQualifier _, cheeseWineCalculate _)
    val channelRule = (channelQualifier _, channelCalculate _)
    val paymentRule = (paymentQualifier _, paymentCalculate _)

    //list of tuples , each tuple contains a rule
    val rules: List[
      (
        ((Timestamp, String, Date, Int, Double, Int, String, Double, String, String)) => Boolean,
          ((Timestamp, String, Date, Int, Double, Int, String, Double, String, String)) => Double
        )
    ] = List(expirationRule, quantityRule, MerchRule, cheeseWineRule, channelRule, paymentRule)

    // higher order func , takes list of rules , each rule is a tuple of two funcs , and takes an order to be applied on this list
    def calculate_discount(order: (Timestamp, String, Date, Int, Double, Int, String, Double, String, String), rules: List[(
      ((Timestamp, String, Date, Int, Double, Int, String, Double, String, String)) => Boolean,
        ((Timestamp, String, Date, Int, Double, Int, String, Double, String, String)) => Double
      )
    ]): Double = {

      val discount = rules.map(x => {
        if (x._1(order)) x._2(order) else 0.0
      }).filter(_ > 0) // filter out 0.0
      if (discount.isEmpty)
        0.0
      else if (discount.size == 1)
        discount(0)
      else
        discount.toVector.sorted.reverse.take(2).sum / 2
    }

    // main calculation block , uses previous functions
    val result: List[(String, Double, Double, Double)]=try {

      val final_discount = (orders).map(order => calculate_discount(order, rules))

      val product_prices = (orders).map(x => BigDecimal(x._5 * x._4).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble)

      val final_price = product_prices.zip(final_discount).
        map { case (a, b) => BigDecimal(a - (a * b)).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble } //a*b discount amount , a the original price

      val products = (orders).map(x => x._2)

      val products_prices = products.zip(final_price).zip(final_discount).zip(product_prices).map { case (((product, fPrice), tPrice), discount) =>
        (product, fPrice.toDouble, tPrice.toDouble, discount.toDouble)
      }

      (products_prices).foreach(println)

      SimpleLogger.info(s"The Data Has Been Calculated Successfully")

      products_prices
    }
    catch {
      case e: Exception =>
        SimpleLogger.error(s"An error occurred on Data Processing: ${e.getMessage}")
        List.empty[(String, Double, Double, Double)] // return an empty list
    }

// writing to db
    val url = "jdbc:postgresql://localhost:5432/postgres"
    val username = "postgres"
    val password = "123"

    var connection: Connection = null

    try {
      // Connect to PostgreSQL
      connection = DriverManager.getConnection(url, username, password)
      println("Connected to PostgreSQL!")

      // Do some query (optional)
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

      for ((product, original, discount, finalPrice) <- result) {
        pstmt.setString(1, product)
        pstmt.setDouble(2, original)
        pstmt.setDouble(3, discount)
        pstmt.setDouble(4, finalPrice)
        pstmt.executeUpdate()

      }

      SimpleLogger.info(s"The Data Has Been Written to DB Successfully")
    } catch {
      case e: Exception =>
        SimpleLogger.error(s"An error occurred on Writing to DB: ${e.getMessage}")
        e.printStackTrace()
    } finally {
      if (connection != null) connection.close()
    }


  }
}

