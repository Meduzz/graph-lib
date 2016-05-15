package se.kodiak.tools.graphs

import se.kodiak.tools.graphs.model.{DataNode, HashNode, ListNode, Node, VerticeId}

import scala.collection.immutable.Map

trait NodeDelegate {

	def nodeIdGenerator:IdGenerator

	def node(data: String): DataNode = save(DataNode(nodeIdGenerator.generate(), data))
	def node(data: Map[String, String]): HashNode = save(HashNode(nodeIdGenerator.generate(), data))
	def node(data: Seq[String]): ListNode = save(ListNode(nodeIdGenerator.generate(), data))

	def node(id: VerticeId, data: String): DataNode = save(DataNode(id, data))
	def node(id: VerticeId, data: Map[String, String]): HashNode = save(HashNode(id, data))
	def node(id: VerticeId, data: Seq[String]): ListNode = save(ListNode(id, data))

	def save[T <: Node](node:T):T

	def loadDataNode(id:VerticeId):DataNode
	def loadHashNode(id:VerticeId):HashNode
	def loadListNode(id:VerticeId):ListNode

	def delete(node:Node):Unit
}
