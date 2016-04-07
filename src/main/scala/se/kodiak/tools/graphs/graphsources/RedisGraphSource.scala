package se.kodiak.tools.graphs.graphsources

import java.util.concurrent.TimeUnit

import redis.RedisClient
import se.kodiak.tools.graphs.GraphSource
import se.kodiak.tools.graphs.model._

import scala.collection.immutable._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object RedisGraphSource {
 def apply(redis:RedisClient, graph:String):GraphSource = new RedisGraphSource(redis, graph)
}

class RedisGraphSource(val redisClient:RedisClient, val graphName:String) extends GraphSource with KeyBuilder {

  val nodeIndex:String = "se.kodiak.tools.graphs.node"
  val relationIndex:String = "se.kodiak.tools.graphs.relation"
  val indexIndex:String = "se.kodiak.tools.graphs.index"

  val duration = Duration(3L, TimeUnit.SECONDS)

  private var internalEdges:Seq[Edge] = reloadEdges()

  override def add(start: Node, relation: Relation, end: Node): Unit = {
    val edge = Edge(start, relation, end)
    redisClient
      .sadd(indexKey, s"${edge.start.id}:${edge.relation.id}:${edge.end.id}")
      .map(long => if (long > 0) {internalEdges = internalEdges ++ Seq(edge)})

    Unit
  }

  override def node(): Node = {
    val long = for {
      long <- redisClient.incr(nodeIdIncrKey)
      node <- redisClient.set(nodeKey(long), "")
    } yield long

    val id = Await.result(long, duration)
    Node(id)
  }

  override def node(data: String): DataNode = {
    val long = for {
      long <- redisClient.incr(nodeIdIncrKey)
      node <- redisClient.set(nodeKey(long), data)
    } yield long

    val id = Await.result(long, duration)
    DataNode(id, data)
  }

  override def node(data: Map[String, String]): HashNode = {
    val long = for {
      long <- redisClient.incr(nodeIdIncrKey)
      node <- redisClient.hmset(nodeKey(long), data)
    } yield long

    val id = Await.result(long, duration)
    HashNode(id, data)
  }

  override def relation(rel: Relationship): Relation = {
    val long = for {
      long <- redisClient.incr(relationIdIncrKey)
      rel <- redisClient.set(relationKey(long), rel)
    } yield long

    val id = Await.result(long, duration)
    Relation(id, rel)
  }

  override def relation(rel: Relationship, data: String): DataRelation = {
    val long = for {
      long <- redisClient.incr(relationIdIncrKey)
      rel <- redisClient.set(relationKey(long), rel)
      data <- redisClient.set(relationDataKey(long), data)
    } yield long

    val id = Await.result(long, duration)
    DataRelation(id, rel, data)
  }

  override def relation(rel: Relationship, data: Map[String, String]): HashRelation = {
    val long = for {
      long <- redisClient.incr(relationIdIncrKey)
      rel <- redisClient.set(relationKey(long), rel)
      data <- redisClient.hmset(relationDataKey(long), data)
    } yield long

    val id = Await.result(long, duration)
    HashRelation(id, rel, data)
  }

  override def relationSeq(rel: Relationship): Relation = {
    val long = for {
      long <- redisClient.incr(relationIdIncrKey)
      rel <- redisClient.set(relationKey(long), rel)
      data <- redisClient.sadd(relationDataKey(long), "")
      ignore <- redisClient.srem(relationDataKey(long), "")
    } yield long

    val id = Await.result(long, duration)
    ListRelation(id, rel, Seq())
  }

  override def nodeSeq(): Node = {
    val long = for {
      long <- redisClient.incr(nodeIdIncrKey)
      node <- redisClient.sadd(nodeKey(long), "")
      ignore <- redisClient.srem(nodeKey(long), "")
    } yield long

    val id = Await.result(long, duration)
    ListNode(id, Seq())
  }

  override def edges: Seq[Edge] = internalEdges

  override def remove(relation: Relation): Unit = {
    val edge = internalEdges
      .filter(e => e.relation.id.equals(relation.id))
      .head

    redisClient
      .srem(indexKey, s"${edge.start.id}:${edge.relation.id}:${edge.end.id}")
      .map(long => if (long > 0) { internalEdges = internalEdges.filterNot(_.equals(edge)) })

    redisClient.del(relationDataKey(relation.id))
    redisClient.del(relationKey(relation.id))

    Unit
  }

  override def loadDataRelation(id: RelationId): DataRelation = {
    val futureRelation = for {
      rel <- redisClient.get[String](relationKey(id))
      data <- redisClient.get[String](relationDataKey(id))
    } yield DataRelation(id, rel.get, data.getOrElse(""))

    Await.result(futureRelation, duration)
  }

  override def loadHashRelation(id: RelationId): HashRelation = {
    val futureRelation = for {
      rel <- redisClient.get[String](relationKey(id))
      data <- redisClient.hgetall[String](relationDataKey(id))
    } yield HashRelation(id, rel.get, data)

    Await.result(futureRelation, duration)
  }

  override def loadDataNode(id: VerticeId): DataNode = {
    val futureNode = for {
      data <- redisClient.get[String](nodeKey(id))
    } yield DataNode(id, data.getOrElse(""))

    Await.result(futureNode, duration)
  }

  override def loadHashNode(id: VerticeId): HashNode = {
    val futureNode = for {
      data <- redisClient.hgetall[String](nodeKey(id))
    } yield HashNode(id, data)

    Await.result(futureNode, duration)
  }

  override def loadSeqNode(id: VerticeId): ListNode = {
    val futureNode = for {
      data <- redisClient.smembers[String](nodeKey(id))
    } yield ListNode(id, data)

    Await.result(futureNode, duration)
  }

  override def loadSeqRelation(id: RelationId): ListRelation = {
    val futureRelation = for {
      rel <- redisClient.get[String](relationKey(id))
      data <- redisClient.smembers[String](relationDataKey(id))
    } yield ListRelation(id, rel.get, data)

    Await.result(futureRelation, duration)
  }

  protected def removeFromIndex(edge:Edge):Future[Unit] = {
    redisClient.srem(indexKey, s"${edge.start.id}:${edge.relation.id}:${edge.end.id}")
      .map(_ => Unit)
  }

  def reloadEdges():Seq[Edge] = {
    val futureEdges = redisClient
      .smembers[String](indexKey)
      .map(items => items.map(str => {
        val Array(start:String, rel:String, end:String) = str.split(":")
        redisClient.get[String](relationKey(rel))
          .map(someRelation => someRelation.map(relation => Edge(Node(start), Relation(rel, relation), Node(end))))
      }))
      .map(a => Future.sequence(a))
      .flatMap(a => a)
      .map(edges => {
        edges.filter({
          case Some(edge) => true
          case None => false
        }).map(_.get)
      })
      .map(edges => Seq.concat(edges))

    Await.result(futureEdges, duration)
  }
}

trait KeyBuilder {

  def indexIndex:String
  def nodeIndex:String
  def relationIndex:String
  def graphName:String

  def indexKey:String = s"$indexIndex.$graphName"
  def nodeKey(id:VerticeId):String = s"$nodeIndex.$graphName.$id"
  def nodeIdIncrKey:String = s"$nodeIndex.$graphName"
  def relationKey(id:RelationId):String = s"$relationIndex.$graphName.$id"
  def relationIdIncrKey:String = s"$relationIndex.$graphName"
  def relationDataKey(id:RelationId):String = s"${relationKey(id)}.data"
}