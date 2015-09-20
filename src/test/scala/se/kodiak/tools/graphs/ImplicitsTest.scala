package se.kodiak.tools.graphs

import org.scalatest.FunSuite
import se.kodiak.tools.graphs.model._
import Implicits._

class ImplicitsTest extends FunSuite {
  val graph = Graph.mutableSerial(Graph.build(Datasource.edges))
  val graphPar = Graph.mutable(Graph.build(Datasource.edges))

  test("let there be mutators") {
    graph.add(Datasource.person2, new Relation(7L, Datasource.Rel.KNOWS), Datasource.person3)
    graphPar.add(Datasource.person2, new Relation(7L, Datasource.Rel.KNOWS), Datasource.person3)

    val knows = Datasource.person2.`-[]->`(Datasource.Rel.KNOWS)(graph)
    assert(knows.size == 2)
    assert(knows.contains(Datasource.person3))
    assert(knows.contains(Datasource.person1))

    val knows2 = Datasource.person2.`-[]->`(Datasource.Rel.KNOWS)(graphPar)
    assert(knows2.size == 2)
    assert(knows2.contains(Datasource.person3))
    assert(knows2.contains(Datasource.person1))
  }

  test("playing around with implicit explicits") {
    Datasource.gadget2.`--`(graph)
    assert(Datasource.person3.`-[o]->`(Datasource.Rel.OWNS)(graph) == 0)
    Datasource.person1.`++[`(Relation(8L, Datasource.Rel.OWNS), Datasource.gadget2)(graph)
    assert(Datasource.person1.`-[o]->`(Datasource.Rel.OWNS)(graph) == 1)

    Datasource.gadget2.`--`(graphPar)
    assert(Datasource.person3.`-[o]->`(Datasource.Rel.OWNS)(graphPar) == 0)
    Datasource.person1.`++[`(Relation(8L, Datasource.Rel.OWNS), Datasource.gadget2)(graphPar)
    assert(Datasource.person1.`-[o]->`(Datasource.Rel.OWNS)(graphPar) == 1)
  }

  test("moar mutators") {
    val relations = Datasource.person1.`-[X]->`(Datasource.gadget2)(graph)
    relations.foreach(_.--(graph))
    assert(Datasource.person1.`-[o]->`(Datasource.Rel.OWNS)(graph) == 0)

    val relations2 = Datasource.person1.`-[X]->`(Datasource.gadget2)(graphPar)
    relations2.foreach(_.--(graphPar))
    assert(Datasource.person1.`-[o]->`(Datasource.Rel.OWNS)(graphPar) == 0)
  }
}
