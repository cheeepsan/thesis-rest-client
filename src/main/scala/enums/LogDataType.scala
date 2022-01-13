package enums

import io.circe.{Decoder, Encoder}

object LogDataType extends Enumeration {
  self =>

  type LogDataType = Value
  val ERROR, INFO, STARTED, FINISHED, EXITED, RUNNING = Value

  def decode: String = {
    self.Value match {
      case ERROR => "ERROR"
      case INFO => "INFO"
      case STARTED => "STARTED"
      case FINISHED => "FINISHED"
      case EXITED => "EXITED"
      case RUNNING => "RUNNING"
    }
  }

  implicit val logDataTypeDecoder: Decoder[LogDataType.Value] = Decoder.decodeEnumeration(LogDataType)
  implicit val logDataTypeEncoder: Encoder[LogDataType.Value] = Encoder.encodeEnumeration(LogDataType)
}
