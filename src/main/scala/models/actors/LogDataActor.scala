package models.actors

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import models.{LogData, RequestWrapper}
import rest.{Requests, RestInfo}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions
import other.Util.processReq

import scala.util.{Failure, Success}

object LogDataActor {
  def props(restInfo: RestInfo): Props = Props(new LogDataActor()(restInfo))
}

class LogDataActor(implicit restInfo: RestInfo) extends Actor with ActorLogging {
  implicit val ac: ActorSystem = context.system


  override def receive: Receive = {
    case l: LogData =>
      log.info("data logged: " + l.toString)
      restInfo.post(Requests.postLogData(RequestWrapper("", Option(l))))(x => x).onComplete {
        case Failure(exception) =>
          val errorObject: Either[Exception, RequestWrapper] = Left(exception.asInstanceOf[Exception])
          sender() ! errorObject
        case Success(value) =>
          sender() ! value
      }
    case _ => ()
  }
}
