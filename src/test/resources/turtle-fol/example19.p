fof(axiom,axiom,
   ? [BN_1] : (
      triple('http://www.w3.org/TR/rdf-syntax-grammar','http://purl.org/dc/elements/1.1/title','"RDF/XML Syntax Specification (Revised)"^^http://www.w3.org/2001/XMLSchema#string')
      & triple('http://www.w3.org/TR/rdf-syntax-grammar','http://example.org/stuff/1.0/editor',BN_1)
      & triple(BN_1,'http://example.org/stuff/1.0/fullname','"Dave Beckett"^^http://www.w3.org/2001/XMLSchema#string')
      & triple(BN_1,'http://example.org/stuff/1.0/homePage','http://purl.org/net/dajobe/')
   )
).