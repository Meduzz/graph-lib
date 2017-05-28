package se.kodiak.tools.graphs

package object model {
  sealed trait Node {
    def id:VerticeId
  }
  case class LazyNode(id:VerticeId) extends Node

  object Node {
    def apply(id:VerticeId):Node = LazyNode(id)
  }

  sealed trait Relation {
    def relType: Relationship
  }
  case class LazyRelation(relType:Relationship) extends Relation

  object Relation {
    def apply(relType:Relationship):Relation = LazyRelation(relType)
  }

  case class Edge(start:Node, relation:Relation, end:Node)

  type Relationship = String
  type VerticeId = String
  type RelationId = String

  sealed trait Direction {
    def name:String
  }
  object Direction {
    val INBOUND = apply("INBOUND")
    val OUTBOUND = apply("OUTBOUND")
    val BOTH = apply("BOTH")

    def apply(str:String):Direction = new Direction {
      override def name: String = str
    }
  }
}
