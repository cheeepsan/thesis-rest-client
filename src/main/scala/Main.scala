import akka.actor.{ActorSystem, DeadLetter}
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import enums.DataMapping._
import enums.LogDataType
import models.{LogData, RequestWrapper}
import models.actors.{DeadLetterMonitorActor}
import other.Util
import rest.{Requests, RestInfo}
import services.{ExcelService, Integration}

import java.nio.file.Path
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, MINUTES}
import scala.concurrent.{Await, Future}
import scala.language.implicitConversions
import scala.util.{Failure, Success}
object Main {

  implicit val as: ActorSystem = ActorSystem("system")
  implicit val mat: Materializer.type = Materializer
  implicit val conf: Config = ConfigFactory.load()
  val restInfo: RestInfo = new RestInfo().apply
  implicit private val timeout: Timeout = Timeout(Duration.create(2, MINUTES))
  val meta: String = conf.getConfig("meta").getString("currentServer")

  def main(args: Array[String]): Unit = {
    import other.Util.processReq

    val deadLetterMonitorActor = as.actorOf(DeadLetterMonitorActor.props)
    as.eventStream.subscribe(
      deadLetterMonitorActor, classOf[DeadLetter])

    val fLoggedObj: Future[Either[Exception, RestInfo]] = restInfo.login(x => x)
    val f = for {
      loggedObj <- fLoggedObj
      _         = println(loggedObj)
    } yield {
      if (loggedObj.isRight) {
        implicit val loggedRestWithToken: RestInfo = loggedObj.toOption.fold(restInfo)(r => r)
        val integration = new Integration
        implicit val excel: ExcelService = new ExcelService
        excel.prepareData()
        val d = for {
          s <- Util.sendLogData(new LogData(LogDataType.STARTED, Map(SERVERNAME -> meta)))
          i <- integration.process(Path.of("datasample.json"))
          l <- Util.sendLogData(new LogData(LogDataType.RUNNING, Map(PROGRESS -> i.toString)))
          l <- Util.sendLogData(new LogData(LogDataType.RUNNING, Map(TOTAL -> i.toString)))
          f <- Util.sendLogData(new LogData(LogDataType.FINISHED, Map(MESSAGE -> "Integration has successfully finished")))
        } yield {
          println(s)
          println(i)
          println(f)
          println(l)
          f
        }
        d
      } else {
        val ret: Either[Exception, RequestWrapper] = Left(loggedObj.left.getOrElse(new Exception("Unable to log in:")))
        Future.successful(ret)
      }
    }.onComplete {
      case Failure(exception) =>
        println("end: " + exception)
        as.terminate()
      case Success(value) => println("Rest info: " + value)
        as.terminate()
    }
    Await.ready(f, Duration.Inf)

    //val integration = new Integration
    //val excel = new ExcelService
    //excel.prepareData()
    //Await.result(integration.process(Path.of("datasample.json"), excel), Duration.Inf)
    //excel.sheet.saveAsXlsx("data.xlsx")
    //as.terminate()
  }


  /**
   *
   *         integration.process(Path.of("datasample.json"), actor).flatMap {
          res =>
            println("Processed data: " + res.size)
            val f = actor ? new LogData(LogDataType.FINISHED, Map(MESSAGE -> "Integration has successfully finished"))
            f.flatMap {
              x =>
                Future.successful(x)
            }
            f.mapTo[Either[Exception, RequestWrapper]]
        }
   * @param restInfo
   */

  def process(restInfo: RestInfo) = {

  }

}
