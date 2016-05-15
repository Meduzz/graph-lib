package se.kodiak.tools.graphs

import se.kodiak.tools.graphs.model._

import scala.collection.immutable.Map

trait RelationDelegate {

	def relationIdGenerator:IdGenerator

	def relation(rel: Relationship, data: String): DataRelation = save(DataRelation(relationIdGenerator.generate(), rel, data))
	def relation(rel: Relationship, data: Map[String, String]): HashRelation = save(HashRelation(relationIdGenerator.generate(), rel, data))
	def relation(rel: Relationship, data: Seq[String]): ListRelation = save(ListRelation(relationIdGenerator.generate(), rel, data))

	def relation(id: RelationId, rel: Relationship, data: String): DataRelation = save(DataRelation(id, rel, data))
	def relation(id: RelationId, rel: Relationship, data: Map[String, String]): HashRelation = save(HashRelation(id, rel, data))
	def relation(id: RelationId, rel: Relationship, data: Seq[String]): ListRelation = save(ListRelation(id, rel, data))

	def save[T <: Relation](node:T):T

	def loadDataRelation(id:RelationId):DataRelation
	def loadHashRelation(id:RelationId):HashRelation
	def loadListRelation(id:RelationId):ListRelation

	def delete(node:Relation):Unit
}
