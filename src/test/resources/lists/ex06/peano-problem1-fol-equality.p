fof(axiom,axiom,
    ? [BN_1,BN_2,BN_3,BN_4] :
      ( triple(BN_1,'http://example.org/ns#p',BN_2)
      & ! [A,B,First,Rest] :
          ~ ( A != B
            & triple(A,'http://www.example.org#first',First)
            & triple(A,'http://www.example.org#rest',Rest)
            & triple(B,'http://www.example.org#first',First)
            & triple(B,'http://www.example.org#rest',Rest) )
      & triple(BN_1,'http://www.w3.org/1999/02/22-rdf-syntax-ns#first','http://example.org/ns#a')
      & triple(BN_1,'http://www.w3.org/1999/02/22-rdf-syntax-ns#rest','http://www.w3.org/1999/02/22-rdf-syntax-ns#nil')
      & triple(BN_2,'http://www.w3.org/1999/02/22-rdf-syntax-ns#first','http://example.org/ns#b')
      & triple(BN_2,'http://www.w3.org/1999/02/22-rdf-syntax-ns#rest','http://www.w3.org/1999/02/22-rdf-syntax-ns#nil')
      & triple(BN_3,'http://example.org/ns#p',BN_4)
      & triple(BN_3,'http://www.w3.org/1999/02/22-rdf-syntax-ns#first','http://example.org/ns#a')
      & triple(BN_3,'http://www.w3.org/1999/02/22-rdf-syntax-ns#rest','http://www.w3.org/1999/02/22-rdf-syntax-ns#nil')
      & triple(BN_4,'http://www.w3.org/1999/02/22-rdf-syntax-ns#first','http://example.org/ns#b')
      & triple(BN_4,'http://www.w3.org/1999/02/22-rdf-syntax-ns#rest','http://www.w3.org/1999/02/22-rdf-syntax-ns#nil') ) ).

fof(question0,question,
    ? [BN_5,BN_6] :
      ( triple(BN_5,'http://example.org/ns#p',BN_6)
      & triple(BN_5,'http://www.w3.org/1999/02/22-rdf-syntax-ns#first','http://example.org/ns#a')
      & triple(BN_5,'http://www.w3.org/1999/02/22-rdf-syntax-ns#rest','http://www.w3.org/1999/02/22-rdf-syntax-ns#nil')
      & triple(BN_6,'http://www.w3.org/1999/02/22-rdf-syntax-ns#first','http://example.org/ns#b')
      & triple(BN_6,'http://www.w3.org/1999/02/22-rdf-syntax-ns#rest','http://www.w3.org/1999/02/22-rdf-syntax-ns#nil') ) ).