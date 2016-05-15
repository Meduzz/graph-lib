package se.kodiak.tools.graphs

import scala.collection.immutable._
import model._

trait GraphSource {
  /**
    * Creates a new edge and adds it to the internal source.
 *
    * @param start the start vertice
    * @param relation the relation
    * @param end the end vertice.
    */
  def add(start:Node, relation:Relation, end:Node):Unit

  /**
    * Remove the Edge bound by this this relation.
    *
    * @param relation the relation
    */
  def remove(relation:Relation):Unit

  def relation(rel:Relationship):Relation
  def relation(rel:Relationship, data: String):DataRelation
  def relation(rel:Relationship, data:Map[String, String]):HashRelation
  def relationSeq(rel:Relationship):ListRelation

  def node():Node
  def node(data: String):DataNode
  def node(data:Map[String, String]):HashNode
  def nodeSeq():ListNode

  def loadDataNode(id:VerticeId):DataNode
  def loadHashNode(id:VerticeId):HashNode
  def loadSeqNode(id:VerticeId):ListNode
  def loadDataRelation(id:RelationId):DataRelation
  def loadHashRelation(id:RelationId):HashRelation
  def loadSeqRelation(id:RelationId):ListRelation

  def edges:Seq[Edge]
}
