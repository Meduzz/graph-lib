package se.kodiak.tools.graphs

import se.kodiak.tools.graphs.model.{Edge, Node, Relation}

import scala.collection.immutable

trait EdgeDelegate {

	private var internalEdges:Seq[Edge] = null

	def edges:immutable.Seq[Edge] = {
		if (internalEdges == null) {
			internalEdges = initialize()
		}
		internalEdges.asInstanceOf[immutable.Seq[Edge]]
	}

	def add(start:Node, relation:Relation, end:Node):Edge = add(Edge(start, relation, end))
	def add(edge:Edge):Edge = addLocal(save(edge))

	def remove(edge:Edge):Unit = {
		delete(edge)
		removeLocal(edge)
	}

	def save(edge:Edge):Edge
	def delete(edge:Edge):Unit

	def initialize():Seq[Edge]

	protected def addLocal(edge:Edge):Edge = {
		if (internalEdges == null) {
			internalEdges = initialize()
		}

		internalEdges = internalEdges ++ Seq(edge)
		edge
	}

	protected def removeLocal(edge:Edge):Unit = {
		if (internalEdges == null) {
			internalEdges = initialize()
		}

		internalEdges = internalEdges.filterNot(e => e.equals(edge))
	}
}
