package dto.models

import play.api.libs.json.{Json, OFormat}

case class BookModel(
                      id: Int,
                      author_id: Int,
                      title: String,
                      published_in: Int,
                      language_id: Int
                    )

object BookModel {
    implicit val book_format: OFormat[BookModel] = Json.format[BookModel]
}