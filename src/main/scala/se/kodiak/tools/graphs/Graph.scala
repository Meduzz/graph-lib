package se.kodiak.tools.graphs

import se.kodiak.tools.graphs.model._
import scala.collection.immutable._

object Graph {
  def apply(source:GraphSource):Graph with Mutators = new GraphImpl(source)
}

trait Graph {
  /**
   * Count the total number of relations of this vertice.
   * @param vertice the vertice.
   * @param direction specifies the direction to look
   * @return returns the count.
   */
  def degrees(vertice:Node, direction:Direction):Int

  /**
   * Count the total number of relations of the supplied type of this vertice.
   * @param vertice the vertice.
   * @param rel the relationship type.
   * @param direction specifies the direction to look
   * @return returns the count.
   */
  def degrees(vertice:Node, rel:Relationship, direction:Direction):Int

  /**
   * Get all outbound relations and their end vertice of this vertice..
   * @param vertice the source vertice.
   * @return returns a collection of tuples of the relation and the end vertice.
   */
  def outbound(vertice:Node):Seq[(Relation, Node)]

  /**
   * Get all outbound end vertice of this source vertice with the given relationship type.
   * @param vertice the source vertice.
   * @param rel the relationship type.
   * @return returns a collection of end vertices.
   */
  def outbound(vertice:Node, rel:Relationship):Seq[Node]

  /**
    * Get all outbound relation + vertice that match the relationship filter.
    * @param vertice the source vertice.
    * @param rel the relationship filter.
    * @return returns a collection of relation, end vertice pairs.
    */
  def outboundWithRelation(vertice:Node, rel:Relationship):Seq[(Relation, Node)]

  /**
   * Get all source vertices and relations of this end vertice.
   * @param vertice the end vertice.
   * @return a collection of tuples of the source vertice and the relation.
   */
  def inbound(vertice:Node):Seq[(Node, Relation)]

  /**
   * Get all source vertices of this end vertice for the given relationship type.
   * @param vertice the end vertice.
   * @param rel the relationship type.
   * @return returns a collection of the source vertices.
   */
  def inbound(vertice:Node, rel:Relationship):Seq[Node]

  /**
    * Get all inbound relation + vertice that match a relationship filter.
    * @param vertice the end vertice.
    * @param rel the relationship filter.
    * @return returns a collection of start vectice + relation pairs.
    */
  def inboundWithRelation(vertice:Node, rel:Relationship):Seq[(Node, Relation)]

  /**
   * Find all relations connecting these vertices specified by direction.
   * @param start the start vertice.
   * @param end the end vertice.
   * @param direction the direction.
   * @return returns a collection with all relations.
   */
  def relation(start:Node, end:Node, direction:Direction):Seq[Relation]

  /**
   * Finds the relations (if any) between these two vertice specified by the relationship type and direction.
   * @param start the start vertice.
   * @param end the end vertice.
   * @param rel the relationship type.
   * @param direction the direction.
   * @return returns a collection of relations.
   */
  def relation(start:Node, end:Node, rel:Relationship, direction:Direction):Seq[Relation]
}

private class GraphImpl(source:GraphSource) extends Graph with Mutators {

  private def start(vertice:Node, data:Seq[Edge] = source.edges):Seq[Edge] = {
    data.filter(_.start.id.equals(vertice.id))
  }

  private def end(vertice:Node, data:Seq[Edge] = source.edges):Seq[Edge] = {
    data.filter(_.end.id.equals(vertice.id))
  }

  override def degrees(vertice: Node, direction:Direction): Int = {
    val resultingEdges = direction match {
      case Direction.BOTH => {
        val out = start(vertice)
        val in = end(vertice)
        out.union(in)
      }
      case Direction.OUTBOUND => start(vertice)
      case Direction.INBOUND => end(vertice)
    }

    resultingEdges.size
  }

  override def inbound(vertice: Node): Seq[(Node, Relation)] = {
    end(vertice)
      .map { edge =>
        (edge.start, edge.relation)
      }.seq
  }

  override def inbound(vertice: Node, rel: Relationship): Seq[Node] = {
    end(vertice)
      .filter(_.relation.relType.equals(rel))
      .map(_.start)
      .seq
  }

  override def inboundWithRelation(vertice: Node, rel: Relationship): Seq[(Node, Relation)] = {
    end(vertice)
      .filter(_.relation.relType.equals(rel))
      .map(edge => (edge.start, edge.relation))
      .seq
  }

  override def outbound(vertice: Node): Seq[(Relation, Node)] = {
    start(vertice)
      .map { edge =>
        (edge.relation, edge.end)
      }.seq
  }

  override def outbound(vertice: Node, rel: Relationship): Seq[Node] = {
    start(vertice)
      .filter(_.relation.relType.equals(rel))
      .map(_.end)
      .seq
  }

  override def outboundWithRelation(vertice: Node, rel: Relationship): Seq[(Relation, Node)] = {
    start(vertice)
      .filter(_.relation.relType.equals(rel))
      .map(edge => (edge.relation, edge.end))
      .seq
  }

  override def relation(node1: Node, node2: Node, rel: Relationship, direction:Direction): Seq[Relation] = {
    val resultingEdges = direction match {
      case Direction.BOTH => {
        val out = start(node1).filter(_.relation.relType.equals(rel))
        val in = end(node2).filter(_.relation.relType.equals(rel))
        out.union(in)
      }
      case Direction.OUTBOUND => start(node1, end(node2)).filter(_.relation.relType.equals(rel))
      case Direction.INBOUND => end(node1, start(node2)).filter(_.relation.relType.equals(rel))
    }

    resultingEdges.map(_.relation).seq
  }

  override def degrees(vertice: Node, rel: Relationship, direction:Direction): Int = {
    val resultingEdges = direction match {
      case Direction.BOTH => {
        val out = start(vertice).filter(_.relation.relType.equals(rel))
        val in = end(vertice).filter(_.relation.relType.equals(rel))
        out.intersect(in)
      }
      case Direction.OUTBOUND => start(vertice).filter(_.relation.relType.equals(rel))
      case Direction.INBOUND => end(vertice).filter(_.relation.relType.equals(rel))
    }

    resultingEdges.size
  }

  override def relation(node1: Node, node2: Node, direction:Direction): Seq[Relation] = {
    val resultingEdges = direction match {
      case Direction.BOTH => {
        val out = start(node1)
        val in = end(node2)
        out.intersect(in)
      }
      case Direction.OUTBOUND => start(node1, end(node2))
      case Direction.INBOUND => end(node1, start(node2))
    }

    resultingEdges.map(_.relation).seq
  }

  override def add(start: Node, relation: Relation, end: Node): Unit = source.add(start, relation, end)

  override def remove(relation: Relation): Unit = source.remove(relation)
}