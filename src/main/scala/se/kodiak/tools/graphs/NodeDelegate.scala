package se.kodiak.tools.graphs

import se.kodiak.tools.graphs.model.{DataNode, HashNode, ListNode, Node, VerticeId}

import scala.collection.immutable.Map
import scala.concurrent.Future

trait NodeDelegate {

	def nodeIdGenerator:IdGenerator

	def node(data: String):Future[DataNode] = save(DataNode(nodeIdGenerator.generate(), data))
	def node(data: Map[String, String]):Future[HashNode] = save(HashNode(nodeIdGenerator.generate(), data))
	def node(data: Seq[String]):Future[ListNode] = save(ListNode(nodeIdGenerator.generate(), data))

	def node(id: VerticeId, data: String):Future[DataNode] = save(DataNode(id, data))
	def node(id: VerticeId, data: Map[String, String]):Future[HashNode] = save(HashNode(id, data))
	def node(id: VerticeId, data: Seq[String]):Future[ListNode] = save(ListNode(id, data))

	def save[T <: Node](node:T):Future[T]

	def loadDataNode(id:VerticeId):Future[DataNode]
	def loadHashNode(id:VerticeId):Future[HashNode]
	def loadListNode(id:VerticeId):Future[ListNode]

	def delete(node:Node):Future[Unit]
}
