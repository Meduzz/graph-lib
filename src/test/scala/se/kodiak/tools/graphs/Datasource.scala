package se.kodiak.tools.graphs

import scala.collection.immutable._
import model._

object Datasource {
  val person1 = Node("1")
  val person2 = Node("2")
  val person3 = Node("3")

  val gadget1 = Node("4")
  val gadget2 = Node("5")

  val knows = Relation(Rel.KNOWS)

  val owns = Relation(Rel.OWNS)

  val result = Seq(Edge(person1, knows, person2),
    Edge(person1, knows, person3),
    Edge(person2, knows, person1),
    Edge(person3, knows, person1),
    Edge(person2, owns, gadget1),
    Edge(person3, owns, gadget2))

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
	def relations():Seq[Relation] = Seq(knows, owns)
}
