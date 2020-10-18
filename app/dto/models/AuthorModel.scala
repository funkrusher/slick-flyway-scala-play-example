package dto.models

import java.sql.Date

import play.api.libs.json.Json

case class AuthorModel(
                        id: Int,
                        first_name: Option[String],
                        last_name: String,
                        date_of_birth: Option[Date],
                        year_of_birth: Option[Int],
                        distinguished: Option[Int])

object AuthorModel {
    implicit val authorFormat = Json.format[AuthorModel]
}
