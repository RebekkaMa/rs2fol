package rdfSurface

abstract class RDFSurface(
    val graffiti: List<BlankNode>,
    val hayesGraph: List<HayesGraphElement>
) : HayesGraphElement()

class PositiveRDFSurface(graffiti: List<BlankNode>, hayesGraph: List<HayesGraphElement>) :
    RDFSurface(graffiti, hayesGraph)

class NegativeRDFSurface(graffiti: List<BlankNode>, hayesGraph: List<HayesGraphElement>) :
    RDFSurface(graffiti, hayesGraph)

class QueryRDFSurface(graffiti: List<BlankNode>, hayesGraph: List<HayesGraphElement>) :
    RDFSurface(graffiti, hayesGraph)

class NeutralRDFSurface(graffiti: List<BlankNode>, hayesGraph: List<HayesGraphElement>) :
    RDFSurface(graffiti, hayesGraph)

class NegativeTripleRDFSurface(graffiti: List<BlankNode>, hayesGraph: List<HayesGraphElement>) :
    RDFSurface(graffiti, hayesGraph)