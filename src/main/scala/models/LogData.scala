package models

import akka.http.scaladsl.model.DateTime
import enums.DataMapping
import enums.DataMapping.DataMapping
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, HCursor, Json}
import enums.LogDataType.LogDataType

import java.time.{Instant, LocalDateTime, ZoneOffset}

case class LogData(dataType: LogDataType, data: Map[DataMapping, String], timestamp: LocalDateTime = LocalDateTime.now) extends RequestEntity
//case class LogData(dataType: LogDataType, data: Map[String, String], timestamp: LocalDateTime = LocalDateTime.now) extends RequestEntity
object LogData {
  import enums.LogDataType.logDataTypeEncoder

  implicit val encodeLogData: Encoder[LogData] = new Encoder[LogData] {
    final def apply(logData: LogData): Json = Json.obj(
      ("dataType", logData.dataType.asJson),
      ("data", logData.data.map { case (k, v) => ((DataMapping.encode(k) -> v))}.asJson),
      ("timestamp", Json.fromLong(logData.timestamp.toInstant(ZoneOffset.UTC).getEpochSecond))
    )
  }

  implicit val decodeLogData: Decoder[LogData] = new Decoder[LogData] {
    final def apply(c: HCursor): Decoder.Result[LogData] =
      for {
        dataType <- c.downField("dataType").as[LogDataType]
        data <- c.downField("data").as[Map[String, String]]
        timestamp <- c.downField("timestamp").as[Long].map(t => LocalDateTime.ofEpochSecond(t, 0, ZoneOffset.UTC))
      } yield {

        new LogData(dataType, data.map{case (k, v) => (DataMapping.decode(k) -> v)}, timestamp)
      }
  }

}