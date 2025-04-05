# rs2fol

**rs2fol** is a reasoning tool that transforms RDF surfaces into first-order logic (FOL) and performs entailment checking using external theorem provers, following the informal semantics of the [RDF Surface primer](https://w3c-cg.github.io/rdfsurfaces/).

The repository contains test cases that were directly taken from the [RDF Surfaces Community Group](https://www.w3.org/community/rdfsurfaces/) and can be found at the GitHub repository [RDF Surfaces tests](https://github.com/eyereasoner/rdfsurfaces-tests).

## Features

- Translates RDF surfaces into TPTP annotated first-order formulas
- Satisfiability checking using FOL provers
- Consequence checking using FOL provers
    - Supports D-entailment (by canonicalisation of literals (lexical values & datatypes)) and simple entailment
- Provides experimental support for the negative answer surface using FOL answer extraction

## Installation

### Executables

Every release contains executables of the project for

- Windows
- MacOS
- Linux

You can find these executables in the release section.

### Build from Source

To build the project from source, you need to follow these steps:

```bash
git clone https://github.com/RebekkaMa/rs2fol.git
cd rs2fol
make
```

The CLI executable will be in `bin/`:

```bash
./bin/rs2fol --help
```

## Theorem Prover Integration

For subcommands like `check` and `transform-qa`, an external FOL theorem prover must be installed.

### Requirements
- [TPTP format](https://tptp.org/UserDocs/TPTPLanguage/SyntaxBNF.html) support as input
- [SZS Ontology](https://tptp.org/UserDocs/SZSOntology/) support as output
- For `transform-qa`: Answer extraction support using SZS Ontology together with the [tuple answer form](https://tptp.org/Proposals/AnswerExtraction.html)

I recommend [Vampire](https://github.com/vprover/vampire), built from the [`qa` branch](https://github.com/vprover/vampire/tree/qa) to support answer extraction (`transform-qa`). [eprover](https://www.eprover.org/) was also tested together with rs2fol and supports answer extraction as well.

Create a `config.json` to configure executable paths and options:

```json
{
  "programs": {
    "vampire": {
      "exe": "path/to/vampire",
      "options": [
        {
          "optionId": 0,
          "flags": ["-t", "${timeLimit}s"]
        },
        {
          "optionId": 1,
          "flags": ["--mode", "casc", "--cores", "0", "-t", "${timeLimit}s"]
        }
      ]
    },
    "eprover": {
      "exe": "/path/to/eprover",
      "options": [
        {
          "optionId": 0,
          "flags": ["--soft-cpu-limit=${timeLimit}"]
        }
      ]
    }
  }
}
```
An example configuration file with settings that yielded the best results during internal evaluations of rs2fol can be found here: [exampleConfig.json](exampleConfig.json).

## Usage

### Transform

This subcommand transforms the RDF surface (`--input`, `-i`) into a TPTP annotated first-order formula `<fof_annotated>` of type `axiom`.

```bash
./bin/rs2fol transform -i path/to/rdfSurface.n3s
```

### Rewrite

This subcommand parses an RDF surface (`--input`, `-i`) and rewrites it into a more readable and standardized format. It can be useful for debugging purposes or for canonicalizing literals (when using `--d-entailment`).

```bash
./bin/rs2fol rewrite -i path/to/rdfSurface.n3s
```

### Check

#### Entailment checking

This subcommand performs the following steps:
1. Transforms the given RDF surface (`--input`, `-i`) into a TPTP annotated first-order formula `<fof_annotated>` of type `axiom`, excluding any query or negative answer surfaces on the default surface. Nested query/negative answer surfaces will be interpreted as negative surfaces.
2. Transforms the consequence RDF surface (`--consequence`, `-c`) into a TPTP annotated formula of type `conjecture`
3. Combines the `conjecture` and the `axiom`
4. Starts a theorem prover process and passes the result of step 3 for entailment checking
5. Receives and interprets the output of the theorem prover

Possible results:
- **consequence** → the consequence follows from the RDF surface
- **no consequence** → the consequence does not follow
- **contradiction** → the axioms are unsatisfiable
- **timeout** → the time limit was exceeded

```bash
./bin/rs2fol check -i path/to/antecedent.n3s -c path/to/consequence.n3s --program vampire --option-id 0 --config /path/to/config.json
```

```bash
./bin/rs2fol check -i path/to/antecedent.n3s -c path/to/consequence.n3s --program vampire --option-id 0 --config /path/to/config.json --d-entailment
```

#### Satisfiability checking

This subcommand performs the following steps:
1. Transforms the RDF surface (`--input`, `-i`) into a TPTP annotated first-order formula `<fof_annotated>` of type `axiom`, excluding any query or negative answer surfaces on the default surface. Nested query/negative answer surfaces will be interpreted as negative surfaces.
2. Starts a theorem prover process and passes the `axiom` for satisfiability checking
3. Receives and interprets the output of the theorem prover

Possible results:
- **satisfiable** → the RDF surface is satisfiable
- **unsatisfiable** → the RDF surface is not satisfiable
- **timeout** → the time limit was exceeded

```bash
./bin/rs2fol check -i path/to/antecedent.n3s --program vampire --option-id 0 --config /path/to/config.json
```

Because no consequence surface is provided, this checks the satisfiability of the input RDF surface.

### Transform with Question Answering (experimental)

Simulates the behavior of the negative answer surface. Only supported on the default surface, not nested ones.

Steps:
1. Transforms the RDF surface (`--input`, `-i`) into a TPTP annotated first-order formula `<fof_annotated>` of type `axiom`, excluding any query or negative answer surfaces on the default surface. Nested query/negative answer surfaces will be interpreted as negative surfaces.
2. transforms the query/negative answer surface into a TPTP annotated first order formula <fof_annotated> of type question
3. starts a theorem prover process and passes both first order formula for answer extraction
4. receives the output triples of the theorem prover process
5. transforms the output triples (in tuple answer form) into an RDF surface using the query/negative answer surface within the input RDF surface

```bash
./bin/rs2fol transform-qa --program vampire-qa --option-id 2 -t 5 -i path/to/surface.n3s --config /path/to/config.json
```

#### Caution
Lack of results does not imply lack of answers. Poor prover configuration can lead to missing or partial answers. Best results were obtained using Vampire with `--mode casc`. This mode usually does not terminate, so setting a timeout (e.g., 5s) is recommended. See the sample `config.json`.

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

## Encoding Notes

The TPTP syntax allows only a limited set of characters for the representation of variables and constants.
Therefore, characters that are not allowed are encoded during transformation into a FOL formula.
Since the choice of allowed characters is tiny, two different encoding types are used, which, for lack of options, are not standardized.
However, both encodings use the UTF-16 encoding of a character.
UTF-32 codepoints that go beyond this range are represented as usual with two UTF-16 characters (high surrogate, low surrogate).

1. `\u####` – UTF-16 hex code
2. `Ox####` – same, with `O` (not `0`) prefix

The encoding can be deactivated by using the `--no-enc` option. This is useful for debugging purposes, but the output may not be valid TPTP syntax.

## Architecture

The project follows the principles of [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) as proposed by [Robert C. Martin](https://de.wikipedia.org/wiki/Robert_Cecil_Martin).
