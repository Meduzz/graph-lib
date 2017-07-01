package se.kodiak.tools.graphs.edge

import se.kodiak.tools.graphs.model.{Edge, Node, Relation}

import scala.collection.immutable

object EdgeStorage {
	def apply(delegate: EdgeStorageDelegate):EdgeStorage = new EdgeStorage(delegate)
}

class EdgeStorage(delegate:EdgeStorageDelegate) {

	private var internalEdges:Seq[Edge] = delegate.initialize()

	def edges:immutable.Seq[Edge] = {
		internalEdges.asInstanceOf[immutable.Seq[Edge]]
	}

	def add(start:Node, relation:Relation, end:Node):Edge = add(Edge(start, relation, end))
	def add(edge:Edge):Edge = save(edge)

	def remove(edge:Edge):Unit = {
		delete(edge)
	}

	def save(edge:Edge):Edge = {
		if (delegate.onAdd(edge)) {
			internalEdges = internalEdges ++ Seq(edge)
		}

		edge
	}

	def delete(edge:Edge):Edge = {
		if (delegate.onDelete(edge)) {
			internalEdges = internalEdges.filterNot(e => e.equals(edge))
		}

		edge
	}

	def close():Unit = delegate.close()
}

trait EdgeStorageDelegate {

	def onAdd(edge:Edge):Boolean
	def onDelete(edge:Edge):Boolean

	def initialize():Seq[Edge]

	def close():Unit

}
