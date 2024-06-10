# Measurement Tests

This folder contains the results from the execution of the measurement scripts.

## Versions Used

### Vampire
- **Version:** 4.7
- **Commit:** 807e37dd9
- **Date:** 2022-08-23 09:55:27 +0200
- **Linked with Z3:** 4.8.13.0 (f03d756e086f81f2596157241e0decfb1c982299 z3-4.8.4-5390-gf03d756e0)

### EYE
- **Version:** v10.13.5
- **Date:** 2024-06-07
- **SWI-Prolog Version:** 9.2.5

## Measurement Details

Each example was executed 100 times, and the average runtime was calculated.

Files starting with `vampire` measure only the runtime of Vampire without the transformation. Files starting with `runtime` measure the total runtime of `rs2fol`, including the transformation from RDF surfaces to FOL.
