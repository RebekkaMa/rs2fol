fof(axiom,axiom,
   ? [BN_1,BN_2] : (
      triple('http://example.org/stuff/1.0/a','http://example.org/stuff/1.0/b',BN_2)
      & triple(BN_1,'http://www.w3.org/1999/02/22-rdf-syntax-ns#first','"banana"^^http://www.w3.org/2001/XMLSchema#string')
      & triple(BN_1,'http://www.w3.org/1999/02/22-rdf-syntax-ns#rest','http://www.w3.org/1999/02/22-rdf-syntax-ns#nil')
      & triple(BN_2,'http://www.w3.org/1999/02/22-rdf-syntax-ns#first','"apple"^^http://www.w3.org/2001/XMLSchema#string')
      & triple(BN_2,'http://www.w3.org/1999/02/22-rdf-syntax-ns#rest',BN_1)
   )
).