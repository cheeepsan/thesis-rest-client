package services

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.{IOResult, Materializer}
import akka.stream.alpakka.json.scaladsl.JsonReader
import akka.stream.scaladsl.{FileIO, Flow, Sink, Source}
import akka.util.{ByteString, Timeout}
import io.circe.{Decoder, Json, ParsingFailure}
import models.{LogData, RequestWrapper, RestaurantData}
import io.circe.parser._

import java.nio.file.Path
import scala.concurrent.{ExecutionContext, Future}
import RestaurantData.decodeRestaurantData
import akka.pattern.ask
import enums.DataMapping.{MESSAGE, PROGRESS}
import enums.LogDataType
import other.Util
import rest.{Requests, RestInfo}
import spoiwo.model.Sheet

import java.time.LocalDateTime
import scala.concurrent.duration.{Duration, MINUTES}
import scala.util.{Failure, Success}

class Integration(implicit val a: ActorSystem, mat: Materializer) {
  implicit private val timeout: Timeout = Timeout(Duration.create(2, MINUTES))

  private val parseFlow = Flow[ByteString].map(bs => parse(bs.utf8String))
  private def parseToModel[A](implicit decoder: Decoder[A]) = Flow[Either[Exception, Json]].map {
    case Left(e: ParsingFailure) => Left(e)
    case Right(value) => value.as[A]
  }

  def process(pathToFile: Path)(implicit excel: ExcelService, ec: ExecutionContext, restInfo: RestInfo) = {

    val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(pathToFile)


    source.via(JsonReader.select("$[*]"))
      .via(parseFlow)
      .via(parseToModel).mapAsync(4) {
      case Left(err) =>
        // TODO: LOG ERROR
        val l = LogData(LogDataType.ERROR, Map(MESSAGE -> err.toString), LocalDateTime.now())
        Util.sendLogData(l).map(Option.apply)
      case Right(value) =>
        try {
          excel.fillColumn(value) // ???? TODO: SHOULD BE INTERFACED, PASSED AS ARGUMENT
          // TODO: LOG DEBUG
          Future.successful(None)
        } catch {
          case e: Exception =>
            val l = LogData(LogDataType.ERROR, Map(MESSAGE -> e.toString), LocalDateTime.now())
            // TODO: LOG ERROR
            Util.sendLogData(l).map(Option.apply)
        }
    }.foldAsync(0) {
      case (acc, data) =>
            // TODO: LOG data TO DEBUG
            val nextAcc = acc + 1
            if (nextAcc % 100 == 0) {
              val l = LogData(LogDataType.RUNNING, Map(PROGRESS -> nextAcc.toString), LocalDateTime.now())
              Util.sendLogData(l).map(Option.apply).flatMap(res =>
                // TODO: LOG res TO DEBUG
                Future.successful(nextAcc))
            } else Future.successful(nextAcc)
    }.runWith(Sink.last)
  }
}
