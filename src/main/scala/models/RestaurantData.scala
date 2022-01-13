package models

import io.circe.Decoder.Result
import io.circe.syntax.EncoderOps
import io.circe.{ACursor, CursorOp, Decoder, DecodingFailure, Encoder, HCursor, Json, parser}
import models.RestaurantDataType.RestaurantDataType

case class GeoData(coordinates: List[Double], dataType: String) {
  self =>
    override def toString: String = {
      self.coordinates.reverse.mkString(", ")
    }
}
object GeoData {
  implicit val encodeGeoData: Encoder[GeoData] = new Encoder[GeoData] {
    final def apply(geoData: GeoData): Json = Json.obj(
      ("coordinates", geoData.coordinates.asJson),
      ("dataType", Json.fromString(geoData.dataType))
    )
  }

  implicit val decodeGeoData: Decoder[GeoData] = new Decoder[GeoData] {
    final def apply(c: HCursor): Decoder.Result[GeoData] =
      for {
        coordinates <- c.downField("coordinates").as[List[Double]]
        dataType <- c.downField("type").as[String]
      } yield {
        new GeoData(coordinates, dataType)
      }
  }
}
case class GeoDataCenter(dataType: String, value: String)
object GeoDataCenter{
  implicit val encodeGeoDataCenter: Encoder[GeoDataCenter] = new Encoder[GeoDataCenter] {
    final def apply(geoDataCenter: GeoDataCenter): Json = Json.obj(
      ("dataType", geoDataCenter.dataType.asJson),
      ("value", Json.fromString(geoDataCenter.value))
    )
  }

  implicit val decodeGeoDataCenter: Decoder[GeoDataCenter] = new Decoder[GeoDataCenter] {
    final def apply(c: HCursor): Decoder.Result[GeoDataCenter] =
      for {
        dataType <- c.downField("type").as[String]
        value <- c.downField("value").as[String]
      } yield {
        new GeoDataCenter(dataType, value)
      }
  }
}

object RestaurantDataType extends Enumeration {
  type RestaurantDataType = Value
  val bar, cafe, restaurant, buffet,
  canteen, fastfood, cafeteria, diner,
  gastronomy, undefined = Value

  implicit val encodeRestaurantDataType: Encoder[RestaurantDataType.Value] = Encoder.encodeEnumeration(RestaurantDataType)

  implicit val decodeRestaurantDataType: Decoder[RestaurantDataType.Value] = new Decoder[RestaurantDataType.Value] {
    final def apply(c: HCursor): Decoder.Result[RestaurantDataType.Value] =
      for {
        dataType <- c.as[String]
      } yield {
        decode(dataType)
      }
  }

  def decode(value: String): Value = value match {
    case "предприятие быстрого обслуживания"  => fastfood
    case "бар"                                => bar
    case "кафе"                               => cafe
    case "ресторан"                           => restaurant
    case "буфет"                              => buffet
    case "столовая"                           => canteen
    case "кафетерий"                          => cafeteria
    case "закусочная"                         => diner
    case "магазин (отдел кулинарии)"          => gastronomy
    case _                                    => undefined
  }

  def encode(value: RestaurantDataType.Value): String = value match {
    case   RestaurantDataType.fastfood    => "предприятие быстрого обслуживания"
    case   RestaurantDataType.bar         => "бар"
    case   RestaurantDataType.cafe        => "кафе"
    case   RestaurantDataType.restaurant  => "ресторан"
    case   RestaurantDataType.buffet      => "буфет"
    case   RestaurantDataType.canteen     => "столовая"
    case   RestaurantDataType.cafeteria   => "кафетерий"
    case   RestaurantDataType.diner       => "закусочная"
    case   RestaurantDataType.gastronomy  => "магазин (отдел кулинарии)"
    case   RestaurantDataType.undefined   => "undefined"
  }

  def encodeAsEng(value: RestaurantDataType.Value): String = value match {
    case   RestaurantDataType.fastfood    => "fastfood"
    case   RestaurantDataType.bar         => "bar"
    case   RestaurantDataType.cafe        => "cafe"
    case   RestaurantDataType.restaurant  => "restaurant"
    case   RestaurantDataType.buffet      => "buffet"
    case   RestaurantDataType.canteen     => "canteen"
    case   RestaurantDataType.cafeteria   => "cafeteria"
    case   RestaurantDataType.diner       => "diner"
    case   RestaurantDataType.gastronomy  => "gastronomy"
    case   RestaurantDataType.undefined   => "undefined"
  }

  def encodeToNum(value: RestaurantDataType.Value): Int = value match {
    case   RestaurantDataType.fastfood    => 1
    case   RestaurantDataType.bar         => 2
    case   RestaurantDataType.cafe        => 3
    case   RestaurantDataType.restaurant  => 4
    case   RestaurantDataType.buffet      => 5
    case   RestaurantDataType.canteen     => 6
    case   RestaurantDataType.cafeteria   => 7
    case   RestaurantDataType.diner       => 8
    case   RestaurantDataType.gastronomy  => 9
    case   RestaurantDataType.undefined   => 0
  }
}



object RestaurantData {

  val headerData: List[String]
    = List("id", "name", "globalId", "isNetObject", "operatingCompany", "typeObject", "admArea",
    "district", "address", "publicPhoneList", "seatsCount", "socialPrivileges", "longitudeWGS84", "latitudeWGS84", "geoData")

  implicit val encodeRestaurantData: Encoder[RestaurantData] = new Encoder[RestaurantData] {
    final def apply(restaurantData: RestaurantData): Json = Json.obj(
      ("id"               , Json.fromString(restaurantData.id)),
      ("name"             , Json.fromString(restaurantData.name)),
      ("global_id"        , Json.fromLong(restaurantData.global_id)),
      ("isNetObject"      , Json.fromBoolean(restaurantData.isNetObject)),
      ("operatingCompany" , Json.fromString(restaurantData.operatingCompany)),
      ("typeObject"       , restaurantData.typeObject.asJson),
      ("admArea"          , Json.fromString(restaurantData.admArea)),
      ("district"         , Json.fromString(restaurantData.district)),
      ("address"          , Json.fromString(restaurantData.address)),
      ("publicPhoneList"  , restaurantData.publicPhoneList.asJson),
      ("seatsCount"       , Json.fromInt(restaurantData.seatsCount)),
      ("socialPrivileges" , Json.fromBoolean(restaurantData.socialPrivileges)),
      ("longitudeWGS84"   , Json.fromDoubleOrString(restaurantData.longitudeWGS84)),
      ("latitudeWGS84"    , Json.fromDoubleOrString(restaurantData.latitudeWGS84)),
      ("geoData"          , restaurantData.geoData.asJson),
      ("geoDataCenter"    , restaurantData.geoDataCenter.asJson)
    )
  }

  implicit val decodeRestaurantData: Decoder[RestaurantData] = new Decoder[RestaurantData] {
    final def apply(c: HCursor): Decoder.Result[RestaurantData] = {
      for {
        id                <- c.downField("ID").as[String]
        name              <- c.downField("Name").as[String]
        global_id         <- c.downField("global_id").as[Long]
        isNetObject       <- decodeStringToBoolean(c.downField("IsNetObject").as[String], c)
        operatingCompany  <- c.downField("OperatingCompany").as[String]
        typeObject        <- c.downField("TypeObject").as[RestaurantDataType.Value]
        admArea           <- c.downField("AdmArea").as[String]
        district          <- c.downField("District").as[String]
        address           <- c.downField("Address").as[String]
        publicPhoneList   = c.downField("PublicPhone").values
                              .fold(List.empty[String])(
                                _.flatMap(
                                  _.hcursor.downField("PublicPhone").as[String].toOption).toList)

        seatsCount        <- c.downField("SeatsCount").as[Int]
        socialPrivileges  <- decodeStringToBoolean(c.downField("SocialPrivileges").as[String], c)
        longitudeWGS84    <- c.downField("Longitude_WGS84").as[Double]
        latitudeWGS84     <- c.downField("Latitude_WGS84").as[Double]
        geoData           <- c.downField("geoData").as[GeoData]
        geoDataCenter     <- c.downField("geodata_center").as[GeoDataCenter]
      } yield {
        new RestaurantData(id,
                           name,
                           global_id,
                           isNetObject,
                           operatingCompany,
                           typeObject,
                           admArea,
                           district,
                           address,
                           publicPhoneList,
                           seatsCount,
                           socialPrivileges,
                           longitudeWGS84,
                           latitudeWGS84,
                           geoData,
                           geoDataCenter)
      }
    }
  }

  private def decodeStringToBoolean(res: Result[String], c: HCursor): Result[Boolean] = {
    res match {
      case Left(e: DecodingFailure) => Left(e)
      case Right(value) => value match {
        case "да"  => Right(true)
        case "нет" => Right(false)
        case _     => Left(DecodingFailure("Unable to process String to Boolean variable", c.history))
      }
    }
  }
}

case class RestaurantData(id: String,
                          name: String,
                          global_id: Long,
                          isNetObject: Boolean,
                          operatingCompany: String,
                          typeObject: RestaurantDataType,
                          admArea: String,
                          district: String,
                          address: String,
                          publicPhoneList: List[String],
                          seatsCount: Int,
                          socialPrivileges: Boolean,
                          longitudeWGS84: Double,
                          latitudeWGS84: Double,
                          geoData: GeoData,
                          geoDataCenter: GeoDataCenter
                         )


