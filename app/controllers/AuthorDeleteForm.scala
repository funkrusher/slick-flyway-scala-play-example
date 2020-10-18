package controllers

import play.api.data.Form
import play.api.data.Forms.{mapping, _}


case class AuthorDeleteForm(
                             id: Int)

object AuthorDeleteForm {
    val form: Form[AuthorDeleteForm] = Form(
        mapping(
            "id" -> number
        )(AuthorDeleteForm.apply)(AuthorDeleteForm.unapply)
    )
}