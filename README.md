A Sudoku trainer / solver / generator.
======================================

This repository contains a full-blown sudoku trainer / solver / generator
which allows training of various techniques to solve sudoku puzzles.

## Features

 * different types of sudoku puzzles (classic 9x9, 6x6, 4x4, jigsaw)
 * various logical solving techniques
   - singles (full house, naked & hidden singles)
   - locked pairs / triples
   - intersections (pointing / claiming)
   - hidden pairs / triples / quadruples
   - naked pairs / triples / quadruples
   - basic fish (x-wing, swordfish, jellyfish)
   - single digit patterns (skyscraper, 2 string kite)
   - uniqueness tests (unique rectangle 1/2/4)
   - wings (xy-wing, xyz-wing, w-wing)
   - chains (remote pair, x-chain, xy-chain)
 * all possible solution steps
 * brute force solver
 * rating mechanism
 * puzzle generator
 * visual explanation of each solution step
 * much more ...
 
## Getting Started

The app requires a working Java 11 installation on your computer.

To run the app, simply type:

```
./gradlew run
```

## Screenshots

![Screenshot](https://raw.githubusercontent.com/netomi/sudoku-trainer/master/images/sudoku-trainer-screenshot.png)

Copyright (c) 2020 Thomas Neidhart

