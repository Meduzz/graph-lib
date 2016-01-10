package se.kodiak.tools.graphs

import se.kodiak.tools.graphs.model._

trait Mutators {
  /**
    * Creates a new edge and adds it to the internal source.
    * @param start the start vertice
    * @param relation the relation
    * @param end the end vertice.
    */
  def add(start:Node, relation: Relation, end:Node):Unit

  /**
    * Remove the Edge bound by this this relation.
    * @param relation the relation
    */
  def remove(relation: Relation):Unit
}
