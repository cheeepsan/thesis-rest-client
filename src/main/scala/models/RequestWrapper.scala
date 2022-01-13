package models

import akka.http.scaladsl.model.HttpResponse
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Sink}
import akka.util.ByteString
import io.circe
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, HCursor, Json}

import scala.concurrent.{ExecutionContext, Future}
import io.circe.parser.parse
import rest.LoginObject


case class RequestWrapper(token: String, data: Option[RequestEntity])
trait RequestEntity

object RequestWrapper {
  import LogData.encodeLogData
  import LoginObject.encodeLogData

  implicit val encodeRequestWrapper: Encoder[RequestWrapper] = new Encoder[RequestWrapper] {

    def fillModel[A <: RequestEntity](wrapper: RequestWrapper)(implicit encodeA: Encoder[A]): Json =
      Json.obj(
        ("token", Json.fromString(wrapper.token)),
        ("data", wrapper.data.fold(None.asJson)(d => d.asInstanceOf[A].asJson))
      )

    final def apply(wrapper: RequestWrapper): Json = {
      val default: Json = Json.obj(
        ("token", Json.fromString(wrapper.token)),
        ("data", None.asJson)
      )
      wrapper.data.fold(default) {
          case _: LogData => fillModel[LogData](wrapper)
          case _: LoginObject => fillModel[LoginObject](wrapper)
          case _ => default
      }

    }
  }

  implicit val decodeRequestWrapper: Decoder[RequestWrapper] = new Decoder[RequestWrapper] {
    final def apply(c: HCursor): Decoder.Result[RequestWrapper] =
      for {
        token <- c.downField("token").as[String]
        logData <- c.downField("data").as[Option[LogData]]
      } yield {
        new RequestWrapper(token, logData)
      }
  }

  def streamToRequestWrapper(response: HttpResponse)(implicit mat: Materializer, ec: ExecutionContext): Future[Either[circe.Error, RequestWrapper]] = {
    val sinkFold: Sink[ByteString, Future[ByteString]] = Sink.fold(ByteString("")) { case (acc, str) =>
      acc ++ str
    }

    response.entity.dataBytes.runWith(sinkFold).map {
      bs: ByteString =>
        val s = bs.utf8String
        parse(s) match {
          case Left(value) => Left(value)
          case Right(value) => value.as[RequestWrapper]
        }
    }
  }
}
