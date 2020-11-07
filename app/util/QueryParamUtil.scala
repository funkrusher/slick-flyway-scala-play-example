package util


import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.ast.Ordering
import slick.ast.Ordering.Direction
import slick.jdbc.JdbcProfile
import slick.lifted.ColumnOrdered


/**
 * Helper-trait for dynamic field-selection of models (use this trait in your model)
 */
trait QueryParamFieldSelector {

    import slick.lifted.Rep

    // The runtime map between string names and table columns
    // the field-names must be exactly as given by the frontend
    // fields that should not be available for query from the frontend should be left out.
    val queryParamFields: Map[String, Rep[_]]
}


/**
 * Helper-class to append dynamic query-params from our [[QueryParamModel]] to a given slick-query.
 * Note: we need the "profile.api" from the [[HasDatabaseConfigProvider]] to have the "===" comparators available here.
 *
 * @param dbConfigProvider dbConfigProvider
 */
@Singleton
class QueryParamUtil @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] {

    import profile.api._

    /**
     * make a SELECT-Query for the given query-param-model
     *
     * @param query           slick-query
     * @param queryParamModel queryParamModel
     * @return slick-query with applied query-params
     */
    def resolveQueryParam[A <: QueryParamFieldSelector, B, C[_]](
                                                                  query: Query[A, B, C],
                                                                  queryParamModel: QueryParamModel): Query[A, B, C] = {

        var extendedQuery = resolveFilters(query, queryParamModel);

        // sorters
        if (queryParamModel.sorter.isDefined) {
            var ordering: Direction = Ordering.Asc;
            if (queryParamModel.sorter.get.sortOrder.equals("desc")) {
                ordering = Ordering.Desc;
            }
            val sortOrderRep: Rep[_] => ColumnOrdered[_] = ColumnOrdered(_, Ordering(ordering))
            val sortColumnRep: A => Rep[_] = _.queryParamFields(queryParamModel.sorter.get.sortName)
            extendedQuery = extendedQuery.sortBy(sortColumnRep)(sortOrderRep)
        }
        // pagination (drop, take)
        if (queryParamModel.drop.isDefined)
            extendedQuery = extendedQuery.drop(queryParamModel.drop.get);
        if (queryParamModel.take.isDefined)
            extendedQuery = extendedQuery.take(queryParamModel.take.get);

        extendedQuery;
    }

    /**
     * make a COUNT-Query for the given query-param-model
     *
     * @param query           slick-query
     * @param queryParamModel queryParamModel
     * @return count-statement
     */
    def resolveQueryParamCount[A <: QueryParamFieldSelector, B, C[_]](
                                                                       query: Query[A, B, C],
                                                                       queryParamModel: QueryParamModel): Rep[Int] = {
        resolveFilters(query, queryParamModel).size;
    }

    /**
     * resolve all given filters from the query-param-model to the given query.
     *
     * @param query           query
     * @param queryParamModel queryParamModel
     * @return query with filters appended.
     */
    private def resolveFilters[A <: QueryParamFieldSelector, B, C[_]](
                                                                       query: Query[A, B, C],
                                                                       queryParamModel: QueryParamModel): Query[A, B,
      C] = {
        var extendedQuery = query;
        if (queryParamModel.filters.isDefined) {
            queryParamModel.filters.get.foreach(filter => {

                if (filter.filterComparator.equals("like")) {
                    val compare: A => Rep[Boolean] = _.queryParamFields(
                        filter.filterName).asInstanceOf[Rep[String]] like filter.filterValue;
                    extendedQuery = extendedQuery.filter(compare);
                } else if (filter.filterComparator.equals("in")) {
                    val compare: A => Rep[Boolean] = _.queryParamFields(
                        filter.filterName).asInstanceOf[Rep[String]] inSet filter.filterValue.split(",").toList;
                    extendedQuery = extendedQuery.filter(compare);
                }
            })
        }
        extendedQuery
    }


}
