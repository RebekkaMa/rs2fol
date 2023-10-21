fof(axiom,axiom,
   triple('http://example.org/ns#i','http://www.w3.org/1999/02/22-rdf-syntax-ns#type','http://example.org/ns#A')
   & ~(
         triple('http://example.org/ns#i','http://www.w3.org/1999/02/22-rdf-syntax-ns#type','http://example.org/ns#C')
      )
   & ! [S] :  ~(
         triple(S,'http://www.w3.org/1999/02/22-rdf-syntax-ns#type','http://example.org/ns#A')
         & ~(
            triple(S,'http://www.w3.org/1999/02/22-rdf-syntax-ns#type','http://example.org/ns#B')
         )
         & ~(
            triple(S,'http://www.w3.org/1999/02/22-rdf-syntax-ns#type','http://example.org/ns#C')
         )
      )
).
fof(query_0,question,
   ? [S,C] : (
      triple(S,'http://www.w3.org/1999/02/22-rdf-syntax-ns#type',C)
   )
).