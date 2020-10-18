package controllers

import dto.AuthorRepository
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import play.twirl.api.Html

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
class AuthorController @Inject()(
                                  cc: ControllerComponents,
                                  authorRepository: AuthorRepository) extends AbstractController(cc) with I18nSupport {

    /**
     * fetch all authors and show them as view
     *
     * @return authors-view
     */
    def fetchAllAuthors: Action[AnyContent] = Action.async { implicit request =>

        val t0 = System.currentTimeMillis()

        // fetch all authors from the database
        val result = for {
            all <- authorRepository.fetchAll()
        } yield all

        result.map({
            authors =>
                // render html-view (transform the fetched jOOQ-Records to HTML)
                val t1 = System.currentTimeMillis()
                println("Elapsed time: " + (t1 - t0) + "ms")

                // memory info
                val mb = 1024 * 1024
                val runtime = Runtime.getRuntime
                println("ALL RESULTS IN MB")
                println("** Used Memory:  " + (runtime.totalMemory - runtime.freeMemory) / mb)
                println("** Free Memory:  " + runtime.freeMemory / mb)
                println("** Total Memory: " + runtime.totalMemory / mb)
                println("** Max Memory:   " + runtime.maxMemory / mb)

                Ok(views.html.fetchAllAuthors(Html(authors.toString), AuthorAddForm.form, AuthorDeleteForm.form))
        })
    }


    /**
     * add a new author to the list
     *
     * @return author-data as json
     */
    def addAuthor: Action[AnyContent] = Action.async { implicit request =>
        // TODO
        Future(Ok("TODO"))
    }

    /**
     * delete a author from the list
     *
     * @return success-status
     */
    def deleteAuthor: Action[AnyContent] = Action.async { implicit request =>
        // TODO
        Future(Ok("TODO"))
    }
}
