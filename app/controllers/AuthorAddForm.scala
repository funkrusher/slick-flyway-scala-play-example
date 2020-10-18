package controllers

import play.api.data.Form
import play.api.data.Forms.{mapping, _}

case class AuthorAddForm(
                          first_name: String,
                          last_name: String)

object AuthorAddForm {
    val form: Form[AuthorAddForm] = Form(
        mapping(
            "first_name" -> text,
            "last_name" -> text,
        )(AuthorAddForm.apply)(AuthorAddForm.unapply)
    )
}
