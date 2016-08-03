package se.kodiak.tools.graphs.edge.delegate

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.util.ByteString
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import redis.RedisClient
import se.kodiak.tools.graphs.Graph
import se.kodiak.tools.graphs.edge.EdgeStorage
import se.kodiak.tools.graphs.model._
import se.kodiak.tools.graphs.Implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class RedisGraphSourceTest extends FunSuite with BeforeAndAfterAll with ScalaFutures {
  implicit val system = ActorSystem("graph-lib-test")
  implicit val redis = RedisClient()

	val duration = Duration(3L, TimeUnit.SECONDS)

  val graphName = "test"

	implicit val edges = EdgeStorage(RedisStorageDelegate(graphName, ":"))
  implicit val graph:Graph = Graph(edges)

  test("we store edges correctly") {

		val result = addOne()

		whenReady(redis.smembers(graphName).mapTo[Seq[ByteString]]) { members =>
			assert(members.nonEmpty)
			assert(members.size == 1)

			val Array(start:String, id:String, typ:String, end:String) = members.head.utf8String.split(":")
			val edge = Edge(Node(start), Relation(id, typ), Node(end))

			assert(result.equals(edge))
		}
  }

	test("we remove edges correctly") {
		redis.flushall()

		val edge = addOne()
		edge.start.delete()
		whenReady(redis.smembers(graphName).mapTo[Seq[ByteString]]) { members =>
			assert(members.isEmpty)
		}

		val edge2 = addOne()
		edge2.relation.delete()

		whenReady(redis.smembers(graphName).mapTo[Seq[ByteString]]) { members =>
			assert(members.isEmpty)
		}

		val edge3 = addOne()
		edge3.end.delete()

		whenReady(redis.smembers(graphName).mapTo[Seq[ByteString]]) { members =>
			assert(members.isEmpty)
		}
	}

	def addOne():Edge = {
		val first = Node("1")
		val second = Node("2")
		val relation = Relation("1", "SPAM")
		edges.add(first, relation, second)
	}

  override protected def afterAll(): Unit = {
    redis.flushall()
  }
}
