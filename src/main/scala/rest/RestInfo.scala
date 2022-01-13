package rest

import akka.http.scaladsl.model.HttpRequest
import com.typesafe.config.Config
import io.circe
import models.RequestWrapper

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

case class RestInfo(url: Option[String] = None,
               username: Option[String] = None,
               password: Option[String] = None,
               token: Option[String] = None) {
  self =>

  def getUrl: String = self.url.fold("")(x => x)
  private def createLoginObject: RequestWrapper = RequestWrapper("", LoginObject(self.username.getOrElse(""), self.password.getOrElse("")))
  implicit def wrapToOption[T](x: T): Option[T] = Option[T](x)

  def apply(implicit configuration: Config): RestInfo = {
    val restConf = configuration.getConfig("rest")

    val url = restConf.getString("url")
    val username = restConf.getString("username")
    val password = restConf.getString("password")

    RestInfo(url, username, password)
  }

  def login(fx : HttpRequest => Future[Either[Exception, RequestWrapper]])
           (implicit ec: ExecutionContext): Future[Either[Exception, RestInfo]] =  {
    fx(Requests.login(createLoginObject)(self)).map {
      case Left(value) => Left(value)
      case Right(value) =>Right(self.copy(token = value.token))
    }
  }


  def post(request: HttpRequest)(fx : HttpRequest => Future[Either[Exception, RequestWrapper]])
          (implicit ec: ExecutionContext): Future[Either[Exception, RequestWrapper]] =  {
    fx(request)
  }

  override def toString: String = { // not to show pass or token
    s"""Url: $url, username: $username"""
  }

}
