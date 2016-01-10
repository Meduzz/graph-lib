package se.kodiak.tools.graphs.graphsources

import scala.collection.immutable._
import se.kodiak.tools.graphs.GraphSource
import se.kodiak.tools.graphs.model._

class SeqBasedInMemory(var edges:Seq[Edge]) extends GraphSource {

  /**
    * Creates a new edge and adds it to the internal source.
    * @param start the start vertice
    * @param relation the relation
    * @param end the end vertice.
    */
  override def add(start: Node, relation: Relation, end: Node): Unit = edges = edges ++ Seq(Edge(start, relation, end))

  /**
    * Remove the Edge bound by this this relation.
    * @param relation the relation
    */
  override def remove(relation: Relation): Unit = edges = edges.filterNot(_.relation.id.equals(relation.id))
}

object SeqBasedInMemory {
  def build(data:Seq[(VerticeId, RelationId, Relationship, VerticeId)]):SeqBasedInMemory = {
    new SeqBasedInMemory(data.map { tuple =>
      Edge(Node(tuple._1), Relation(tuple._2, tuple._3), Node(tuple._4))
    })
  }
}
