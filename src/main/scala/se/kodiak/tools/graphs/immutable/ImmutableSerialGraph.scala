package se.kodiak.tools.graphs.immutable

import se.kodiak.tools.graphs.SerialGraph
import se.kodiak.tools.graphs.model.Edge

class ImmutableSerialGraph(protected val edges:Seq[Edge]) extends SerialGraph
