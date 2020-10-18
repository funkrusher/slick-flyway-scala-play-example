package dto.models

import play.api.libs.json.{Json, OFormat}

case class BookStoreModel(
                           name: String
                         )

object BookStoreModel {
    implicit val book_store_model_format: OFormat[BookStoreModel] = Json.format[BookStoreModel]
}