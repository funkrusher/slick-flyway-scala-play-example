package dto

import java.sql.Date

import dto.models.{AuthorApi, AuthorModel}
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import scalaz.{-\/, \/, \/-}
import slick.ast.Ordering
import slick.ast.Ordering.Direction
import slick.jdbc.JdbcProfile
import slick.lifted.ColumnOrdered
import util.QueryParamModel
import util.SlickImplictHelpers._

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
     * Returns the authorIds filtered by the given filters.
     *
     * @param qParam
     * @return authorIds for filters.
     */
    def filterAuthorIds(
                         qParam: QueryParamModel): Future[Seq[Int]] = {

        val query = for {
            a <- author.filter(x => {
                // OR-condition filter for all relevant filters that have to be done on this table.
                // TODO we may need to check other kinds of filterTypes later on.

                val default = LiteralColumn(1) === LiteralColumn(1)

                val condition1: Option[Rep[Option[Boolean]]] = qParam.findFilterByName("Author", "first_name") match {
                    case Some(filter) => Some(x.first_name.like(filter.filterValue))
                    case _ => None
                }
                val condition2: Option[Rep[Option[Boolean]]] = qParam.findFilterByName("Author", "last_name") match {
                    case Some(filter) => Some(x.last_name.like(filter.filterValue))
                    case _ => None
                }
                // bundle all found filters with OR-criteria
                val res: Rep[Option[Boolean]] = List(
                    condition1,
                    condition2,
                ).collect({ case Some(it) => it }).reduceLeftOption((x, y) => x || y).getOrElse(default)
                res
            })

        } yield (a.id)

        db.run(query.result);
    }


    /**
     * Returns the total count for the pagination
     *
     * @param qParam            qParam
     * @param filteredAuthorIds filteredAuthorIds
     * @return count
     */
    def doPaginationCount(
                           qParam: QueryParamModel,
                           filteredAuthorIds: Option[Seq[Int]]): Future[Int] = {
        doPaginationExtended(qParam, filteredAuthorIds, isCount = true) match {
            case \/-(result) => result
            case _ => Future(0)
        }
    }

    /**
     * Returns all authors in a specific pagination-window (starting with offset)
     *
     * @param qParam            qParam
     * @param filteredAuthorIds filteredAuthorIds
     * @return authors of pagination-window
     */
    def doPaginationExtended(
                              qParam: QueryParamModel,
                              filteredAuthorIds: Option[Seq[Int]]): Future[Seq[AuthorApi]] = {
        doPaginationExtended(qParam, filteredAuthorIds, isCount = false) match {
            case -\/(result) => result
            case _ => Future(Seq())
        }
    }

    /**
     * Returns all authors in a specific pagination-window (starting with offset)
     *
     * @param qParam            qParam
     * @param filteredAuthorIds filterRegulationIds
     * @return authors or count
     */
    private def doPaginationExtended(
                                      qParam: QueryParamModel,
                                      filteredAuthorIds: Option[Seq[Int]],
                                      isCount: Boolean): Future[Seq[AuthorApi]] \/ Future[Int] = {

        // query-params:
        // - we filter and sort on the author-table
        // - we paginate on the join of the related tables (inner-join)
        // - no 1:n joins are allowed here, because we must calculate a count, which would be wrong if 1:n joins are
        // made.
        var query = for {
            a <- (
              author
                .filterOpt(filteredAuthorIds)((row, value: Seq[Int]) => row.id.inSet(value))
              )

        } yield (a)

        // we need to sort our intermediate result now, before starting with pagination,
        // because the pagination must be done on the sorted result ...
        if (qParam.sorter.isDefined) {
            var ordering: Direction = Ordering.Asc;
            if (qParam.sorter.get.sortOrder.equals("desc")) {
                ordering = Ordering.Desc;
            }
            val sortOrderRep: Rep[_] => ColumnOrdered[Any] = ColumnOrdered(_, Ordering(ordering))

            if (qParam.sorter.get.sortName == "first_name") {
                query = query.sortBy(_.first_name)(sortOrderRep);
            } else if (qParam.sorter.get.sortName == "last_name") {
                query = query.sortBy(_.last_name)(sortOrderRep);
            }
        }

        if (isCount) {
            // return the count (no 1:n joins are allowed, because we need to count)
            \/-(db.run(query.size.result))

        } else {
            // paginate the filtered and sorted result now.
            query = query.drop(qParam.drop.getOrElse(-1)).take(qParam.take.getOrElse(-1))

            // after we have done the pagination we can join additional tables into the result.
            val queryExtended = for {
                (query, books) <- (query
                  joinLeft bookRepository.book on (_.id === _.author_id)
                  )
            } yield (query, books)

            // note: i must use our own custom "groupByOrdered" function, because scala "groupBy"
            // would loose our already sorted insertion-order otherwise.
            -\/(db.run(queryExtended.result).map(_.groupByOrdered(_._1).map {
                case (author, composedResult) =>
                    author.toApi.copy(
                        books = Option(composedResult.groupBy(_._2).flatMap(_._1).toSeq)
                    )
            }.toSeq))
        }
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
