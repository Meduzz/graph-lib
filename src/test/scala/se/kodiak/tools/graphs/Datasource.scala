package se.kodiak.tools.graphs

import scala.collection.immutable._
import model._

object Datasource {
  val person1 = Node("1")
  val person2 = Node("2")
  val person3 = Node("3")

  val gadget1 = Node("4")
  val gadget2 = Node("5")

  val knows1 = Relation("1", Rel.KNOWS)
  val knows2 = Relation("2", Rel.KNOWS)
  val knows3 = Relation("3", Rel.KNOWS)
  val knows4 = Relation("4", Rel.KNOWS)

  val owns1 = Relation("5", Rel.OWNS)
  val owns2 = Relation("6", Rel.OWNS)

  val result = Seq(Edge(person1, knows1, person2),
    Edge(person1, knows2, person3),
    Edge(person2, knows3, person1),
    Edge(person3, knows4, person1),
    Edge(person2, owns1, gadget1),
    Edge(person3, owns2, gadget2))

  val edges = Seq[(VerticeId, RelationId, Relationship, VerticeId)](("1", "1", Rel.KNOWS, "2"),
    ("1", "2", Rel.KNOWS, "3"),
    ("2", "3", Rel.KNOWS, "1"),
    ("3", "4", Rel.KNOWS, "1"),
    ("2", "5", Rel.OWNS, "4"),
    ("3", "6", Rel.OWNS, "5"))

  object Rel {
    val KNOWS = "KNOWS"
    val OWNS = "OWNS"
  }

  def tupled():Seq[(VerticeId, RelationId, Relationship, VerticeId)] = edges
  def prebuilt():Seq[Edge] = result

  def nodes():Seq[Node] = Seq(person1, person2, person3, gadget1, gadget2)
	def relations():Seq[Relation] = Seq(knows1, knows2, knows3, knows4, owns1, owns2)
}
