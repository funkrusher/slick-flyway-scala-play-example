package dto

import java.sql.Date

import dto.models.AuthorModel
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
 * A repository for people.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 */
@Singleton
class AuthorRepository @Inject()(
                                  dbConfigProvider: DatabaseConfigProvider,
                                  bookRepository: BookRepository)
                                (implicit ec: ExecutionContext) {

    // We want the JdbcProfile for this provider
    // it must be defined as protected because we return DBIO as result.
    protected val dbConfig = dbConfigProvider.get[JdbcProfile]

    // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
    // The second one brings the Slick DSL into scope, which lets you define the table and other queries.

    import dbConfig._
    import profile.api._
    /**
     * The starting point for all queries on the people table.
     */
    private val author = TableQuery[AuthorTable]

    /**
     * returns a list of author-records
     *
     * @return list
     */
    def fetchAll(): Future[Seq[AuthorModel]] = db.run {
        author.result
    }

    /**
     * Fetches all authors that belong to the given book-ids
     *
     * @return list of authors
     */
    def fetchAllByBookIds(ids: Seq[Int]): Future[Seq[AuthorModel]] = {
        val innerJoin = for {
            (a, _) <- author join bookRepository.book on (_.id === _.author_id) filter (_._2.id.inSet(ids.toList))
        } yield (a)
        db.run(innerJoin.result)
    }

    def fetchById(id: Int): Future[Option[AuthorModel]] = {
        db.run(fetchByIdAction(id));
    }

    def fetchByIdAction(id: Int): DBIO[Option[AuthorModel]] = {
        author.filter(_.id === id).result.headOption
    }

    def save(input: AuthorModel): Future[AuthorModel] = {
        db.run(saveAction(input))
    }

    def saveAction(input: AuthorModel): DBIO[AuthorModel] = {
        ((author returning author.map(_.id) into ((u, insertId) => input.copy(id = insertId))) += input)
    }

    /**
     * Here we define the table. It will have a name of people
     */
    private class AuthorTable(tag: Tag) extends Table[AuthorModel](tag, "author") {

        /**
         * This is the tables default "projection".
         *
         * It defines how the columns are converted to and from the Person object.
         *
         * In this case, we are simply passing the id, name and page parameters to the Person case classes
         * apply and unapply methods.
         */
        def * = (id, first_name, last_name, date_of_birth, year_of_birth, distinguished) <> ((AuthorModel.apply _)
          .tupled, AuthorModel.unapply)

        /** The ID column, which is the primary key, and auto incremented */
        def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

        /** The first_name column */
        def first_name = column[Option[String]]("first_name")

        /** The last_name column */
        def last_name = column[String]("last_name")

        /** The date_of_birth column */
        def date_of_birth = column[Option[Date]]("date_of_birth")

        /** The year_of_birth column */
        def year_of_birth = column[Option[Int]]("year_of_birth")

        /** The distinguished column */
        def distinguished = column[Option[Int]]("distinguished")
    }


}
