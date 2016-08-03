package se.kodiak.tools.graphs

import se.kodiak.tools.graphs.edge.EdgeStorage
import se.kodiak.tools.graphs.model._

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
  def link(relation: Relation, end:Node)(implicit edges:EdgeStorage):Node = {
		edges.add(node, relation, end)
		node
	}

  def delete()(implicit graph: Graph, edges: EdgeStorage):Unit = graph.edges(node).foreach(edges.delete)
}

trait GraphRelationDSL {

  def relation:Relation

	def start()(implicit graph: Graph):Seq[Node] = graph.edges(relation).map(_.start)

	def end()(implicit graph: Graph):Seq[Node] = graph.edges(relation).map(_.end)

  def delete()(implicit graph: Graph, edges: EdgeStorage):Unit = graph.edges(relation).foreach(edges.delete)
}