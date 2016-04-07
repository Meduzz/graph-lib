package se.kodiak.tools.graphs

import java.io.Serializable

package object model {
  sealed trait Node {
    def id:VerticeId
  }
  case class LazyNode(id:VerticeId) extends Node
  case class DataNode(id:VerticeId, data:String) extends Node
  case class HashNode(id:VerticeId, data:Map[String, String]) extends Node
  case class ListNode(id:VerticeId, data:Seq[String]) extends Node
  object Node {
    def apply(id:VerticeId):Node = LazyNode(id)
  }

  sealed trait Relation {
    def id: RelationId
    def relType: Relationship
  }
  case class LazyRelation(id:RelationId, relType:Relationship) extends Relation
  case class DataRelation(id:RelationId, relType:Relationship, data:String) extends Relation
  case class HashRelation(id:RelationId, relType:Relationship, data:Map[String, String]) extends Relation
  case class ListRelation(id:RelationId, relType:Relationship, data:Seq[String]) extends Relation
  object Relation {
    def apply(id:RelationId, relType:Relationship):Relation = LazyRelation(id, relType)
  }

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
