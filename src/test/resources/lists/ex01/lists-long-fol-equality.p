fof(axiom,axiom,
    ? [BN_1,BN_2,BN_3,T] : (
      ( ! [A,B,First,First1,P] :
          ~ ( ~ (First1 = First)
            & triple(A,'http://www.example.org#first',First)
            & triple(A,'http://www.example.org#first',First1) )
      )
      & triple(T,'pre:predicate',BN_1)
      & triple(BN_1,'http://www.example.org#first','pre:a')
      & triple(BN_1,'http://www.example.org#rest',BN_2)
      & triple(BN_2,'http://www.example.org#first','pre:b')
      & triple(BN_2,'http://www.example.org#rest',BN_3)
      & triple(BN_3,'http://www.example.org#first','pre:c')
      & triple(BN_3,'http://www.example.org#rest','http://www.example.org#nil')
      & triple(BN_1,'http://www.example.org#first','pre:c')
      & triple('pre:c','http://www.w3.org/1999/02/22-rdf-syntax-ns#type','pre:cat') ) ).

fof(question,question,
    ? [A] : triple(A,'http://www.w3.org/1999/02/22-rdf-syntax-ns#type','pre:cat') ).