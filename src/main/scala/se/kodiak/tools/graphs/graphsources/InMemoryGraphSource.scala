package se.kodiak.tools.graphs.graphsources

import java.util.concurrent.atomic.AtomicLong

import se.kodiak.tools.graphs.GraphSource
import se.kodiak.tools.graphs.model._

import scala.collection.immutable._

class InMemoryGraphSource(var edges:Seq[Edge], nodeCount:Long, relationCount:Long) extends GraphSource {

  val nodeId = new AtomicLong(nodeCount)
  val relationId = new AtomicLong(relationCount)

  /**
    * Creates a new edge and adds it to the internal source.
    *
    * @param start the start vertice
    * @param relation the relation
    * @param end the end vertice.
    */
  override def add(start: Node, relation: Relation, end: Node): Unit = edges = edges ++ Seq(Edge(start, relation, end))

  /**
    * Remove the Edge bound by this this relation.
    *
    * @param relation the relation
    */
  override def remove(relation: Relation): Unit = edges = edges.filterNot(_.relation.id.equals(relation.id))

  override def relation(rel: Relationship): Relation = Relation(relationId.incrementAndGet(), rel)

  override def node(): Node = Node(nodeId.incrementAndGet())

  override def node(data: String): DataNode = DataNode(nodeId.incrementAndGet(), data)

  override def node(data: Map[String, String]): HashNode = HashNode(nodeId.incrementAndGet(), data)

  override def relation(rel: Relationship, data: String): DataRelation = DataRelation(relationId.incrementAndGet(), rel, data)

  override def relation(rel: Relationship, data: Map[String, String]): HashRelation = HashRelation(relationId.incrementAndGet(), rel, data)

  override def loadDataRelation(id: RelationId): DataRelation = throw new RuntimeException("Not implemented.")

  override def loadDataNode(id:VerticeId):DataNode = throw new RuntimeException("Not implemented.")

  override def loadHashNode(id: VerticeId): HashNode = throw new RuntimeException("Not implemented.")

  override def loadHashRelation(id: RelationId): HashRelation = throw new RuntimeException("Not implemented.")

  override def relationSeq(rel: Relationship): Relation = ListRelation(relationId.incrementAndGet(), rel, Seq())

  override def loadSeqNode(id: VerticeId): ListNode = throw new RuntimeException("Not implemented.")

  override def nodeSeq(): Node = ListNode(nodeId.incrementAndGet(), Seq())

  override def loadSeqRelation(id: RelationId): ListRelation = throw new RuntimeException("Not implemented.")
}

object InMemoryGraphSource {
  def build(data:Seq[(VerticeId, RelationId, Relationship, VerticeId)], nodeIndex:Long, relationIndex:Long):InMemoryGraphSource = {
    new InMemoryGraphSource(data.map { tuple =>
      Edge(Node(tuple._1), Relation(tuple._2, tuple._3), Node(tuple._4))
    }, nodeIndex, relationIndex)
  }

  def empty():InMemoryGraphSource = new InMemoryGraphSource(Seq(), 0L, 0L)
}
