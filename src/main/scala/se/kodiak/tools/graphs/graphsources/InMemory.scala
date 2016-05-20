package se.kodiak.tools.graphs.graphsources

import java.io.Serializable
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

import se.kodiak.tools.graphs.model._
import se.kodiak.tools.graphs._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object InMemory {

	trait InMemoryBuilder { self:InMemoryBuilderImpl =>
		def withNodeIdGen(idGen:IdGenerator):InMemoryBuilder = copy(nodeDelegate = new InMemoryNodeDelegate(idGen, nodes(edgeDelegate.edges)))
		def withRelationIdGen(idGen:IdGenerator):InMemoryBuilder = copy(relationDelegate = new InMemoryRelationDelegate(idGen, relations(edgeDelegate.edges)))
		def withNodeStartPos(pos:Long):InMemoryBuilder = withNodeIdGen(LongIdGenerator(pos))
		def withRelationStartPos(pos:Long):InMemoryBuilder = withRelationIdGen(LongIdGenerator(pos))
		def withNodeDelegate(delegate: NodeDelegate):InMemoryBuilder = copy(nodeDelegate = delegate)
		def withRelationDelegate(delegate: RelationDelegate):InMemoryBuilder = copy(relationDelegate = delegate)
		def build():Graph with Mutators = Graph(edgeDelegate, nodeDelegate, relationDelegate)

		protected def nodes(edges:Seq[Edge]):Seq[Node] = {
			edges.map(e => Seq(e.start, e.end)).flatMap(s => s)
		}

		protected def relations(edges:Seq[Edge]):Seq[Relation] = {
			edges.map(e => e.relation)
		}
	}

	object InMemoryBuilder {
		def fromData(data:Seq[Edge]):InMemoryBuilder = InMemoryBuilderImpl(new InMemoryEdgeDelegate(data), new InMemoryNodeDelegate(LongIdGenerator(0), nodes(data)), new InMemoryRelationDelegate(LongIdGenerator(0), relations(data)))
		def fromRawData(data:Seq[(VerticeId, RelationId, Relationship, VerticeId)]):InMemoryBuilder = InMemoryBuilderImpl(new InMemoryEdgeDelegate(edges(data)), new InMemoryNodeDelegate(LongIdGenerator(0), nodes(edges(data))), new InMemoryRelationDelegate(LongIdGenerator(0), relations(edges(data))))
		def fromEdgeDelegate(delegate: EdgeDelegate):InMemoryBuilder = InMemoryBuilderImpl(delegate, new InMemoryNodeDelegate(LongIdGenerator(0), nodes(delegate.edges)), new InMemoryRelationDelegate(LongIdGenerator(0), relations(delegate.edges)))

		protected def nodes(edges:Seq[Edge]):Seq[Node] = {
			edges.map(e => Seq(e.start, e.end)).flatMap(s => s)
		}

		protected def relations(edges:Seq[Edge]):Seq[Relation] = {
			edges.map(e => e.relation)
		}

		protected def edges(data:Seq[(VerticeId, RelationId, Relationship, VerticeId)]):Seq[Edge] = {
			data.map(i => Edge(LazyNode(i._1), LazyRelation(i._2, i._3), LazyNode(i._4)))
		}
	}

	case class InMemoryBuilderImpl(edgeDelegate: EdgeDelegate, nodeDelegate: NodeDelegate, relationDelegate: RelationDelegate) extends InMemoryBuilder

	object LongIdGenerator {
		def apply(start:Long = 0L):LongIdGenerator = new LongIdGenerator(start)
	}

	class LongIdGenerator(count: Long) extends IdGenerator {
		val id = new AtomicLong(count)

		override def generate(): Serializable = {
			id.incrementAndGet().asInstanceOf[Serializable]
		}
	}

	class UUIDGenerator extends IdGenerator {
		override def generate():Serializable = UUID.randomUUID().toString
	}

	class InMemoryNodeDelegate(val nodeIdGenerator: IdGenerator, nodeses: Seq[Node]) extends NodeDelegate {

		private var nodes: Map[Serializable, Node] = nodeses.map(nod => (nod.id, nod)).toMap

		override def save[T <: Node](node: T):Future[T] = Future {
			nodes = nodes ++ Map(node.id -> node)
			node
		}

		override def loadDataNode(id: VerticeId):Future[DataNode] = Future(nodes(id).asInstanceOf[DataNode])

		override def loadHashNode(id: VerticeId):Future[HashNode] = Future(nodes(id).asInstanceOf[HashNode])

		override def loadListNode(id: VerticeId):Future[ListNode] = Future(nodes(id).asInstanceOf[ListNode])

		override def delete(node: Node):Future[Unit] = {
			nodes = nodes.filterKeys(id => !id.equals(node.id))
			Future(Unit)
		}
	}

	class InMemoryRelationDelegate(val relationIdGenerator: IdGenerator, rels: Seq[Relation]) extends RelationDelegate {

		private var relations: Map[Serializable, Relation] = rels.map(rel => (rel.id, rel)).toMap

		override def save[T <: Relation](relation: T):Future[T] = Future {
			relations = relations ++ Map(relation.id -> relation)
			relation
		}

		override def loadListRelation(id: RelationId):Future[ListRelation] = Future(relations(id).asInstanceOf[ListRelation])

		override def loadDataRelation(id: RelationId):Future[DataRelation] = Future(relations(id).asInstanceOf[DataRelation])

		override def loadHashRelation(id: RelationId):Future[HashRelation] = Future(relations(id).asInstanceOf[HashRelation])

		override def delete(relation: Relation):Future[Unit] = {
			relations = relations.filterKeys(id => !id.equals(relation.id))
			Future(Unit)
		}
	}

	class InMemoryEdgeDelegate(var e: Seq[Edge]) extends EdgeDelegate {

		override def save(edge: Edge):Future[Edge] = Future(edge)

		override def delete(edge: Edge):Future[Edge] = Future(edge)

		override def initialize(): Seq[Edge] = e

	}

}