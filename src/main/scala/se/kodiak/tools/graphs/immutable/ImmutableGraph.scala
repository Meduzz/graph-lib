package se.kodiak.tools.graphs.immutable

import se.kodiak.tools.graphs.ParallellGraph
import se.kodiak.tools.graphs.model.Edge

import scala.collection.parallel.ParSeq

class ImmutableGraph(protected val edges:ParSeq[Edge]) extends ParallellGraph
