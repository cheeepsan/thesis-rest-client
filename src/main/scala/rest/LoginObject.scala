package rest

import io.circe.{Decoder, Encoder, HCursor, Json}
import models.RequestEntity

case class LoginObject(username: String, password: String) extends RequestEntity
object LoginObject {

    implicit val encodeLogData: Encoder[LoginObject] = new Encoder[LoginObject] {
      final def apply(loginObject: LoginObject): Json = Json.obj(
        ("username", Json.fromString(loginObject.username)),
        ("password", Json.fromString(loginObject.password))
      )
    }

    implicit val decodeLogData: Decoder[LoginObject] = new Decoder[LoginObject] {
      final def apply(c: HCursor): Decoder.Result[LoginObject] =
        for {
          username <- c.downField("username").as[String]
          password <- c.downField("password").as[String]
        } yield {
          new LoginObject(username, password)
        }
    }
}

