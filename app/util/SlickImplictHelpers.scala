package util

import slick.lifted.{CanBeQueryCondition, Query, Rep}

import scala.collection.mutable.{LinkedHashMap, LinkedHashSet, Map => MutableMap}

object SlickImplictHelpers {

    // CONDITIONAL QUERY FILTER
    implicit class ConditionalQueryFilter[A, B, C[_]](q: Query[A, B, C]) {
        def filterOpt[D, T <: Rep[_] : CanBeQueryCondition](option: Option[D])
                                                           (f: (A, D) => T): Query[A, B, C] =
            option.map(d => q.filter(a => f(a, d))).getOrElse(q)

        def filterIf(p: Boolean)
                    (f: A => Rep[Boolean]): Query[A, B, C] =
            if (p) q.filter(f) else q
    }


    implicit class GroupByOrderedImplicitImpl[A](val t: Traversable[A]) extends AnyVal {
        def groupByOrdered[K](f: A => K): MutableMap[K, LinkedHashSet[A]] = {
            val map = LinkedHashMap[K, LinkedHashSet[A]]().withDefault(_ => LinkedHashSet[A]())
            for (i <- t) {
                val key = f(i)
                map(key) = map(key) + i
            }
            map
        }
    }

}
