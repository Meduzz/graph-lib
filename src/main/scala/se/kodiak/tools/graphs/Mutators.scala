package se.kodiak.tools.graphs

import se.kodiak.tools.graphs.model._

trait Mutators {

  def edgeDelegate:EdgeDelegate
	def nodeDelegate:NodeDelegate
	def relationDelegate:RelationDelegate

  def add(start:Node, relation: Relation, end:Node):Unit = edgeDelegate.add(start, relation, end)
  def remove(edge: Edge):Unit = edgeDelegate.remove(edge)

  def relation(rel:Relationship):Relation = relationDelegate.save(LazyRelation(relationDelegate.relationIdGenerator.generate(), rel))
  def relation(rel: Relationship, data: String):DataRelation = relationDelegate.relation(rel, data)
  def relation(rel:Relationship, data:Map[String, String]):HashRelation = relationDelegate.relation(rel, data)
  def relation(rel:Relationship, data:Seq[String]):ListRelation = relationDelegate.relation(rel, data)

  def node():Node = nodeDelegate.save(LazyNode(nodeDelegate.nodeIdGenerator.generate()))
  def node(data: String):DataNode = nodeDelegate.node(data)
  def node(data:Map[String, String]):HashNode = nodeDelegate.node(data)
  def node(data:Seq[String]):ListNode = nodeDelegate.node(data)

  def loadDataNode(id:VerticeId):DataNode = nodeDelegate.loadDataNode(id)
  def loadHashNode(id:VerticeId):HashNode = nodeDelegate.loadHashNode(id)
  def loadListNode(id:VerticeId):ListNode = nodeDelegate.loadListNode(id)

  def loadDataRelation(id:RelationId):DataRelation = relationDelegate.loadDataRelation(id)
  def loadHashRelation(id:RelationId):HashRelation = relationDelegate.loadHashRelation(id)
  def loadListRelation(id:RelationId):ListRelation = relationDelegate.loadListRelation(id)
}
