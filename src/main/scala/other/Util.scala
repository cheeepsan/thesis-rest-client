package other

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import io.circe
import models.{LogData, RequestWrapper}
import rest.{Requests, RestInfo}

import scala.concurrent.{ExecutionContext, Future}

object Util {
  implicit def processReq(req: HttpRequest)(implicit ac: ActorSystem, ec: ExecutionContext): Future[Either[Exception, RequestWrapper]] = {
    Http()
      .singleRequest(req)
      .flatMap {
        res =>
          res.status match {
            case StatusCodes.BadRequest => Future.successful(Left(new Exception(s"Bad request: ${res.toString()}")))
            case _ => RequestWrapper.streamToRequestWrapper(res)
          }
      }.recoverWith {
      case e: Exception =>
        Future.successful(Left(e))
    }
  }

  def sendLogData(l: LogData)(implicit restInfo: RestInfo, ec: ExecutionContext, ac: ActorSystem): Future[Either[Exception, RequestWrapper]] = {
    import scala.language.implicitConversions

    restInfo.post(Requests.postLogData(RequestWrapper("", Option(l))))(x => x)
  }
}
