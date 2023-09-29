fof(axiom,axiom,
   ? [Ox00620,Ox00621,Ox00622] : (
      triple(Ox00620,'http://www.w3.org/1999/02/22-rdf-syntax-ns#first','"1"^^http://www.w3.org/2001/XMLSchema#integer')
      & triple(Ox00620,'http://www.w3.org/1999/02/22-rdf-syntax-ns#rest',Ox00621)
      & triple(Ox00621,'http://www.w3.org/1999/02/22-rdf-syntax-ns#first','"2"^^http://www.w3.org/2001/XMLSchema#decimal')
      & triple(Ox00621,'http://www.w3.org/1999/02/22-rdf-syntax-ns#rest',Ox00622)
      & triple(Ox00622,'http://www.w3.org/1999/02/22-rdf-syntax-ns#first','"30.0"^^http://www.w3.org/2001/XMLSchema#double')
      & triple(Ox00622,'http://www.w3.org/1999/02/22-rdf-syntax-ns#rest','http://www.w3.org/1999/02/22-rdf-syntax-ns#nil')
      & triple(Ox00620,'http://example.org/stuff/1.0/p','"w"^^http://www.w3.org/2001/XMLSchema#string')
   )
).