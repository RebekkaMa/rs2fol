fof(axiom,axiom,
   ? [Ox0061,Ox0062,Ox0063] : (
      triple(Ox0061,'http://xmlns.com/foaf/0.1/name','"Alice"^^http://www.w3.org/2001/XMLSchema#string')
      & triple(Ox0061,'http://xmlns.com/foaf/0.1/knows',Ox0062)
      & triple(Ox0062,'http://xmlns.com/foaf/0.1/name','"Bob"^^http://www.w3.org/2001/XMLSchema#string')
      & triple(Ox0062,'http://xmlns.com/foaf/0.1/knows',Ox0063)
      & triple(Ox0063,'http://xmlns.com/foaf/0.1/name','"Eve"^^http://www.w3.org/2001/XMLSchema#string')
      & triple(Ox0062,'http://xmlns.com/foaf/0.1/mbox','file:///home/rebekka/Nextcloud/Studium/SS23/untitled1/src/test/resources/turtle/bob@example.com')
   )
).