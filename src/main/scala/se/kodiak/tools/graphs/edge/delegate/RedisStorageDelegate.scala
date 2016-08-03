package se.kodiak.tools.graphs.edge.delegate

import java.util.concurrent.TimeUnit

import redis.RedisClient
import se.kodiak.tools.graphs.edge.EdgeStorageDelegate
import se.kodiak.tools.graphs.model.{Edge, Node, Relation}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

object RedisStorageDelegate {
	def apply(db:String, split:String = ":")(implicit redis:RedisClient, ec:ExecutionContext):RedisStorageDelegate = new RedisStorageDelegate(db, split)
}

class RedisStorageDelegate(val db:String, val split:String)(implicit redis:RedisClient, ec:ExecutionContext) extends EdgeStorageDelegate {

	private val duration = Duration(3L, TimeUnit.SECONDS)

	override def onAdd(edge:Edge):Boolean = {
		val redisResult = redis.sadd(db, edgeToString(edge))
			.map {
				case 1 => true
				case _ => false
			}

		Await.result(redisResult, duration)
	}

	override def onDelete(edge:Edge):Boolean = {
		val redisResult = redis.srem(db, edgeToString(edge))
			.map {
				case 0 => false
				case _ => true
			}

		Await.result(redisResult, duration)
	}

	override def initialize():Seq[Edge] = {
		val futureEdges = redis
			.smembers[String](db)
			.map(items => items.map(str => {
				val Array(start:String, rel:String, typ:String, end:String) = str.split(split)
				Edge(Node(start), Relation(rel, typ), Node(end))
			}))
			.map(edges => Seq.concat(edges))

		Await.result(futureEdges, duration)
	}

	// TODO add support for union and other cool set operations

	def edgeToString(edge:Edge):String = {
		s"${edge.start.id}:${edge.relation.id}:${edge.relation.relType}:${edge.end.id}"
	}
}
