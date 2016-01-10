package se.kodiak.tools.graphs

import scala.collection.immutable._
import model._

object Datasource {
  val person1 = Node(1L)
  val person2 = Node(2L)
  val person3 = Node(3L)

  val gadget1 = Node(4L)
  val gadget2 = Node(5L)

  val knows1 = Relation(1L, Rel.KNOWS)
  val knows2 = Relation(2L, Rel.KNOWS)
  val knows3 = Relation(3L, Rel.KNOWS)
  val knows4 = Relation(4L, Rel.KNOWS)

  val owns1 = Relation(5L, Rel.OWNS)
  val owns2 = Relation(6L, Rel.OWNS)

  val result = Seq(Edge(person1, knows1, person2),
    Edge(person1, knows2, person3),
    Edge(person2, knows3, person1),
    Edge(person3, knows4, person1),
    Edge(person2, owns1, gadget1),
    Edge(person3, owns2, gadget2))

  val edges = Seq[(VerticeId, RelationId, Relationship, VerticeId)]((1L, 1L, Rel.KNOWS, 2L),
    (1L, 2L, Rel.KNOWS, 3L),
    (2L, 3L, Rel.KNOWS, 1L),
    (3L, 4L, Rel.KNOWS, 1L),
    (2L, 5L, Rel.OWNS, 4L),
    (3L, 6L, Rel.OWNS, 5L))

  object Rel {
    val KNOWS = "KNOWS"
    val OWNS = "OWNS"
  }

  def tupled():Seq[(VerticeId, RelationId, Relationship, VerticeId)] = edges
  def prebuilt():Seq[Edge] = result

  val nodeIndex = 5L
  val relationIndex = 6L
}
