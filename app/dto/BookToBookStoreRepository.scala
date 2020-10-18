package dto

import dto.models.BookToBookStoreModel
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

/**
 * A repository for people.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 */
@Singleton
class BookToBookStoreRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)
                                         (implicit ec: ExecutionContext) {
    // We want the JdbcProfile for this provider
    val dbConfig = dbConfigProvider.get[JdbcProfile]

    // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
    // The second one brings the Slick DSL into scope, which lets you define the table and other queries.

    import dbConfig._
    import profile.api._

    /**
     * The starting point for all queries on the book_to_book_store table.
     */
    val book_to_book_store = TableQuery[BookToBookStoreTable]

    /**
     * Here we define the table. It will have a name of people
     */
    class BookToBookStoreTable(tag: Tag) extends Table[BookToBookStoreModel](tag, "book_to_book_store") {

        /**
         * This is the tables default "projection".
         *
         * It defines how the columns are converted to and from the Person object.
         *
         * In this case, we are simply passing the id, name and page parameters to the Person case classes
         * apply and unapply methods.
         */
        def * = (name, book_id, stock) <> ((BookToBookStoreModel.apply _).tupled, BookToBookStoreModel.unapply)

        /** The name column, which is the primary key */
        def name = column[String]("name", O.PrimaryKey)

        /** The book_id column */
        def book_id = column[Int]("book_id")

        /** The stock column */
        def stock = column[Int]("stock")
    }

}
