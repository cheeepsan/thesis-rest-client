package rest

import akka.http.scaladsl.model.headers.{Authorization, RawHeader}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest}
import akka.util.ByteString
import com.typesafe.config.Config
import io.circe.Json
import models.{RequestEntity, RequestWrapper}
import io.circe.generic.auto._
import io.circe.syntax._
import models.RequestWrapper._

import scala.language.implicitConversions

// https://doc.akka.io/docs/akka-http/current/client-side/request-and-response.html#creating-requests
object Requests{
  def postLogData(payload: RequestWrapper)(implicit restInfo: RestInfo): HttpRequest = {
    createHttpPostRequest(restInfo, payload, "logData")
  }

  def login(payload: RequestWrapper)(implicit restInfo: RestInfo): HttpRequest = {
    createHttpPostRequest(restInfo, payload, "login")
  }

  private def createHttpPostRequest(restInfo: RestInfo, payload: RequestWrapper, route: String) = {
    val s = (payload.asJson \\ "data").headOption.getOrElse(Json.Null).toString() //quick hack, TODO: fix by adding decoders for data <: RequestEntity
    HttpRequest(
      method = HttpMethods.POST,
      headers = Seq(RawHeader("AuthToken", restInfo.token.getOrElse(""))), // should be BasicHttpCredentials, but I'm lazy
      uri = s"${restInfo.getUrl}/$route",
      entity = HttpEntity(ContentTypes.`application/json`, ByteString(s))
    )
  }
}
