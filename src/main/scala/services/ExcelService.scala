package services

import akka.stream.scaladsl.Flow
import com.sun.tools.javac.tree.TreeInfo.args
import models.{RestaurantData, RestaurantDataType}
import spoiwo.model.Cell.Empty.style
import spoiwo.model.{Row, Sheet}
import spoiwo.model._
import spoiwo.model.enums._
import spoiwo.natures.xlsx.Model2XlsxConversions.XlsxSheet

import java.time.LocalDate
import spoiwo.natures.xlsx.Model2XlsxConversions.XlsxSheet

import java.time.LocalDate

class ExcelService {

  var sheet: Sheet = _

  val headerStyle: CellStyle =
    CellStyle(
      fillPattern = CellFill.Solid,
      fillForegroundColor = Color.AquaMarine,
      fillBackgroundColor = Color.AquaMarine,
      font = Font(bold = true)
    )

  def prepareData() = {
    val sheet = Sheet(name = "Mos-ru-dataset").withRows(
      Row(style = headerStyle).withCellValues(RestaurantData.headerData)
    )
    this.sheet = sheet
    sheet
  }

  def fillColumn(restaurantData: RestaurantData) = {
    val row = Row().withCellValues(
      restaurantData.id,
      restaurantData.name,
      restaurantData.global_id,
      restaurantData.isNetObject,
      restaurantData.operatingCompany,
      RestaurantDataType.encodeAsEng(restaurantData.typeObject),
      restaurantData.admArea,
      restaurantData.district,
      restaurantData.address,
      restaurantData.publicPhoneList.map(s => s.replaceAll("(\\s*|\\(|\\)|\\-)", "")).mkString(","),
      restaurantData.seatsCount,
      restaurantData.socialPrivileges,
      restaurantData.longitudeWGS84,
      restaurantData.latitudeWGS84,
      restaurantData.geoData.toString
    )

    this.sheet = this.sheet.addRow(row)
    restaurantData
  }

  def saveAndClose(sheet: Sheet, fileName: String) =  sheet.saveAsXlsx(fileName)

}
