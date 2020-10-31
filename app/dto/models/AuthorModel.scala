package dto.models

import java.sql.Date

import play.api.libs.json.Json

case class AuthorModel(
                        id: Int,
                        first_name: Option[String],
                        last_name: String,
                        date_of_birth: Option[Date],
                        year_of_birth: Option[Int],
                        distinguished: Option[Int]
                      ) {
    def toApi: AuthorApi = AuthorApi.fromModel(this)
}

object AuthorModel {
    implicit val fmt = Json.format[AuthorModel]
}


case class AuthorApi(
                      id: Int,
                      first_name: Option[String],
                      last_name: String,
                      date_of_birth: Option[Date],
                      year_of_birth: Option[Int],
                      distinguished: Option[Int],
                      books: Option[Seq[BookModel]]
                    )

object AuthorApi {
    implicit val fmt = Json.format[AuthorApi]

    def fromModel(model: AuthorModel): AuthorApi = {
        AuthorApi(
            id = model.id,
            first_name = model.first_name,
            last_name = model.last_name,
            date_of_birth = model.date_of_birth,
            year_of_birth = model.year_of_birth,
            distinguished = model.distinguished,
            books = None
        )
    }

}
