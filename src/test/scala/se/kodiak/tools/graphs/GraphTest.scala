package se.kodiak.tools.graphs

import org.scalatest.FunSuite
import model._
import se.kodiak.tools.graphs.graphsources.InMemoryGraphSource

class GraphTest extends FunSuite {

  val graph = Graph(InMemoryGraphSource.build(Datasource.tupled(), Datasource.nodeIndex, Datasource.relationIndex))

  test("graphs of the third degree") {
    assert(graph.degrees(Datasource.person1, Direction.BOTH) == 4)
    assert(graph.degrees(Datasource.person1, Datasource.Rel.KNOWS, Direction.INBOUND) == 2)
    assert(graph.degrees(Datasource.person1, Datasource.Rel.KNOWS, Direction.OUTBOUND) == 2)
  }

  test("so you know somebody?") {
    val outgoing = graph.outbound(Datasource.person3)

    assert(outgoing.size == 2)
    outgoing.foreach(rels => {
      assert(Seq(Datasource.knows4, Datasource.owns2).contains(rels._1))
      assert(Seq(Datasource.person1, Datasource.gadget2).contains(rels._2))
    })
  }

  test("who's your master?") {
    val owner = graph.inbound(Datasource.gadget1, Datasource.Rel.OWNS)
    assert(owner.contains(Datasource.person2))
    assert(owner.size == 1)
  }

  test("do you guys know each other?") {
    val relations = graph.relation(Datasource.person2, Datasource.person3, Direction.BOTH)
    assert(relations.isEmpty)
  }
}
