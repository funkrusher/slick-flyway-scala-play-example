package controllers

import play.api.data.Form
import play.api.data.Forms.{mapping, _}


case class BookDeleteForm(
                           id: Int)

object BookDeleteForm {
    val form: Form[BookDeleteForm] = Form(
        mapping(
            "id" -> number
        )(BookDeleteForm.apply)(BookDeleteForm.unapply)
    )
}