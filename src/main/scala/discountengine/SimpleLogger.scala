// File: src/main/scala/SimpleLogger.scala
package discountengine

import java.io.{FileWriter, PrintWriter}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

case class SimpleLogger(logFilePath: String) {
  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

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
