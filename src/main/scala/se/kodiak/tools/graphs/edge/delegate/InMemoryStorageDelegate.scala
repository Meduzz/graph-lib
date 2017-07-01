package se.kodiak.tools.graphs.edge.delegate

import se.kodiak.tools.graphs.edge.EdgeStorageDelegate
import se.kodiak.tools.graphs.model.Edge

object InMemoryStorageDelegate {
	def apply(data:Seq[Edge]):EdgeStorageDelegate = new InMemoryStorageDelegate(data)
}

class InMemoryStorageDelegate(val edges:Seq[Edge]) extends EdgeStorageDelegate {

	override def onAdd(edge:Edge):Boolean = true

	override def onDelete(edge:Edge):Boolean = true

	override def initialize():Seq[Edge] = edges

	override def close() = Unit
}
