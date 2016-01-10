package se.kodiak.tools.graphs

import org.scalatest.FunSuite
import se.kodiak.tools.graphs.graphsources.SeqBasedInMemory
import se.kodiak.tools.graphs.model._
import Implicits._

class ImplicitsTest extends FunSuite {
  val graph = Graph(new SeqBasedInMemory(Datasource.prebuilt()))

  test("let there be mutators") {
    graph.add(Datasource.person2, Relation(7L, Datasource.Rel.KNOWS), Datasource.person3)
    val knows = Datasource.person2.endNodesOfRelationType(Datasource.Rel.KNOWS)(graph)
    assert(knows.size == 2)
    assert(knows.contains(Datasource.person3))
    assert(knows.contains(Datasource.person1))

  }

  test("playing around with implicit explicits") {
    Datasource.gadget2.findRelationsToNode(Datasource.person3, Direction.INBOUND)(graph).foreach(_.remove(graph))
    assert(Datasource.person3.degreesOfRelationType(Datasource.Rel.OWNS, Direction.OUTBOUND)(graph) == 0)
    Datasource.person1.link(Relation(8L, Datasource.Rel.OWNS), Datasource.gadget2)(graph)
    assert(Datasource.person1.degreesOfRelationType(Datasource.Rel.OWNS, Direction.OUTBOUND)(graph) == 1)
  }

  test("moar mutators") {
    val relations = Datasource.person1.outgoingRelations()(graph)
    relations.map(_._1).foreach(_.remove(graph))
    assert(Datasource.person1.degreesOfRelationType(Datasource.Rel.OWNS, Direction.OUTBOUND)(graph) == 0)
  }
}
