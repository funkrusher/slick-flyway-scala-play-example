package controllers

import akka.NotUsed
import akka.stream.scaladsl.{Concat, Source}
import akka.util.ByteString
import dto.models._
import dto.{AuthorRepository, BookRepository}
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import play.api.http.HttpEntity
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import slick.basic.DatabasePublisher
import slick.jdbc.JdbcProfile
import util.{QueryParamFilterModel, QueryParamModel, QueryParamSorterModel}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class TestController @Inject()(
                                dbConfigProvider: DatabaseConfigProvider,
                                cc: ControllerComponents,
                                authorRepository: AuthorRepository,
                                bookRepository: BookRepository) extends AbstractController(cc) with I18nSupport {
    // We want the JdbcProfile for this provider
    val dbConfig = dbConfigProvider.get[JdbcProfile]

    // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
    // The second one brings the Slick DSL into scope, which lets you define the table and other queries.

    import dbConfig._
    import profile.api._


    /**
     * Inserts 5000 entries with a batch-insert
     *
     * @return success-status
     */
    def test2: Action[AnyContent] = Action.async { implicit request =>

        println("GO1")

        // create 100000 authors
        var authors: Seq[AuthorModel] = Seq();
        (100 to 100000).foreach(i => {
            val author: AuthorModel = new AuthorModel(
                id = -1,
                first_name = Some("Max" + i),
                last_name = "Mustermann" + i,
                date_of_birth = None,
                year_of_birth = None,
                distinguished = None
            )
            authors = authors :+ author
        })
        println("GO2")

        // insert 100000 authors with batch-query
        val result = for {
            fetchResult <- authorRepository.insertMany(authors)
        } yield (fetchResult)

        result.map({
            list => {
                println("GO3")
                // create 100000 books
                var books: Seq[BookModel] = Seq();
                (100 to 100000).foreach(i => {
                    val book: BookModel = new BookModel(
                        id = -1,
                        author_id = i % 1000,
                        title = "Test" + i,
                        published_in = 1,
                        language_id = 1
                    )
                    books = books :+ book
                })

                println("GO4")
                // insert 100000 books with batch-query
                val result2 = for {
                    fetchResult <- bookRepository.insertMany(books)
                } yield (fetchResult)

                result2.map({
                    println("GO5")
                    list2 => {
                        // render html-view (transform the fetched jOOQ-Records to HTML)
                    }
                })
                Ok("OK")
            }
        })
    }


    /**
     * Tests transactional error with rollback
     *
     * @return success-status
     */
    def test5: Action[AnyContent] = Action.async { implicit request =>

        // see also: https://github.com/slick/slick/issues/1197
        // see also: https://stackoverflow.com/questions/38221021/transactional-method-in-scala-play-with-slick
        // -similar-to-spring-transactional

        val action = (for {
            // those statements are potentially in parallel with two parallel running connections,
            // but because the second depends on the first, the second is waiting until the first is finished.
            author <- authorRepository.fetchByIdAction(1)
            author2 <- authorRepository.fetchByIdAction(2)
            save1 <- authorRepository.saveAction(author.head.copy(id = 1000, first_name = Some("Gustave")))
            save2 <- authorRepository.saveAction(author.head.copy(id = 1001, first_name = Some("Lydia")))

        } yield (author, author2, save1, save2)).flatMap {
            case (author, author2, save1, save2) =>
                // lets force a rollback after all statements have been resolved
                // but while the transactionlly is still open.
                DBIO.failed(new Exception("force a rollback of the transaction with this ex-throw!"))
        }.transactionally;


        db.run(action).map({
            case (r) =>
                // render html-view
                Ok("OK")
        })
    }


    /**
     * Tests multi-joined fetch
     *
     * @return success-status
     */
    def test6: Action[AnyContent] = Action.async { implicit request =>

        val result = for {
            // those statements are potentially in parallel with two parallel running connections,
            // but because the second depends on the first, the second is waiting until the first is finished.
            books <- bookRepository.fetchAllByBookStoreNames(Seq("Orell Füssli"))
            authors <- authorRepository.fetchAllByBookIds(books.map(x => x.id))
        } yield (books, authors)

        result.map({
            case (books, authors) =>
                // the books and authors are of the jOOQ-Record type.
                // we can not serialize them easily as json.
                // just to test it out we push the jOOQ-Record into Scala Case-Classes which can be serialized
                // but it would be a bad idea to reintroduce handwritten classes in addition to the autogenerated ones.
                // TODO maybe consider using Jackson-Serizalizer Library instead of the OFormat

                val booksWithAuthors: Seq[BooksWithAuthorsModel] = books.map({
                    book =>
                        val foundAuthors = authors
                          .filter(author => author.id.equals(book.author_id))
                          .map(author => new AuthorModel(
                              author.id, author.first_name, author.last_name, author.date_of_birth,
                              author.year_of_birth, author.distinguished));
                        new BooksWithAuthorsModel(
                            id = book.id,
                            authors = foundAuthors);
                })
                Ok(Json.obj(
                    "booksWithAuthors" -> booksWithAuthors
                ))
        })
    }

    /**
     * Tests query-params
     *
     * @return success-status
     */
    def test7: Action[AnyContent] = Action.async { implicit request =>

        // TODO resolve queryParamModel from GET-Request Query-Params Deserialized
        val qParam = QueryParamModel(
            drop = Some(0),
            take = Some(10),
            sorter = Some(QueryParamSorterModel(
                sortOrder = "asc",
                sortName = "last_name"
            )),
            filters = Some(Seq(QueryParamFilterModel(
                tableName = "Author",
                filterName = "first_name",
                filterValue = "George",
                filterComparator = "like"
            )))
        )

        val result = for {
            filteredAuthorIds <- filterAuthorIds(qParam)
            count <- authorRepository.countAuthorApis(qParam, filteredAuthorIds)
            data <- authorRepository.fetchAuthorApis(qParam, filteredAuthorIds)
        } yield (count, data)

        result.map({
            case (count, data) =>
                Ok(Json.obj(
                    "count" -> count,
                    "data" -> data
                ))
        })
    }

    def test8: Action[AnyContent] = Action.async { implicit request =>
        // TODO resolve queryParamModel from GET-Request Query-Params Deserialized
        val qParam = QueryParamModel(
            drop = Some(0),
            take = Some(10),
            sorter = Some(QueryParamSorterModel(
                sortOrder = "asc",
                sortName = "last_name"
            )),
            filters = None
        )

        filterAuthorIds(qParam).map({
            case (filteredAuthorIds) =>
                val authorPublisher = authorRepository.getAuthorPublisher(qParam, filteredAuthorIds)
                val authorApisSource = getAuthorApiSource(authorPublisher)

                val csvHeader = Source.single(
                    ByteString(""""First Name","Last Name","Year","BookTitle"""" + "\n"))
                val csvLines: Source[ByteString, NotUsed] = authorApisSource.map(
                    authorApi => {
                        ByteString(
                            s""""${
                                authorApi.first_name.getOrElse("")
                            }","${
                                authorApi.last_name
                            }","${
                                authorApi.year_of_birth.getOrElse("")
                            }","${
                                authorApi.books match {
                                    case Some(books) => books.map(x => x.title).mkString("-")
                                    case None => ""
                                }
                            }"""".stripMargin + "\n")
                    }
                )
                val s = Source.combine(csvHeader, csvLines)(Concat[ByteString])

                Result(
                    header = ResponseHeader(OK, Map(CONTENT_DISPOSITION → s"attachment; filename=customers.csv")),
                    body = HttpEntity.Streamed(s, None, None))
        })
    }

    /**
     * Returns a list of all authorIds that correspond to the given filters list.
     *
     * @param qParam
     * @return
     */
    def filterAuthorIds(qParam: QueryParamModel): Future[Option[Seq[Int]]] = {
        if (qParam.filters.isEmpty) {
            return Future(None)
        }

        // we need to search all relevant tables here, that can have filter-conditions on it.
        val result = for {
            authorIds1 <- bookRepository.filterAuthorIds(qParam)
            authorIds2 <- authorRepository.filterAuthorIds(qParam)

        } yield (authorIds1, authorIds2)

        // return all authorIds that we have found in the different tables of the author-relations.
        result.flatMap(x => {
            Future(Some(x._1 ++ x._2))
        })
    }


    def getAuthorApiSource(publisher: DatabasePublisher[AuthorModel]): Source[AuthorApi, NotUsed] = {
        Source.fromPublisher(publisher)
          .log("test")
          .grouped(3000) // group our separate items into chunks of 3000 items
          .mapAsync(2)(items => {
              val itemIds = items.map(x => x.id)

              // spawn multiple futures, that can fetch data from different tables at the same time
              // for this chunk of 3000 items.
              val relations = for {
                  futureBookModels <- bookRepository.fetchByAuthors(itemIds)
              } yield (futureBookModels)

              // collect the results into our chunk of 3000 items
              relations.map((bookModels) => {
                  items.map(a => {
                      AuthorApi.fromModel(a).copy(
                          books = Some(bookModels.filter(_.author_id == a.id))
                      )
                  })
              })
          })
          .mapConcat(s => s.toList) // flatten the chunk of 3000 items to separate items again.
    }


}