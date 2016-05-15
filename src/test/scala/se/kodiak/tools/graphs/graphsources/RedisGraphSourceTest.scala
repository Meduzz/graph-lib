package se.kodiak.tools.graphs.graphsources

import akka.actor.ActorSystem
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import redis.RedisClient
import se.kodiak.tools.graphs.model._
import se.kodiak.tools.graphs.{Graph, Mutators}
import se.kodiak.tools.graphs.Implicits._
import se.kodiak.tools.graphs.graphsources.Redis.RedisGraphBuilder

import scala.collection.immutable._

class RedisGraphSourceTest extends FunSuite with BeforeAndAfterAll with KeyBuilder with ScalaFutures {
  implicit val system = ActorSystem("graph-lib-test")
  implicit val redis = RedisClient()

  val graphName = "test"
	val nodeIndex:String = "node"
	val relationIndex:String = "relation"
	val indexIndex:String = "index"

  implicit val graph:Graph with Mutators = RedisGraphBuilder.forGraph(graphName).withNodePrefix(nodeIndex).withRelationPrefix(relationIndex).build()

  test("nodes and relations should be stored properly") {
    val start = graph.node()
    val end = graph.node()
    val relation = graph.relation("KNOWS")

    start.link(relation, end)

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
    val start = graph.node(Map("key" -> "value"))
    val end = graph.node(Seq("key", "value"))
    val relation = graph.relation("REL", Seq("key", "value"))

		val startData = graph.loadHashNode(start.id)
    val endData = graph.loadListNode(end.id)
    val relationData = graph.loadListRelation(relation.id)

    assert(endData.data.size == 2)
    assert(endData.data.contains("key"))
    assert(endData.data.contains("value"))

    assert(relationData.data.size == 2)
    assert(relationData.data.contains("key"))
    assert(relationData.data.contains("value"))

		assert(startData.data("key").equals("value"))
	}

  override protected def afterAll(): Unit = {
    redis.flushall()
  }
}
