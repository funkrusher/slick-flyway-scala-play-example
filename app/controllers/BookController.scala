package controllers

import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}

@Singleton
class BookController @Inject()(
                                cc: ControllerComponents) extends AbstractController(cc) with I18nSupport {

}
