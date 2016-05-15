package se.kodiak.tools.graphs.graphsources

import java.util.concurrent.TimeUnit

import redis.RedisClient
import se.kodiak.tools.graphs._
import se.kodiak.tools.graphs.model._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object Redis {

	trait RedisGraphBuilder { self:RedisGraphBuilderImpl =>
		def withNodeIdGenerator(idGen:IdGenerator):RedisGraphBuilder = copy(nodeIdGen = idGen)
		def withRelationIdGenerator(idGen:IdGenerator):RedisGraphBuilder = copy(relIdGen = idGen)
		def withRelationPrefix(prefix:String):RedisGraphBuilder = copy(relIdGen = new RedisIdGenerator(graphName, prefix)(redis))
		def withNodePrefix(prefix:String):RedisGraphBuilder = copy(nodeIdGen = new RedisIdGenerator(graphName, prefix)(redis))
		def build():Graph with Mutators = {
			require(nodeIdGen != null, "Node ID generator must not be null.")
			require(relIdGen != null, "Relation ID generator must not be null.")

			Graph(new RedisEdgeDelegate(graphName)(redis), new RedisNodeDelegate(graphName, nodeIdGen)(redis), new RedisRelationDelegate(graphName, relIdGen)(redis))
		}
	}

	object RedisGraphBuilder {
		def forGraph(name:String)(implicit redis:RedisClient):RedisGraphBuilder = RedisGraphBuilderImpl(redis, name, null, null)
	}

	case class RedisGraphBuilderImpl(redis:RedisClient, graphName:String, nodeIdGen:IdGenerator, relIdGen:IdGenerator) extends RedisGraphBuilder

	class RedisNodeDelegate(val graphName:String, val nodeIdGenerator: IdGenerator)(implicit val redis:RedisClient) extends NodeDelegate with KeyBuilder {

		val nodeIndex:String = "node"
		val relationIndex:String = "relation"
		val indexIndex:String = "index"

		val duration = Duration(3L, TimeUnit.SECONDS)

		override def save[T <: Node](node: T): T = {
			val maybe = node match {
				case node:DataNode => saveDataNode(node)
				case node:HashNode => saveHashNode(node)
				case node:ListNode => saveListNode(node)
				case node:LazyNode => saveLazyNode(node)
			}

			val res = Await.result[Boolean](maybe, duration)

			if (res) {
				node
			} else {
				throw new RuntimeException("Saving node failed.")
			}
		}

		override def loadDataNode(id: VerticeId): DataNode = {
			val futureNode = for {
				data <- redis.get[String](nodeKey(id))
			} yield DataNode(id, data.getOrElse(""))

			Await.result(futureNode, duration)
		}

		override def loadHashNode(id: VerticeId): HashNode = {
			val futureNode = for {
				data <- redis.hgetall[String](nodeKey(id))
			} yield HashNode(id, data)

			Await.result(futureNode, duration)
		}

		override def loadListNode(id: VerticeId): ListNode = {
			val futureNode = for {
				data <- redis.smembers[String](nodeKey(id))
			} yield ListNode(id, data)

			Await.result(futureNode, duration)
		}

		override def delete(node: Node): Unit = redis.del(nodeKey(node.id))

		def saveDataNode(node:DataNode):Future[Boolean] = {
				redis.set(nodeKey(node.id), node.data)
		}

		def saveHashNode(node:HashNode):Future[Boolean] = {
			redis.hmset(nodeKey(node.id), node.data)
		}

		def saveListNode(node:ListNode):Future[Boolean] = {
			Future.sequence(node.data.map(item => redis.sadd(nodeKey(node.id), item))).map(a => a.sum > 0)
		}

		def saveLazyNode(node:LazyNode):Future[Boolean] = {
			redis.set(nodeKey(node.id), "")
		}
	}

	class RedisRelationDelegate(val graphName:String, val relationIdGenerator: IdGenerator)(implicit val redis:RedisClient) extends RelationDelegate with KeyBuilder {

		val nodeIndex:String = "node"
		val relationIndex:String = "relation"
		val indexIndex:String = "index"

		val duration = Duration(3L, TimeUnit.SECONDS)

		override def save[T <: Relation](relation: T): T = {
			val maybe = relation match {
				case rel:DataRelation => saveDataRelation(rel)
				case rel:HashRelation => saveHashRelation(rel)
				case rel:ListRelation => saveListRelation(rel)
				case rel:LazyRelation => saveLazyRelation(rel)
			}

			val result = Await.result(maybe, duration)

			if (result) {
				relation
			} else {
				throw new RuntimeException("Saving relatioin failed.")
			}
		}

		override def loadDataRelation(id: RelationId): DataRelation = {
			val futureRelation = for {
				rel <- redis.get[String](relationKey(id))
				data <- redis.get[String](relationDataKey(id))
			} yield DataRelation(id, rel.get, data.getOrElse(""))

			Await.result(futureRelation, duration)
		}

		override def loadHashRelation(id: RelationId): HashRelation = {
			val futureRelation = for {
				rel <- redis.get[String](relationKey(id))
				data <- redis.hgetall[String](relationDataKey(id))
			} yield HashRelation(id, rel.get, data)

			Await.result(futureRelation, duration)
		}

		override def loadListRelation(id: RelationId): ListRelation = {
			val futureRelation = for {
				rel <- redis.get[String](relationKey(id))
				data <- redis.smembers[String](relationDataKey(id))
			} yield ListRelation(id, rel.get, data)

			Await.result(futureRelation, duration)
		}

		override def delete(relation: Relation): Unit = redis.del(relationKey(relation.id))

		def saveDataRelation(relation:DataRelation):Future[Boolean] = {
			for {
				rel <- redis.set(relationKey(relation.id), relation.relType)
				data <- redis.set(relationDataKey(relation.id), relation.data)
			} yield rel && data
		}

		def saveHashRelation(relation:HashRelation):Future[Boolean] = {
			for {
				rel <- redis.set(relationKey(relation.id), relation.relType)
				data <- redis.hmset(relationDataKey(relation.id), relation.data)
			} yield rel && data
		}

		def saveListRelation(relation:ListRelation):Future[Boolean] = {
			for {
				rel <- redis.set(relationKey(relation.id), relation.relType)
				data <- Future.sequence(relation.data.map(item => redis.sadd(relationDataKey(relation.id), item)))
			} yield rel && data.sum > 0
		}

		def saveLazyRelation(relation:LazyRelation):Future[Boolean] = {
			redis.set(relationKey(relation.id), relation.relType)
		}
	}

	class RedisEdgeDelegate(val graphName:String)(implicit val redis:RedisClient) extends EdgeDelegate with KeyBuilder {

		val nodeIndex:String = "node"
		val relationIndex:String = "relation"
		val indexIndex:String = "index"

		val duration = Duration(3L, TimeUnit.SECONDS)

		override def save(edge: Edge): Edge = {
			redis.sadd(indexKey, edgeToString(edge))
			edge
		}

		override def initialize(): Seq[Edge] = {
			val futureEdges = redis
				.smembers[String](indexKey)
				.map(items => items.map(str => {
					val Array(start:String, rel:String, end:String) = str.split(":")
					redis.get[String](relationKey(rel))
						.map(someRelation => someRelation.map(relation => Edge(Node(start), Relation(rel, relation), Node(end))))
				}))
				.map(a => Future.sequence(a))
				.flatMap(a => a)
				.map(edges => {
					edges.filter({
						case Some(edge) => true
						case None => false
					}).map(_.get)
				})
				.map(edges => Seq.concat(edges))

			Await.result(futureEdges, duration)
		}

		override def delete(edge: Edge): Unit = {
			redis.srem(indexKey, edgeToString(edge))
		}

		def edgeToString(edge:Edge):String = {
			s"${edge.start.id}:${edge.relation.id}:${edge.end.id}"
		}
	}

	class RedisIdGenerator(val graphName:String, val prefix:String)(implicit redis:RedisClient) extends IdGenerator {

		val duration = Duration(3L, TimeUnit.SECONDS)

		override def generate(): VerticeId = {
			val incr = redis.incr(s"$graphName.$prefix")

			val result = Await.result(incr, duration)
			result
		}
	}

}

trait KeyBuilder {

  def indexIndex:String
  def nodeIndex:String
  def relationIndex:String
  def graphName:String

  def indexKey:String = s"$graphName.$indexIndex"
  def nodeKey(id:VerticeId):String = s"$graphName.$nodeIndex.$id"
  def nodeIdIncrKey:String = s"$graphName.$nodeIndex"
  def relationKey(id:RelationId):String = s"$graphName.$relationIndex.$id"
  def relationIdIncrKey:String = s"$graphName.$relationIndex"
  def relationDataKey(id:RelationId):String = s"${relationKey(id)}.data"

}