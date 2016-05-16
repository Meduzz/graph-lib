package se.kodiak.tools.graphs

import model._

object Implicits {
  implicit def nodeImplicits(node:Node):ImplicitNode = new ImplicitNode(node)
  implicit def relationImplicits(relation:Relation):ImplicitRelation = new ImplicitRelation(relation)

  class ImplicitNode(val node:Node) extends GraphNodeDSL {
  }

  class ImplicitRelation(val relation:Relation) extends GraphRelationDSL {
  }
}

trait GraphNodeDSL {

  def node:Node

  // in/outbound node through relationship
  def endNodesOfRelationType(rel:Relationship)(implicit graph:Graph):Seq[Node] = graph.outbound(node, rel)
  def startNodesOfRelationType(rel:Relationship)(implicit graph:Graph):Seq[Node] = graph.inbound(node, rel)

  // in/outbound with/out relationship filter.
  def outgoingRelations()(implicit graph:Graph):Seq[(Relation, Node)] = graph.outbound(node)
  def incomingRelations()(implicit graph:Graph):Seq[(Node, Relation)] = graph.inbound(node)
  def outgoingRelationsOfType(rel:Relationship)(implicit graph:Graph):Seq[(Relation, Node)] = graph.outboundWithRelation(node, rel)
  def incomingRelationsOfType(rel:Relationship)(implicit graph:Graph):Seq[(Node, Relation)] = graph.inboundWithRelation(node, rel)

  // degrees
  def degrees(direction:Direction = Direction.BOTH)(implicit graph:Graph):Int = graph.degrees(node, direction)
  def degreesOfRelationType(relation:Relationship, direction: Direction)(implicit graph:Graph):Int = graph.degrees(node, relation, direction)

  // relations without filter
  def findRelationsToNode(other:Node, direction: Direction)(implicit graph:Graph):Seq[Relation] = graph.relation(node, other, direction)

  // relations with filter
  def findRelationsToNode(other:Node, rel:Relationship, direction: Direction)(implicit graph:Graph):Seq[Relation] = graph.relation(node, other, rel, direction)

  // create/remove relationships
  def link(relation: Relation, end:Node)(implicit graph:Graph with Mutators):Unit = graph.add(node, relation, end)
  def link(rel:Relationship, end:Node)(implicit graph:Graph with Mutators):Unit = graph.add(node, graph.relation(rel), end)

	def delete(implicit graph:Graph with Mutators):Unit = {
		graph.edges(node).foreach(graph.remove)
		graph.nodeDelegate.delete(node)
	}

	def save(implicit graph: Graph with Mutators):Node = graph.nodeDelegate.save(node)

  // load node data
  def asData(implicit graph:Graph with Mutators):DataNode = graph.loadDataNode(node.id)
  def asSeq(implicit graph:Graph with Mutators):ListNode = graph.loadListNode(node.id)
	def asHash(implicit graph:Graph with Mutators):HashNode = graph.loadHashNode(node.id)
}

trait GraphRelationDSL {

  def relation:Relation

  // remove the edge bound by this relation.
  def delete(implicit graph:Graph with Mutators):Unit = {
		graph.edges(relation).foreach(graph.remove)
    graph.relationDelegate.delete(relation)
  }

	def save(implicit graph: Graph with Mutators):Relation = graph.relationDelegate.save(relation)

  // load relation data.
  def asData(implicit graph:Graph with Mutators):DataRelation = graph.loadDataRelation(relation.id)
	def asSeq(implicit graph:Graph with Mutators):ListRelation = graph.loadListRelation(relation.id)
	def asHash(implicit graph:Graph with Mutators):HashRelation = graph.loadHashRelation(relation.id)
}