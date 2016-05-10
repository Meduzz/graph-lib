package se.kodiak.tools.graphs

import se.kodiak.tools.graphs.model._

trait Mutators {

  def source:GraphSource

  /**
    * Creates a new edge and adds it to the internal source.
    * @param start the start vertice
    * @param relation the relation
    * @param end the end vertice.
    */
  def add(start:Node, relation: Relation, end:Node):Unit = source.add(start, relation, end)

  /**
    * Remove the Edge bound by this this relation.
    * @param relation the relation
    */
  def remove(relation: Relation):Unit = source.remove(relation)

  def relation(rel:Relationship):Relation = source.relation(rel)
  def relation(rel: Relationship, data: String):DataRelation = source.relation(rel, data)

  def node():Node = source.node()
  def node(data: String):DataNode = source.node(data)

  def loadDataNode(id:VerticeId):DataNode = source.loadDataNode(id)
  def loadHashNode(id:VerticeId):HashNode = source.loadHashNode(id)
  def loadListNode(id:VerticeId):ListNode = source.loadSeqNode(id)

  def loadDataRelation(id:RelationId):DataRelation = source.loadDataRelation(id)
  def loadHashRelation(id:RelationId):HashRelation = source.loadHashRelation(id)
  def loadListRelation(id:RelationId):ListRelation = source.loadSeqRelation(id)
}
