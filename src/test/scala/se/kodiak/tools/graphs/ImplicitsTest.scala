package se.kodiak.tools.graphs

import org.scalatest.FunSuite
import org.scalatest.concurrent.ScalaFutures
import se.kodiak.tools.graphs.Implicits._
import se.kodiak.tools.graphs.edge.EdgeStorage
import se.kodiak.tools.graphs.edge.delegate.InMemoryStorageDelegate
import se.kodiak.tools.graphs.model._

class ImplicitsTest extends FunSuite with ScalaFutures {

	implicit val edges = EdgeStorage(InMemoryStorageDelegate(Datasource.prebuilt()))
  implicit val graph:Graph = Graph(edges)

  test("let there be mutators") {
    val link = Datasource.person2.link(Relation("7", Datasource.Rel.KNOWS), Datasource.person3)
		val knows = Datasource.person2.endNodesOfRelationType(Datasource.Rel.KNOWS)(graph)

		assert(knows.size == 2)
		assert(knows.contains(Datasource.person3))
		assert(knows.contains(Datasource.person1))
  }

  test("playing around with implicit explicits") {
    Datasource.gadget2.findRelationsToNode(Datasource.person3, Direction.INBOUND).foreach(_.delete)
    assert(Datasource.person3.degreesOfRelationType(Datasource.Rel.OWNS, Direction.OUTBOUND) == 0)
    Datasource.person1.link(Relation("8", Datasource.Rel.OWNS), Datasource.gadget2)
    assert(Datasource.person1.degreesOfRelationType(Datasource.Rel.OWNS, Direction.OUTBOUND) == 1)
  }

  test("moar mutators") {
    val relations = Datasource.person1.outgoingRelations()
    relations.map(_._1).foreach(_.delete)

		assert(Datasource.person1.degreesOfRelationType(Datasource.Rel.OWNS, Direction.OUTBOUND) == 0)
  }

  test("lazy creation of graphs") {
		val rel = "LOVES"
		val relation = Relation("10", rel)

		Datasource.person3.link(relation, Datasource.person2)

		assert(Datasource.person2.startNodesOfRelationType(rel).size == 1)
	}
}
