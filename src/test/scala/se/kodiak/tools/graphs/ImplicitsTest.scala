package se.kodiak.tools.graphs

import org.scalatest.FunSuite
import org.scalatest.concurrent.ScalaFutures
import se.kodiak.tools.graphs.Implicits._
import se.kodiak.tools.graphs.graphsources.InMemory._
import se.kodiak.tools.graphs.model._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImplicitsTest extends FunSuite with ScalaFutures {

  implicit val graph:Graph with Mutators = InMemoryBuilder.fromData(Datasource.prebuilt()).build()

  test("let there be mutators") {
    val link = Datasource.person2.link(Datasource.Rel.KNOWS, Datasource.person3)

		whenReady(link) { rel =>
			val knows = Datasource.person2.endNodesOfRelationType(Datasource.Rel.KNOWS)(graph)

			assert(knows.size == 2)
			assert(knows.contains(Datasource.person3))
			assert(knows.contains(Datasource.person1))
		}
  }

  test("playing around with implicit explicits") {
    Datasource.gadget2.findRelationsToNode(Datasource.person3, Direction.INBOUND).foreach(_.delete)
    assert(Datasource.person3.degreesOfRelationType(Datasource.Rel.OWNS, Direction.OUTBOUND) == 0)
    Datasource.person1.link(Relation(8L, Datasource.Rel.OWNS), Datasource.gadget2)
    assert(Datasource.person1.degreesOfRelationType(Datasource.Rel.OWNS, Direction.OUTBOUND) == 1)
  }

  test("moar mutators") {
    val relations = Datasource.person1.outgoingRelations()
    val del = Future.sequence(relations.map(_._1).map(_.delete))

		whenReady(del) { d =>
			assert(Datasource.person1.degreesOfRelationType(Datasource.Rel.OWNS, Direction.OUTBOUND) == 0)
		}
  }

  test("lazy creation of graphs") {
    val res = graph.node().map(node => node.link("LOVES", Datasource.person1))

		whenReady(res) { i =>
			assert(Datasource.person1.startNodesOfRelationType("LOVES").size == 1)
		}
  }
}
