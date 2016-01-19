package se.kodiak.tools.graphs.graphsources

import akka.actor.ActorSystem
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import redis.RedisClient
import se.kodiak.tools.graphs.model._
import se.kodiak.tools.graphs.{Mutators, Graph}
import se.kodiak.tools.graphs.Implicits._

class RedisGraphSourceTest extends FunSuite with BeforeAndAfterAll with KeyBuilder with ScalaFutures {
  implicit val system = ActorSystem("graph-lib-test")
  val redis = RedisClient()
  val graphName = "test"
  val source = RedisGraphSource(redis, graphName)
  implicit val graph:Graph with Mutators = Graph(source)

  val nodeIndex:String = "se.kodiak.tools.graphs.node"
  val relationIndex:String = "se.kodiak.tools.graphs.relation"
  val indexIndex:String = "se.kodiak.tools.graphs.index"

  test("nodes and relations should be stored properly") {
    val start = source.node()
    val end = source.node()
    val relation = source.relation("KNOWS")

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
    start.findRelationsToNode(end, Direction.OUTBOUND).head.remove

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

  override protected def afterAll(): Unit = {
    redis.flushall()
  }
}
