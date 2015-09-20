package se.kodiak.tools.graphs

import java.io.Serializable

package object model {
  case class Node(id:VerticeId)
  case class Relation(id:RelationId, relType:Relationship)
  case class Edge(start:Node, relation:Relation, end:Node)

  type Relationship = String
  type VerticeId = Serializable
  type RelationId = Serializable

  trait Direction {
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
