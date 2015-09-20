package se.kodiak.tools.graphs

import se.kodiak.tools.graphs.immutable.{ImmutableGraph, ImmutableSerialGraph}
import se.kodiak.tools.graphs.model._
import se.kodiak.tools.graphs.mutable.{MutableSerialGraph, MutableGraph}

import scala.collection.parallel.ParSeq

// TODO add cypher inspired implicit explicits to Node & Relation
// TODO add callbacks for new & removed edges when Mutators are called. Perhaps in 2 flavours, one with callbacks and one with a simple save...
// TODO turn Node and Relation into traits.
// TODO have a look at how to make graphs lazyloaded.

object Graph {
  def immutable(edges:Seq[Edge]):Graph = new ImmutableGraph(edges.par)
  def immutableSerial(edges:Seq[Edge]):Graph = new ImmutableSerialGraph(edges)
  def mutable(edges:Seq[Edge]):Graph with Mutators = new MutableGraph(edges.par)
  def mutableSerial(edges:Seq[Edge]):Graph with Mutators = new MutableSerialGraph(edges)

  def build(start:Map[VerticeId, List[RelationId]], end:Map[RelationId, VerticeId], relTypes:Map[RelationId, Relationship]):Seq[Edge] = {
    start.flatMap { pair =>
      val startNode = Node(pair._1)
      pair._2.map { relId =>
        val relation = Relation(relId, relTypes(relId))
        val endNode = Node(end(relId))

        Edge(startNode, relation, endNode)
      }
    }.toSeq
  }

  def build(data:Seq[(VerticeId, RelationId, Relationship, VerticeId)]):Seq[Edge] = {
    data.map { tuple =>
      Edge(Node(tuple._1), Relation(tuple._2, tuple._3), Node(tuple._4))
    }
  }
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

trait SerialGraph extends Graph {

  protected def edges:Seq[Edge]

  private def start(vertice:Node, data:Seq[Edge] = edges):Seq[Edge] = {
    data.filter(_.start.id.equals(vertice.id))
  }

  private def end(vertice:Node, data:Seq[Edge] = edges):Seq[Edge] = {
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
      }
  }

  override def inbound(vertice: Node, rel: Relationship): Seq[Node] = {
    end(vertice)
      .filter(_.relation.relType.equals(rel))
      .map(_.start)
  }

  override def outbound(vertice: Node): Seq[(Relation, Node)] = {
    start(vertice)
      .map { edge =>
        (edge.relation, edge.end)
      }
  }

  override def outbound(vertice: Node, rel: Relationship): Seq[Node] = {
    start(vertice)
      .filter(_.relation.relType.equals(rel))
      .map(_.end)
  }

  override def relation(startNode: Node, endNode: Node, rel: Relationship, direction:Direction): Seq[Relation] = {
    val resultingEdges = direction match {
      case Direction.BOTH => {
        val out = start(startNode).filter(_.relation.relType.equals(rel))
        val in = end(endNode).filter(_.relation.relType.equals(rel))
        out.intersect(in)
      }
      case Direction.OUTBOUND => start(startNode, end(endNode)).filter(_.relation.relType.equals(rel))
      case Direction.INBOUND => end(endNode, start(startNode)).filter(_.relation.relType.equals(rel))
    }

    resultingEdges.map(_.relation)
  }

  override def degrees(vertice: Node, rel: Relationship, direction:Direction): Int = {
    val resultingEdges = direction match {
      case Direction.BOTH => {
        val out = start(vertice).filter(_.relation.relType.equals(rel))
        val in = end(vertice).filter(_.relation.relType.equals(rel))
        out.union(in)
      }
      case Direction.OUTBOUND => start(vertice).filter(_.relation.relType.equals(rel))
      case Direction.INBOUND => end(vertice).filter(_.relation.relType.equals(rel))
    }

    resultingEdges.count(_.relation.relType.equals(rel))
  }

  override def relation(startNode: Node, endNode: Node, direction:Direction): Seq[Relation] = {
    val resultingEdges = direction match {
      case Direction.BOTH => {
        val out = start(startNode)
        val in = end(endNode)
        out.intersect(in)
      }
      case Direction.OUTBOUND => start(startNode, end(endNode))
      case Direction.INBOUND => end(endNode, start(startNode))
    }

    resultingEdges.map(_.relation)
  }
}

trait ParallellGraph extends Graph {

  protected def edges:ParSeq[Edge]

  private def start(vertice:Node, data:ParSeq[Edge] = edges):ParSeq[Edge] = {
    data.filter(_.start.id.equals(vertice.id))
  }

  private def end(vertice:Node, data:ParSeq[Edge] = edges):ParSeq[Edge] = {
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

  override def relation(startNode: Node, endNode: Node, rel: Relationship, direction:Direction): Seq[Relation] = {
    val resultingEdges = direction match {
      case Direction.BOTH => {
        val out = start(startNode).filter(_.relation.relType.equals(rel))
        val in = end(endNode).filter(_.relation.relType.equals(rel))
        out.union(in)
      }
      case Direction.OUTBOUND => start(startNode, end(endNode)).filter(_.relation.relType.equals(rel))
      case Direction.INBOUND => end(endNode, start(startNode)).filter(_.relation.relType.equals(rel))
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

  override def relation(startNode: Node, endNode: Node, direction:Direction): Seq[Relation] = {
    val resultingEdges = direction match {
      case Direction.BOTH => {
        val out = start(startNode)
        val in = end(endNode)
        out.intersect(in)
      }
      case Direction.OUTBOUND => start(startNode, end(endNode))
      case Direction.INBOUND => end(endNode, start(startNode))
    }

    resultingEdges.map(_.relation)
      .seq
  }
}

trait Mutators {
  def add(edge:Edge):Unit
  def add(start:Node, relation: Relation, end:Node):Unit
  def remove(edge:Edge):Unit
  def remove(vertice:Node):Unit
  def remove(relation: Relation):Unit
  def remove(rel:Relationship):Unit
}