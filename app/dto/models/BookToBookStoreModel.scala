package dto.models

import play.api.libs.json.{Json, OFormat}

case class BookToBookStoreModel(
                                 name: String,
                                 book_id: Int,
                                 stock: Int
                               )

object BookToBookStoreModel {
    implicit val bookToBookStore_format: OFormat[BookToBookStoreModel] = Json.format[BookToBookStoreModel]
}
