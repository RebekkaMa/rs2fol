fof(axiom,axiom,
   ? [A,B,C] : (
      triple(A,'http://xmlns.com/foaf/0.1/name','"Alice"^^http://www.w3.org/2001/XMLSchema#string')
      & triple(A,'http://xmlns.com/foaf/0.1/knows',B)
      & triple(B,'http://xmlns.com/foaf/0.1/name','"Bob"^^http://www.w3.org/2001/XMLSchema#string')
      & triple(B,'http://xmlns.com/foaf/0.1/knows',C)
      & triple(C,'http://xmlns.com/foaf/0.1/name','"Eve"^^http://www.w3.org/2001/XMLSchema#string')
      & triple(B,'http://xmlns.com/foaf/0.1/mbox','bob@example.com')
   )
).