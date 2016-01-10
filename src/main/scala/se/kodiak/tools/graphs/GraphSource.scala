package se.kodiak.tools.graphs

import scala.collection.immutable._
import model._

trait GraphSource {
  /**
    * Creates a new edge and adds it to the internal source.
    * @param start the start vertice
    * @param relation the relation
    * @param end the end vertice.
    */
  def add(start:Node, relation:Relation, end:Node):Unit

  /**
    * Remove the Edge bound by this this relation.
    * @param relation the relation
    */
  def remove(relation:Relation):Unit

  def relation(rel:Relationship):Relation
  def relation(rel:Relationship, data:Any):DataRelation

  def node():Node
  def node(data:Any):DataNode

  def loadNode(id:VerticeId):DataNode
  def loadRelation(id:RelationId):DataRelation

  def edges:Seq[Edge]
}
