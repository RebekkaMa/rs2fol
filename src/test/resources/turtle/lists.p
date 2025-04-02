fof(axiom,axiom,
   ? [T,M,BN_1] : (
      triple(T,'http://example.org/foopredicate',list('http://example.org/fooA','http://example.org/fooB','http://example.org/fooC'))
      & ? [T] : (
         triple('http://example.org/foosubject','http://example.org/foopredicate2',list)
      )
      & ! [T,O] :  ~(
         triple('http://example.org/foosubject','http://example.org/foopredicate2',list('http://example.org/fooC','http://example.org/fooF',list('http://example.org/fooL',M,BN_1)))
         & triple(BN_1,'http://example.org/fooA','http://example.org/fooC')
      )
   )
).