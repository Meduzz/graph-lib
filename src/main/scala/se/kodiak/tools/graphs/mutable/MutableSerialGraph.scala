package se.kodiak.tools.graphs.mutable

import se.kodiak.tools.graphs.{Mutators, SerialGraph}
import se.kodiak.tools.graphs.model._

class MutableSerialGraph(protected var edges:Seq[Edge]) extends SerialGraph with Mutators {
  override def add(edge: Edge): Unit = edges = edges :+ edge

  override def remove(edge: Edge): Unit = edges = edges.filterNot(_.equals(edge))

  override def remove(vertice: Node): Unit = {
    edges = edges.filterNot(_.start.id.equals(vertice.id))
      .filterNot(_.end.id.equals(vertice.id))
  }

  override def remove(relation: Relation): Unit = edges = edges.filterNot(_.relation.id.equals(relation.id))

  override def remove(rel: Relationship): Unit = edges = edges.filterNot(_.relation.relType.equals(rel))

  override def add(start: Node, relation: Relation, end: Node): Unit = edges = edges :+ Edge(start, relation, end)
}
