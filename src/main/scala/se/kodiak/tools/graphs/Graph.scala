package se.kodiak.tools.graphs

import se.kodiak.tools.graphs.edge.EdgeStorage
import se.kodiak.tools.graphs.model._

object Graph {
  def apply(edges:EdgeStorage):Graph = new GraphImpl(edges)
}

trait Graph {
  /**
   * Count the total number of relations of this node.
 *
   * @param node the node.
   * @param direction specifies the direction to look
   * @return returns the count.
   */
  def degrees(node:Node, direction:Direction):Int

  /**
   * Count the total number of relations of the supplied type of this node.
 *
   * @param node the node.
   * @param rel the relationship type.
   * @param direction specifies the direction to look
   * @return returns the count.
   */
  def degrees(node:Node, rel:Relationship, direction:Direction):Int

  /**
   * Get all outbound relations and their end node of this node..
 *
   * @param startNode the source node.
   * @return returns a collection of tuples of the relation and the end node.
   */
  def outbound(startNode:Node):Seq[(Relation, Node)]

  /**
   * Get all outbound end node of this source node with the given relationship type.
 *
   * @param startNode the source node.
   * @param rel the relationship type.
   * @return returns a collection of end vertices.
   */
  def outbound(startNode:Node, rel:Relationship):Seq[Node]

  /**
    * Get all outbound relation + node that match the relationship filter.
 *
    * @param startNode the source node.
    * @param rel the relationship filter.
    * @return returns a collection of relation, end node pairs.
    */
  def outboundWithRelation(startNode:Node, rel:Relationship):Seq[(Relation, Node)]

  /**
   * Get all source vertices and relations of this end node.
 *
   * @param endNode the end node.
   * @return a collection of tuples of the source node and the relation.
   */
  def inbound(endNode:Node):Seq[(Node, Relation)]

  /**
   * Get all source vertices of this end node for the given relationship type.
 *
   * @param endNode the end node.
   * @param rel the relationship type.
   * @return returns a collection of the source vertices.
   */
  def inbound(endNode:Node, rel:Relationship):Seq[Node]

  /**
    * Get all inbound relation + node that match a relationship filter.
 *
    * @param endNode the end node.
    * @param rel the relationship filter.
    * @return returns a collection of start vectice + relation pairs.
    */
  def inboundWithRelation(endNode:Node, rel:Relationship):Seq[(Node, Relation)]

  /**
   * Find all relations connecting these vertices specified by direction.
 *
   * @param start the start node.
   * @param end the end node.
   * @param direction the direction.
   * @return returns a collection with all relations.
   */
  def relation(start:Node, end:Node, direction:Direction):Seq[Relation]

  /**
   * Finds the relations (if any) between these two node specified by the relationship type and direction.
 *
   * @param start the start node.
   * @param end the end node.
   * @param rel the relationship type.
   * @param direction the direction.
   * @return returns a collection of relations.
   */
  def relation(start:Node, end:Node, rel:Relationship, direction:Direction):Seq[Relation]

	/**
		* Find edge that this relation binds together. For various reasons this can return several edges, but will most
		* times only return one edge.
		*
		* @param relation the relation.
		* @return returns the Edge.
		*/
	def edges(relation: Relation):Seq[Edge]

	/**
		* Find the edges that this node are part of.
		*
		* @param node the node
		* @return retusn a list of edges that this node is part of.
		*/
	def edges(node:Node):Seq[Edge]
}

private class GraphImpl(val edges:EdgeStorage) extends Graph {

  private def start(startNode:Node, data:Seq[Edge] = edges.edges):Seq[Edge] = {
    data.filter(_.start.id.equals(startNode.id))
  }

  private def end(endNode:Node, data:Seq[Edge] = edges.edges):Seq[Edge] = {
    data.filter(_.end.id.equals(endNode.id))
  }

  override def degrees(node: Node, direction:Direction): Int = {
    val resultingEdges = direction match {
      case Direction.OUTBOUND => start(node)
      case Direction.INBOUND => end(node)
			case _ => {
				val out = start(node)
				val in = end(node)
				out.union(in)
			}
    }

    resultingEdges.size
  }

  override def inbound(startNode: Node): Seq[(Node, Relation)] = {
    end(startNode)
      .map { edge =>
        (edge.start, edge.relation)
      }.seq
  }

  override def inbound(endNode: Node, rel: Relationship): Seq[Node] = {
    end(endNode)
      .filter(_.relation.relType.equals(rel))
      .map(_.start)
      .seq
  }

  override def inboundWithRelation(endNode: Node, rel: Relationship): Seq[(Node, Relation)] = {
    end(endNode)
      .filter(_.relation.relType.equals(rel))
      .map(edge => (edge.start, edge.relation))
      .seq
  }

  override def outbound(startNode: Node): Seq[(Relation, Node)] = {
    start(startNode)
      .map { edge =>
        (edge.relation, edge.end)
      }.seq
  }

  override def outbound(startNode: Node, rel: Relationship): Seq[Node] = {
    start(startNode)
      .filter(_.relation.relType.equals(rel))
      .map(_.end)
      .seq
  }

  override def outboundWithRelation(startNode: Node, rel: Relationship): Seq[(Relation, Node)] = {
    start(startNode)
      .filter(_.relation.relType.equals(rel))
      .map(edge => (edge.relation, edge.end))
      .seq
  }

  override def relation(firstNode: Node, secondNode: Node, rel: Relationship, direction:Direction): Seq[Relation] = {
    val resultingEdges = direction match {
      case Direction.OUTBOUND => start(firstNode, end(secondNode)).filter(_.relation.relType.equals(rel))
      case Direction.INBOUND => end(firstNode, start(secondNode)).filter(_.relation.relType.equals(rel))
			case _ => {
				val out = start(firstNode).filter(_.relation.relType.equals(rel))
				val in = end(secondNode).filter(_.relation.relType.equals(rel))
				out.union(in)
			}
    }

    resultingEdges.map(_.relation).seq
  }

  override def degrees(node: Node, rel: Relationship, direction:Direction): Int = {
    val resultingEdges = direction match {
      case Direction.OUTBOUND => start(node).filter(_.relation.relType.equals(rel))
      case Direction.INBOUND => end(node).filter(_.relation.relType.equals(rel))
			case _ => {
				val out = start(node).filter(_.relation.relType.equals(rel))
				val in = end(node).filter(_.relation.relType.equals(rel))
				out.intersect(in)
			}
    }

    resultingEdges.size
  }

  override def relation(firstNode: Node, secondNode: Node, direction:Direction): Seq[Relation] = {
    val resultingEdges = direction match {
      case Direction.OUTBOUND => start(firstNode, end(secondNode))
      case Direction.INBOUND => end(firstNode, start(secondNode))
			case _ => {
				val out = start(firstNode)
				val in = end(secondNode)
				out.intersect(in)
			}
    }

    resultingEdges.map(_.relation).seq
  }

	override def edges(relation: Relation):Seq[Edge] = {
		edges.edges.filter(e => e.relation.relType.equals(relation.relType))
	}

	override def edges(node: Node): Seq[Edge] = {
		start(node) ++ end(node)
	}
}