package se.kodiak.tools.graphs

import java.io.Serializable

trait IdGenerator {
	def generate():Serializable
}
