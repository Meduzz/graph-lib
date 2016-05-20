package se.kodiak.tools.graphs.graphsources

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import redis.RedisClient
import se.kodiak.tools.graphs.model._
import se.kodiak.tools.graphs.{Graph, Mutators}
import se.kodiak.tools.graphs.Implicits._
import se.kodiak.tools.graphs.graphsources.Redis.RedisGraphBuilder

import scala.collection.immutable._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

class RedisGraphSourceTest extends FunSuite with BeforeAndAfterAll with KeyBuilder with ScalaFutures {
  implicit val system = ActorSystem("graph-lib-test")
  implicit val redis = RedisClient()

	val duration = Duration(3L, TimeUnit.SECONDS)

  val graphName = "test"
	val nodeIndex:String = "node"
	val relationIndex:String = "relation"
	val indexIndex:String = "index"

  implicit val graph:Graph with Mutators = RedisGraphBuilder.forGraph(graphName).withNodePrefix(nodeIndex).withRelationPrefix(relationIndex).build()

  test("nodes and relations should be stored properly") {

		val entities = for {
			start <- graph.node()
			end <- graph.node()
			relation <- graph.relation("KNOWS")
			result <- start.link(relation, end)
		} yield (start, end, relation)

		val (start:Node, end:Node, relation:Relation) = Await.result(entities, duration)

		whenReady(redis.get[String](nodeKey(start.id))) { someNode =>
			assert(someNode.isDefined)
			val node = someNode.get
			assert(node equals "", "Start node had data.")
		}

		whenReady(redis.get[String](nodeKey(end.id))) { someNode =>
			assert(someNode.isDefined)
			val node = someNode.get
			assert(node equals "", "End node had data.")
		}

		whenReady(redis.get[String](relationKey(relation.id))) { someRel =>
			assert(someRel.isDefined)
			val rel = someRel.get
			assert(rel equals "KNOWS", "Relation did not match KNOWS.")
		}

		whenReady(redis.get[String](nodeIdIncrKey)) { someId =>
			assert(someId.isDefined)
			val id = someId.get
			assert(id.toInt == 2)
		}

		whenReady(redis.get[String](relationIdIncrKey)) { someId =>
			assert(someId.isDefined)
			val id = someId.get
			assert(id.toInt == 1)
		}
  }

  test("edges come and go, nodes remain") {
    val start = Node(1L)
    val end = Node(2L)
    start.findRelationsToNode(end, Direction.OUTBOUND).head.delete

    whenReady(redis.get[String](nodeKey(start.id))) { someNode =>
      assert(someNode.isDefined)
      val node = someNode.get
      assert(node equals "", "Start node had data.")
    }

    whenReady(redis.get[String](nodeKey(end.id))) { someNode =>
      assert(someNode.isDefined)
      val node = someNode.get
      assert(node equals "", "End node had data.")
    }

    whenReady(redis.get[String](relationKey(1L))) { someRel =>
      assert(someRel.isEmpty)
    }

    whenReady(redis.scard(indexKey)) { count =>
      assert(count == 0L)
    }

    start.link("HATES", end)

    whenReady(redis.get[String](nodeIdIncrKey)) { someId =>
      assert(someId.isDefined)
      val id = someId.get
      assert(id.toInt == 2)
    }

    whenReady(redis.get[String](relationIdIncrKey)) { someId =>
      assert(someId.isDefined)
      val id = someId.get
      assert(id.toInt == 2)
    }

    whenReady(redis.get[String](relationKey(2L))) { someRel =>
      assert(someRel.isDefined)
      val rel = someRel.get
      assert(rel equals "HATES", "Relation did not match HATES.")
    }
  }

  test("nodes and relations got types...ish") {
		val entities = for {
			start <- graph.node(Map("key" -> "value"))
			end <- graph.node(Seq("key", "value"))
			relation <- graph.relation("REL", Seq("key", "value"))
			result <- start.link(relation, end)
		} yield (start, end, relation)

		val (start:Node, end:Node, relation:Relation) = Await.result(entities, duration)

		val startData = graph.loadHashNode(start.id)
    val endData = graph.loadListNode(end.id)
    val relationData = graph.loadListRelation(relation.id)

		whenReady(startData) { node =>
			assert(node.data("key").equals("value"))
		}

		whenReady(endData) { node =>
			assert(node.data.size == 2)
			assert(node.data.contains("key"))
			assert(node.data.contains("value"))
		}

		whenReady(relationData) { rel =>
			assert(rel.data.size == 2)
			assert(rel.data.contains("key"))
			assert(rel.data.contains("value"))
		}

	}

  override protected def afterAll(): Unit = {
    redis.flushall()
  }
}
