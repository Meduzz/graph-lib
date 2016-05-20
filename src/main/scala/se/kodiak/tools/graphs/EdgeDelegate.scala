package se.kodiak.tools.graphs

import se.kodiak.tools.graphs.model.{Edge, Node, Relation}

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

trait EdgeDelegate {

	private var internalEdges:Seq[Edge] = null

	def edges:immutable.Seq[Edge] = {
		if (internalEdges == null) {
			internalEdges = initialize()
		}
		internalEdges.asInstanceOf[immutable.Seq[Edge]]
	}

	def add(start:Node, relation:Relation, end:Node)(implicit ec:ExecutionContext):Future[Edge] = add(Edge(start, relation, end))
	def add(edge:Edge)(implicit ec:ExecutionContext):Future[Edge] = save(edge).map(addLocal)

	def remove(edge:Edge)(implicit ec:ExecutionContext):Future[Unit] = {
		delete(edge).map(removeLocal)
	}

	def save(edge:Edge):Future[Edge]
	def delete(edge:Edge):Future[Edge]

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
