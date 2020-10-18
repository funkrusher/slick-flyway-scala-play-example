package controllers

import play.api.data.Form
import play.api.data.Forms.{mapping, _}


case class BookAddForm(
                        author_id: Int,
                        title: String,
                        published_in: Int,
                        language_id: Int)

object BookAddForm {
    val form: Form[BookAddForm] = Form(
        mapping(
            "author_id" -> number,
            "title" -> text,
            "published_in" -> number,
            "language_id" -> number
        )(BookAddForm.apply)(BookAddForm.unapply)
    )
}