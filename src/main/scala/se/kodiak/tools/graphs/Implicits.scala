package se.kodiak.tools.graphs

import model._

object Implicits {
  implicit def nodeImplicits(node:Node):ImplicitNode = new ImplicitNode(node)
  implicit def relationImplicits(relation:Relation):ImplicitRelation = new ImplicitRelation(relation)

  class ImplicitNode(val node:Node) extends GraphNodeDSL with GraphMutatorsDSL {
  }

  class ImplicitRelation(val relation:Relation) extends GraphRelationMutatorDSL {
  }
}

trait GraphNodeDSL {

  def node:Node

  // in/outbound through relationship
  /**
   * Alias for outbound.
   */
  def `-[]->`(rel:Relationship)(implicit graph:Graph):Seq[Node] = graph.outbound(node, rel)

  /**
   * Alias for inbound
   */
  def `<-[]-`(rel:Relationship)(implicit graph:Graph):Seq[Node] = graph.inbound(node, rel)
  def outbound(rel:Relationship)(implicit graph:Graph):Seq[Node] = graph.outbound(node, rel)
  def inbound(rel:Relationship)(implicit graph:Graph):Seq[Node] = graph.inbound(node, rel)

  // in/outbound without relationship filter.
  /**
   * Alias for outbound
   */
  def -->(implicit graph:Graph):Seq[(Relation, Node)] = graph.outbound(node)

  /**
   * Alias for inbound.
   */
  def <--(implicit graph:Graph):Seq[(Node, Relation)] = graph.inbound(node)
  def oubound(implicit graph:Graph):Seq[(Relation, Node)] = graph.outbound(node)
  def inboun(implicit graph:Graph):Seq[(Node, Relation)] = graph.inbound(node)

  // degrees
  def `-o-`(implicit graph:Graph):Int = graph.degrees(node, Direction.BOTH)
  def `-o->`(implicit graph:Graph):Int = graph.degrees(node, Direction.OUTBOUND)
  def `<-o-`(implicit graph:Graph):Int = graph.degrees(node, Direction.INBOUND)
  def `-[o]-`(rel:Relationship)(implicit graph:Graph):Int = graph.degrees(node, rel, Direction.BOTH)
  def `-[o]->`(rel:Relationship)(implicit graph:Graph):Int = graph.degrees(node, rel, Direction.OUTBOUND)
  def `<-[o]-`(rel:Relationship)(implicit graph:Graph):Int = graph.degrees(node, rel, Direction.INBOUND)

  // relations without filter
  /**
   * Alias for relation
   */
  def `-[X]->`(end:Node)(implicit graph:Graph):Seq[Relation] = graph.relation(node, end, Direction.OUTBOUND)
  /**
   * Alias for relation
   */
  def `<-[X]-`(start:Node)(implicit graph:Graph):Seq[Relation] = graph.relation(start, node, Direction.INBOUND)
  /**
   * Alias for relation
   */
  def `-[X]-`(end:Node)(implicit graph:Graph):Seq[Relation] = graph.relation(node, end, Direction.BOTH)
  def relation(other:Node, direction: Direction)(implicit graph:Graph):Seq[Relation] = graph.relation(node, other, direction)

  // relations with filter
  /**
   * Alias for relation
   */
  def `X]->`(rel:Relationship, end:Node)(implicit graph:Graph):Seq[Relation] = graph.relation(node, end, rel, Direction.OUTBOUND)
  /**
   * Alias for relation
   */
  def `<-[X`(start:Node, rel:Relationship)(implicit graph:Graph):Seq[Relation] = graph.relation(start, node, rel, Direction.INBOUND)
  /**
   * Alias for relation
   */
  def `<-[X]->`(rel:Relationship, end:Node)(implicit graph:Graph):Seq[Relation] = graph.relation(node, end, rel, Direction.BOTH)
  def relation(other:Node, rel:Relationship, direction: Direction)(implicit graph:Graph):Seq[Relation] = graph.relation(node, other, rel, direction)
}

trait GraphMutatorsDSL {

  def node:Node

  def `++[`(relation: Relation, end:Node)(implicit graph:Graph with Mutators):Unit = graph.add(node, relation, end)
  def `]++`(start:Node, relation:Relation)(implicit graph:Graph with Mutators):Unit = graph.add(start, relation, node)
  def `--[`(relation: Relation)(implicit graph:Graph with Mutators):Unit = graph.remove(relation)
  def `--[`(rel:Relationship)(implicit graph:Graph with Mutators):Unit = graph.remove(rel)
  def `--`(implicit graph:Graph with Mutators):Unit = graph.remove(node)

}

trait GraphRelationMutatorDSL {

  def relation:Relation

  def ++(start:Node, end:Node)(implicit graph:Graph with Mutators):Unit = graph.add(start, relation, end)
  def --(implicit graph:Graph with Mutators):Unit = graph.remove(relation)

}