fof(axiom,axiom,(triple(list2('"0"^^http://www.w3.org/2001/XMLSchema#integer','"0"^^http://www.w3.org/2001/XMLSchema#integer'),'http://example.org/ns#and','"0"^^http://www.w3.org/2001/XMLSchema#integer') & triple(list2('"0"^^http://www.w3.org/2001/XMLSchema#integer','"1"^^http://www.w3.org/2001/XMLSchema#integer'),'http://example.org/ns#and','"0"^^http://www.w3.org/2001/XMLSchema#integer') & triple(list2('"1"^^http://www.w3.org/2001/XMLSchema#integer','"0"^^http://www.w3.org/2001/XMLSchema#integer'),'http://example.org/ns#and','"0"^^http://www.w3.org/2001/XMLSchema#integer') & triple(list2('"1"^^http://www.w3.org/2001/XMLSchema#integer','"1"^^http://www.w3.org/2001/XMLSchema#integer'),'http://example.org/ns#and','"1"^^http://www.w3.org/2001/XMLSchema#integer') & triple(list2('"0"^^http://www.w3.org/2001/XMLSchema#integer','"0"^^http://www.w3.org/2001/XMLSchema#integer'),'http://example.org/ns#or','"0"^^http://www.w3.org/2001/XMLSchema#integer') & triple(list2('"0"^^http://www.w3.org/2001/XMLSchema#integer','"1"^^http://www.w3.org/2001/XMLSchema#integer'),'http://example.org/ns#or','"1"^^http://www.w3.org/2001/XMLSchema#integer') & triple(list2('"1"^^http://www.w3.org/2001/XMLSchema#integer','"0"^^http://www.w3.org/2001/XMLSchema#integer'),'http://example.org/ns#or','"1"^^http://www.w3.org/2001/XMLSchema#integer') & triple(list2('"1"^^http://www.w3.org/2001/XMLSchema#integer','"1"^^http://www.w3.org/2001/XMLSchema#integer'),'http://example.org/ns#or','"1"^^http://www.w3.org/2001/XMLSchema#integer') & triple('"0"^^http://www.w3.org/2001/XMLSchema#integer','http://example.org/ns#inv','"1"^^http://www.w3.org/2001/XMLSchema#integer') & triple('"1"^^http://www.w3.org/2001/XMLSchema#integer','http://example.org/ns#inv','"0"^^http://www.w3.org/2001/XMLSchema#integer') & ! [D,Q] : ~(~(triple(list3(D,'"0"^^http://www.w3.org/2001/XMLSchema#integer',Q),'http://example.org/ns#dff',Q))) & ! [D,Q] : ~(~(triple(list3(D,'"1"^^http://www.w3.org/2001/XMLSchema#integer',Q),'http://example.org/ns#dff',D))) & ! [A,B,Q,T1,NA,NB,T2] : ~(~(triple(list2(A,B),'http://example.org/ns#neta',Q)) & triple(list2(A,B),'http://example.org/ns#and',T1) & triple(A,'http://example.org/ns#inv',NA) & triple(B,'http://example.org/ns#inv',NB) & triple(list2(NA,NB),'http://example.org/ns#and',T2) & triple(list2(T1,T2),'http://example.org/ns#or',Q)) & ! [A,B,C,Q1,Q2,T1,NC,T2,NA,T3] : ~(~(triple(list3(A,B,C),'http://example.org/ns#netb',list2(Q1,Q2))) & triple(list2(A,C),'http://example.org/ns#and',T1) & triple(C,'http://example.org/ns#inv',NC) & triple(list2(B,NC),'http://example.org/ns#and',T2) & triple(A,'http://example.org/ns#inv',NA) & triple(list2(NA,C),'http://example.org/ns#and',T3) & triple(list2(T1,T2),'http://example.org/ns#or',Q1) & triple(list2(T2,T3),'http://example.org/ns#or',Q2)) & ! [C,Qa,Qb,Qc,Za,Zb,Zc,D1,D2,D3] : ~(~(triple(list2(C,list3(Qa,Qb,Qc)),'http://example.org/ns#gcc',list3(Za,Zb,Zc))) & triple(list3(Qa,Qb,Qc),'http://example.org/ns#netb',list2(D1,D2)) & triple(list2(Qa,Qb),'http://example.org/ns#neta',D3) & triple(list3(D1,C,Qa),'http://example.org/ns#dff',Za) & triple(list3(D2,C,Qb),'http://example.org/ns#dff',Zb) & triple(list3(D3,C,Qc),'http://example.org/ns#dff',Zc)) & ! [S] : ~(~(triple(list2(list0,S),'http://example.org/ns#testgcc',list0))) & ! [Cc,S,Nc,C,Cs,N,Ns] : ~(~(triple(list2(Cc,S),'http://example.org/ns#testgcc',Nc)) & triple(Cc,'http://www.w3.org/2000/10/swap/list#firstRest',list2(C,Cs)) & triple(Nc,'http://www.w3.org/2000/10/swap/list#firstRest',list2(N,Ns)) & triple(list2(C,S),'http://example.org/ns#gcc',N) & triple(list2(Cs,N),'http://example.org/ns#testgcc',Ns)))).
fof(conjecture,conjecture,(triple(list2(list9('"1"^^http://www.w3.org/2001/XMLSchema#integer','"1"^^http://www.w3.org/2001/XMLSchema#integer','"1"^^http://www.w3.org/2001/XMLSchema#integer','"1"^^http://www.w3.org/2001/XMLSchema#integer','"1"^^http://www.w3.org/2001/XMLSchema#integer','"1"^^http://www.w3.org/2001/XMLSchema#integer','"1"^^http://www.w3.org/2001/XMLSchema#integer','"1"^^http://www.w3.org/2001/XMLSchema#integer','"1"^^http://www.w3.org/2001/XMLSchema#integer'),list3('"0"^^http://www.w3.org/2001/XMLSchema#integer','"0"^^http://www.w3.org/2001/XMLSchema#integer','"0"^^http://www.w3.org/2001/XMLSchema#integer')),'http://example.org/ns#testgcc',list9(list3('"0"^^http://www.w3.org/2001/XMLSchema#integer','"0"^^http://www.w3.org/2001/XMLSchema#integer','"1"^^http://www.w3.org/2001/XMLSchema#integer'),list3('"0"^^http://www.w3.org/2001/XMLSchema#integer','"1"^^http://www.w3.org/2001/XMLSchema#integer','"1"^^http://www.w3.org/2001/XMLSchema#integer'),list3('"0"^^http://www.w3.org/2001/XMLSchema#integer','"1"^^http://www.w3.org/2001/XMLSchema#integer','"0"^^http://www.w3.org/2001/XMLSchema#integer'),list3('"1"^^http://www.w3.org/2001/XMLSchema#integer','"1"^^http://www.w3.org/2001/XMLSchema#integer','"0"^^http://www.w3.org/2001/XMLSchema#integer'),list3('"1"^^http://www.w3.org/2001/XMLSchema#integer','"1"^^http://www.w3.org/2001/XMLSchema#integer','"1"^^http://www.w3.org/2001/XMLSchema#integer'),list3('"1"^^http://www.w3.org/2001/XMLSchema#integer','"0"^^http://www.w3.org/2001/XMLSchema#integer','"1"^^http://www.w3.org/2001/XMLSchema#integer'),list3('"1"^^http://www.w3.org/2001/XMLSchema#integer','"0"^^http://www.w3.org/2001/XMLSchema#integer','"0"^^http://www.w3.org/2001/XMLSchema#integer'),list3('"0"^^http://www.w3.org/2001/XMLSchema#integer','"0"^^http://www.w3.org/2001/XMLSchema#integer','"0"^^http://www.w3.org/2001/XMLSchema#integer'),list3('"0"^^http://www.w3.org/2001/XMLSchema#integer','"0"^^http://www.w3.org/2001/XMLSchema#integer','"1"^^http://www.w3.org/2001/XMLSchema#integer'))))).