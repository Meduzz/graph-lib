package se.kodiak.tools.graphs

import se.kodiak.tools.graphs.model._

import scala.collection.immutable.Map
import scala.concurrent.Future

trait RelationDelegate {

	def relationIdGenerator:IdGenerator

	def relation(rel: Relationship, data: String):Future[DataRelation] = save(DataRelation(relationIdGenerator.generate(), rel, data))
	def relation(rel: Relationship, data: Map[String, String]):Future[HashRelation] = save(HashRelation(relationIdGenerator.generate(), rel, data))
	def relation(rel: Relationship, data: Seq[String]):Future[ListRelation] = save(ListRelation(relationIdGenerator.generate(), rel, data))

	def relation(id: RelationId, rel: Relationship, data: String):Future[DataRelation] = save(DataRelation(id, rel, data))
	def relation(id: RelationId, rel: Relationship, data: Map[String, String]):Future[HashRelation] = save(HashRelation(id, rel, data))
	def relation(id: RelationId, rel: Relationship, data: Seq[String]):Future[ListRelation] = save(ListRelation(id, rel, data))

	def save[T <: Relation](node:T):Future[T]

	def loadDataRelation(id:RelationId):Future[DataRelation]
	def loadHashRelation(id:RelationId):Future[HashRelation]
	def loadListRelation(id:RelationId):Future[ListRelation]

	def delete(node:Relation):Future[Unit]
}
