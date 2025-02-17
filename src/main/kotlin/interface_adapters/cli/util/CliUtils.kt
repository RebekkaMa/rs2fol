package interface_adapters.cli.util

import entities.rdfsurfaces.rdf_term.IRI

val workingDir = IRI.from("file://" + System.getProperty("user.dir") + "/")
