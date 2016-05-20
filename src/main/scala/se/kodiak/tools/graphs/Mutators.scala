package se.kodiak.tools.graphs

import se.kodiak.tools.graphs.model._

import scala.concurrent.{ExecutionContext, Future}

trait Mutators {

  def edgeDelegate:EdgeDelegate
	def nodeDelegate:NodeDelegate
	def relationDelegate:RelationDelegate

  def add(start:Node, relation: Relation, end:Node)(implicit ec:ExecutionContext):Future[Edge] = edgeDelegate.add(start, relation, end)
  def remove(edge: Edge)(implicit ec:ExecutionContext):Future[Unit] = edgeDelegate.remove(edge)

  def relation(rel:Relationship):Future[Relation] = relationDelegate.save(LazyRelation(relationDelegate.relationIdGenerator.generate(), rel))
  def relation(rel: Relationship, data: String):Future[DataRelation] = relationDelegate.relation(rel, data)
  def relation(rel:Relationship, data:Map[String, String]):Future[HashRelation] = relationDelegate.relation(rel, data)
  def relation(rel:Relationship, data:Seq[String]):Future[ListRelation] = relationDelegate.relation(rel, data)

  def node():Future[Node] = nodeDelegate.save(LazyNode(nodeDelegate.nodeIdGenerator.generate()))
  def node(data: String):Future[DataNode] = nodeDelegate.node(data)
  def node(data:Map[String, String]):Future[HashNode] = nodeDelegate.node(data)
  def node(data:Seq[String]):Future[ListNode] = nodeDelegate.node(data)

  def loadDataNode(id:VerticeId):Future[DataNode] = nodeDelegate.loadDataNode(id)
  def loadHashNode(id:VerticeId):Future[HashNode] = nodeDelegate.loadHashNode(id)
  def loadListNode(id:VerticeId):Future[ListNode] = nodeDelegate.loadListNode(id)

  def loadDataRelation(id:RelationId):Future[DataRelation] = relationDelegate.loadDataRelation(id)
  def loadHashRelation(id:RelationId):Future[HashRelation] = relationDelegate.loadHashRelation(id)
  def loadListRelation(id:RelationId):Future[ListRelation] = relationDelegate.loadListRelation(id)
}
