package dto

import dto.models.BookModel
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import util.QueryParamModel

import scala.concurrent.{ExecutionContext, Future}

/**
 * A repository for people.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 */
@Singleton
class BookRepository @Inject()(
                                dbConfigProvider: DatabaseConfigProvider,
                                bookStoreRepository: BookStoreRepository,
                                bookToBookStoreRepository: BookToBookStoreRepository)
                              (implicit ec: ExecutionContext) {
    // We want the JdbcProfile for this provider
    val dbConfig = dbConfigProvider.get[JdbcProfile]

    // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
    // The second one brings the Slick DSL into scope, which lets you define the table and other queries.

    import dbConfig._
    import profile.api._

    /**
     * The starting point for all queries on the book table.
     */
    val book = TableQuery[BookTable]

    /**
     * returns a list of book-records
     *
     * @return list
     */
    def fetchAll(): Future[Seq[BookModel]] = db.run {
        book.result
    }

    /**
     * Fetches all books that belong to the given book-store names
     *
     * @return list of books
     */
    def fetchAllByBookStoreNames(names: Seq[String]): Future[Seq[BookModel]] = {
        val innerJoin = for {
            ((b, x), y) <- book join bookToBookStoreRepository.book_to_book_store on (_.id === _.book_id) join
              bookStoreRepository.book_store on (_._2.name === _.name) filter (_._2.name.inSet(
                names.toList))
        } yield (b)
        db.run(innerJoin.result)
    }

    /**
     * Returns the authorIds filtered by the given filters.
     *
     * @param qParam
     * @return authorIds for filters.
     */
    def filterAuthorIds(
                         qParam: QueryParamModel): Future[Seq[Int]] = {

        val query = for {
            a <- book.filter(x => {
                // OR-condition filter for all relevant filters that have to be done on this table.
                // TODO we may need to check other kinds of filterTypes later on.

                val default = LiteralColumn(1) === LiteralColumn(1)

                val condition1: Option[Rep[Option[Boolean]]] = qParam.findFilterByName("Book", "title") match {
                    case Some(filter) => Some(x.title.like(filter.filterValue))
                    case _ => None
                }
                // bundle all found filters with OR-criteria
                val res: Rep[Option[Boolean]] = List(
                    condition1
                ).collect({ case Some(it) => it }).reduceLeftOption((x, y) => x || y).getOrElse(default)
                res
            })

        } yield (a.id)

        db.run(query.result);
    }


    /**
     * Here we define the table. It will have a name of people
     */
    class BookTable(tag: Tag) extends Table[BookModel](tag, "book") {

        /**
         * This is the tables default "projection".
         *
         * It defines how the columns are converted to and from the Person object.
         *
         * In this case, we are simply passing the id, name and page parameters to the Person case classes
         * apply and unapply methods.
         */
        def * = (id, author_id, title, published_in, language_id) <> ((BookModel.apply _).tupled, BookModel.unapply)

        /** The ID column, which is the primary key, and auto incremented */
        def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

        /** The author_id column */
        def author_id = column[Int]("author_id")

        /** The title column */
        def title = column[String]("title")

        /** The published_in column */
        def published_in = column[Int]("published_in")

        /** The language_id column */
        def language_id = column[Int]("language_id")
    }

}
