# rs2fol

This repository is a tool for RDF surface reasoning using FOL theorem provers.

For my tests and sample data, I used [EYE](https://github.com/eyereasoner/eye) and [EYE's RDF Surfaces examples](https://github.com/eyereasoner/rdfsurfaces-tests).

## Executables

Every release contains executables of the project for

- Windows
- MacOS
- Linux

You can find these executables in the release section.

## Source Build

If you want to build the project for yourself, you have to do the following steps (conducted on linux):

1. Build [Vampire](https://github.com/vprover/vampire) 
   - This is necessary if you want to use the Vampire dependent subcommands `check` and `transform-qa`
   - To have access to the question answering feature of Vampire (which is necessary for the `transform-qa` subcommand), you have to build Vampire from the [qa-Branch](https://github.com/vprover/vampire/tree/qa) of the Vampire repository.
2. Download this repository
3. Build rs2fol with make:
    ```Bash
    $ make
    ```
4. You now have an executable
   ```Bash
   $ ls bin/
   rs2fol  rs2fol.bat
   ```

## Some notes to some subcommands

### check

This subcommand performs the following steps:
1. transforms the given RDF surface into a FOL formula without the query surface 
2. transforms the consequence into a FOL formula
3. negates the consequence FOL formula
4. combines the consequence FOL formula and the RDF surface FOL formula
5. starts a Vampire process and passes the result of step 4 for satisfiability checking
6. receives the output of the Vampire process and returns its result (true (unsat) or false (sat))
   - **true** -> the entered consequence is indeed a consequence of the RDF surface
   - **false** -> the entered consequence is not a consequence of the RDF surface

### qa-answer-to-rs (experimental)

This subcommand transforms a FOL question answering result into an RDF surface by replacing all blank nodes that are coreferences to the blank node graffiti defined on the given query surface.

rs2fol currently supports only the 'Tuple Answer Form' specified in https://www.tptp.org/TPTP/Proposals/AnswerExtraction.html. <br>
Following input types are possible:
1.  `--input-type raw`
    - accepts only one answer tuple list
    - Examples
      - `[['http://example.com/abc','"123"^^http://www.w3.org/2001/XMLSchema#string'],['http://example.com/abc','"123"^^http://www.w3.org/2001/XMLSchema#string']|_]`
      - `[[list('http://example.com/abc',list('http://example.com/abc','http://www.w3.org/1999/02/22-rdf-syntax-ns#nil'))]]`
2.  `--input-type szs`
    - accepts multiline input, extracts the answer tuple list from all lines containing 'SZS answers Tuple' and combines the results (this is especially useful for the combined use of Vampire's cascade mode)
    - Examples
      - ```% SZS answers Tuple [['http://example.com/abc','"123"^^http://www.w3.org/2001/XMLSchema#string'],['http://example.com/abc','"123"^^http://www.w3.org/2001/XMLSchema#string']|_] for ANS001+1```
      - ```% SZS answers Tuple [[list('http://example.com/abc',list('http://example.com/abc','http://www.w3.org/1999/02/22-rdf-syntax-ns#nil'))]] for ANS001+1```



All variables/blank nodes should be interpreted as universally quantified variables. 
Skolem functions (sK) are also transformed into variables, but these variables should be interpreted as existentially quantified variables. 
This way of quantification of all these variables is not implemented yet. They appear to be existentially quantified.

 Only the following structure types of answer elements are supported:
  - Literals
    - ```'"123"^^http://www.w3.org/2001/XMLSchema#integer'```
    - ```'"cat"@en'```
  - Skolem functions
    - ```sK```
    - ```sK2('http://example.com/abc','http://example.com/efg')```
  - IRIs
    - ```'http://example.com/abc'```
  - Collections
    - ```'http://www.w3.org/1999/02/22-rdf-syntax-ns#nil'```
    - ```list('http://example.com/abc',list('http://example.com/efg', list('http://example.com/abc',list('http://example.com/b','http://www.w3.org/1999/02/22-rdf-syntax-ns#nil'))))```
  - Blank Nodes / Variables
    - ```BN_1```
    - ```Var```

### transform-qa (experimental)

This subcommand performs the following steps:
1. transforms the given RDF surface into a FOL formula
2. starts a Vampire process and passes the FOL formula for question answering
3. receives the output of the Vampire process
4. transforms the output into an RDF surface using the query surface within the input RDF surface

This means that the command 
```Bash
$ ./bin/rs2fol transform-qa -i examples/introduction/abcd.n3s -q --vampire-exec $PATH_TO_VAMPIRE
```
can be seen as an abbreviation for:

```Bash
$ ./bin/rs2fol transform -i - < examples/introduction/abcd.n3s | $PATH_TO_VAMPIRE -av off -qa answer_literal -om smtcomp -t 60s 2>&1 | ./bin/rs2fol qa-answer-to-rs -q examples/introduction/abcd.n3s  -i - 
```

So if you want to use Vampire with other options or another FOL theorem prover, you can replace the middle part with the wanted command.

But you have to keep in mind that this programm is currently optimized only for Vampire and its input and output.
This means that the subcommand `qa-answer-to-rs` can only extract and transform answer tuples as described before.
If you want to use another FOL theorem prover and this programm doesn't accept its output, please let me know so that I can further extend the acceptance range and/or fix possible bugs.

#### Caution
If no answers are found, it doesn't mean there are none. Try to choose other options for Vampire.
If you use another FOL theorem prover, check if it returns results without passing them directly to rs2fol.
Maybe rs2fol just doesn't support the output format of the theorem prover, doesn't recognize the answers as answers, and ignores them.

I had the most success with these 3 option combinations:

```Bash
$ ./bin/rs2fol transform-qa -i examples/introduction/abcd.n3s --vampire-exec $PATH_TO_VAMPIRE -q --vampire-option-mode 0
# which is the short form of
$ ./bin/rs2fol transform -i examples/introduction/abcd.n3s | $PATH_TO_VAMPIRE -av off -qa answer_literal -om smtcomp -t 60s 2>&1 | ./bin/rs2fol qa-answer-to-rs -q examples/introduction/abcd.n3s  -i - 

```

```Bash
$ ./bin/rs2fol transform-qa -i examples/introduction/abcd.n3s --vampire-exec $PATH_TO_VAMPIRE -q --vampire-option-mode 1
# which is the short form of
$ ./bin/rs2fol transform -i examples/introduction/abcd.n3s | $PATH_TO_VAMPIRE -av off -sa discount -s 1 -add large -afp 4000 -afq 1.0 -anc none -gs on -gsem off -inw on -lcm reverse -lwlo on -nm 64 -nwc 1 -sas z3 -sos all -sac on -thi all -uwa all -updr off -uhcvi on -to lpo -qa answer_literal -om smtcomp -t 60s 2>&1 | ./bin/rs2fol qa-answer-to-rs -q examples/introduction/abcd.n3s  -i - 
```

```Bash
# the process will not be terminated before the specified time limit expires (here '15s')
$ ./bin/rs2fol transform -i examples/introduction/abcd.n3s | $PATH_TO_VAMPIRE -av off -uhcvi on -qa answer_literal --mode casc -t 15s 2>&1 | ./bin/rs2fol qa-answer-to-rs -q examples/introduction/abcd.n3s  -i - 
```

It is planned to automate this.


## Further notes

### Encoding

The TPTP syntax allows only a limited set of characters for the representation of variables and constants.
Therefore, characters that are not allowed are encoded during transformation into a FOL formula.
Since the choice of allowed characters is tiny, two different encoding types are used, which, for lack of options, are not standardized.
However, both encodings use the UTF-16 encoding of a character.
UTF-32 codepoints that go beyond this range are represented as usual with two UTF-16 characters (high surrogate, low surrogate).

1. \\\\u####
    - \# ... hexadecimal number
2. Ox####
   - \# ... hexadecimal number
   - **Caution** 
     - The character before the "x" is an O and not the number 0

