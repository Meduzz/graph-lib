package se.kodiak.tools.graphs

import org.scalatest.FunSuite
import model._

class GraphTest extends FunSuite {

  val graph = Graph.immutableSerial(Graph.build(Datasource.tupled()))
  val graphPar = Graph.immutable(Graph.build(Datasource.tupled()))

  test("builders does aight") {
    val edgeGraph = Graph.build(Datasource.tupled())
    val tuple = Datasource.maps()
    val mapGraph = Graph.build(tuple._1, tuple._2, tuple._3)

    assert(edgeGraph.size == Datasource.result.size)
    assert(mapGraph.size == Datasource.result.size)

    Datasource.result.foreach(e => assert(edgeGraph.contains(e)))
    Datasource.result.foreach(e => assert(mapGraph.contains(e)))
  }

  test("graphs of the third degree") {
    assert(graph.degrees(Datasource.person1, Direction.BOTH) == 4)
    assert(graphPar.degrees(Datasource.person1, Direction.BOTH) == 4)
    assert(graph.degrees(Datasource.person1, Datasource.Rel.KNOWS, Direction.INBOUND) == 2)
    assert(graphPar.degrees(Datasource.person1, Datasource.Rel.KNOWS, Direction.INBOUND) == 2)
    assert(graph.degrees(Datasource.person1, Datasource.Rel.KNOWS, Direction.OUTBOUND) == 2)
    assert(graphPar.degrees(Datasource.person1, Datasource.Rel.KNOWS, Direction.OUTBOUND) == 2)
  }

  test("so you know somebody?") {
    val outgoing = graph.outbound(Datasource.person3)

    assert(outgoing.size == 2)
    outgoing.foreach(rels => {
      assert(Seq(Datasource.knows4, Datasource.owns2).contains(rels._1))
      assert(Seq(Datasource.person1, Datasource.gadget2).contains(rels._2))
    })

    val outgoing2 = graphPar.outbound(Datasource.person3)

    assert(outgoing2.size == 2)
    outgoing2.foreach(rels => {
      assert(Seq(Datasource.knows4, Datasource.owns2).contains(rels._1))
      assert(Seq(Datasource.person1, Datasource.gadget2).contains(rels._2))
    })
  }

  test("who's your master?") {
    val owner = graph.inbound(Datasource.gadget1, Datasource.Rel.OWNS)
    assert(owner.contains(Datasource.person2))
    assert(owner.size == 1)

    val owner2 = graphPar.inbound(Datasource.gadget1, Datasource.Rel.OWNS)
    assert(owner2.contains(Datasource.person2))
    assert(owner2.size == 1)
  }

  test("do you guys know each other?") {
    val relations = graph.relation(Datasource.person2, Datasource.person3, Direction.BOTH)
    assert(relations.isEmpty)

    val relations2 = graphPar.relation(Datasource.person2, Datasource.person3, Direction.BOTH)
    assert(relations2.isEmpty)
  }
}
