fof(axiom,axiom,
   triple('http://example.org/stuff/1.0/a','http://example.org/stuff/1.0/b','"The first line\\u005C;nThe second line\\u005C;n  more"^^http://www.w3.org/2001/XMLSchema#string')
   & triple('http://example.org/stuff/1.0/a','http://example.org/stuff/1.0/b','"The first line\\u000A;The second line\\u000A;  more"^^http://www.w3.org/2001/XMLSchema#string')
).