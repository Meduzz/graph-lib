package se.kodiak.tools.graphs

import model._

object Datasource {
  val person1 = Node(1L)
  val person2 = Node(2L)
  val person3 = Node(3L)
  val gadget1 = Node(4L)
  val gadget2 = Node(5L)

  val knows1 = Relation(1L, Rel.KNOWS)
  val knows2 = Relation(2L, Rel.KNOWS)
  val knows3 = Relation(3L, Rel.KNOWS)
  val knows4 = Relation(4L, Rel.KNOWS)
  val owns1 = Relation(5L, Rel.OWNS)
  val owns2 = Relation(6L, Rel.OWNS)

  val result = Seq(Edge(person1, knows1, person2),
    Edge(person1, knows2, person3),
    Edge(person2, knows3, person1),
    Edge(person3, knows4, person1),
    Edge(person2, owns1, gadget1),
    Edge(person3, owns2, gadget2))

  // this got silly...
  val out:Map[VerticeId, List[RelationId]] = mapList(1L, List(1L, 2L)) ++ mapList(2L, List(3L, 5L)) ++ mapList(3L, List(4L, 6L))
  val rels:Map[RelationId, VerticeId] = relVer(1L, 2L) ++ relVer(2L, 3L) ++ relVer(3L, 1L) ++ relVer(4L, 1L) ++ relVer(5L, 4L) ++ relVer(6L, 5L)
  val relTypes:Map[RelationId, Relationship] = relTyp(1L, Rel.KNOWS) ++ relTyp(2L, Rel.KNOWS) ++ relTyp(3L, Rel.KNOWS) ++ relTyp(4L, Rel.KNOWS) ++ relTyp(5L, Rel.OWNS) ++ relTyp(6L, Rel.OWNS)

  val edges = Seq[(VerticeId, RelationId, Relationship, VerticeId)]((1L, 1L, Rel.KNOWS, 2L),
    (1L, 2L, Rel.KNOWS, 3L),
    (2L, 3L, Rel.KNOWS, 1L),
    (3L, 4L, Rel.KNOWS, 1L),
    (2L, 5L, Rel.OWNS, 4L),
    (3L, 6L, Rel.OWNS, 5L))

  object Rel {
    val KNOWS = "KNOWS"
    val OWNS = "OWNS"
  }

  def tupled():Seq[(VerticeId, RelationId, Relationship, VerticeId)] = edges
  def maps():(Map[VerticeId, List[RelationId]], Map[RelationId, VerticeId], Map[RelationId, Relationship]) = (out, rels, relTypes)

  def mapList(key:VerticeId, list:List[RelationId]):Map[VerticeId, List[RelationId]] = Map(key -> list)
  def relVer(key:RelationId, value:VerticeId) = Map(key -> value)
  def relTyp(key:RelationId, rel:Relationship) = Map(key -> rel)
}
