# Change Log

## [unreleased] - yyyy-mm-dd

### Added

- undo / redo mechanism for updating values / candidates
- more solving techniques
  - wings (xy-wing, xyz-wing, w-wing)
  - single digit patterns (empty rectangle, turbot fish)
  - finned fish (finned x-wing / swordfish / jellyfish)
 
### Fixed


## [0.1] - 2020-09-24

First release of the sudoku trainer desktop application.
In this first release the following features are supported:

 - display of different sudoku grids (classical 9x9, 6x6, 4x4, jigsaw)
 - display of pencil marks (either computer or manual)
 - logical solving techniques with graphical display:
   - singles (full house, naked & hidden singles)
   - locked pairs / triples
   - intersections (pointing / claiming)
   - hidden pairs / triples / quadruples
   - naked pairs / triples / quadruples
   - basic fish (x-wing, swordfish, jellyfish)
   - single digit patterns (skyscraper, 2 string kite)
   - uniqueness tests (unique rectangle 1/2/4)
   - chains (remote pair, x-chain, xy-chain)
 - training library for aforementioned techniques
