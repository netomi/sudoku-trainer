/*
 * Sudoku creator / solver / teacher.
 *
 * Copyright (c) 2020 Thomas Neidhart
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.netomi.sudoku.model;

import java.util.BitSet;
import java.util.Iterator;
import java.util.function.Predicate;

/**
 * A class representing a certain region within a sudoku grid.
 * <p>
 * Possible region types are:
 * <p><ul>
 * <li>row
 * <li>column
 * <li>block
 * </ul>
 *
 * @author Thomas Neidhart
 */
public abstract class House {
    // Immutable properties.
    private final Grid   owner;
    private final int    regionIndex;
    private final BitSet cells;

    // Mutable properties.
    private final BitSet assignedValues;

    House(Grid owner, int regionIndex) {
        this.owner          = owner;
        this.regionIndex    = regionIndex;
        this.cells          = new BitSet(owner.getCellCount());
        this.assignedValues = new BitSet(owner.getGridSize() + 1);
    }

    /**
     * Returns the specific type of region of this {@code #House}.
     */
    public abstract Type getType();

    /**
     * Returns the index of this region within its type set (zero-based).
     */
    public int getRegionIndex() {
        return regionIndex;
    }

    /**
     * Returns the number of cells contained in this {@code #House}.
     */
    public int getSize() {
        return cells.cardinality();
    }

    void addCell(Cell cell) {
        cells.set(cell.getCellIndex());
    }

    BitSet getCells() {
        return cells;
    }

    /**
     * Checks whether the given cell, identified by its cell index, is contained in this
     * {@code #House}.
     *
     * @param cellIndex the index of the cell to check
     */
    public boolean containsCell(int cellIndex) {
        return cells.get(cellIndex);
    }

    /**
     * Checks whether the given cell is contained in this {@code #House}.
     */
    public boolean containsCell(Cell cell) {
        return containsCell(cell.getCellIndex());
    }

    /**
     * Checks whether all cells marked in the given {@code #BitSet} are contained
     * in this {@code #House}.
     * <p>
     * Note: an empty input always returns {@code true}.
     *
     * @param cells a {@code #BitSet} whose bits represent cells in the grid
     */
    public boolean containsAllCells(BitSet cells) {
        for (int i = cells.nextSetBit(0); i >= 0; i = cells.nextSetBit(i + 1)) {
            if (!containsCell(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns an {@code #Iterable} containing all cells of this {@code #House}.
     */
    public Iterable<Cell> cells() {
        return owner.getCells(cells);
    }

    /**
     * Returns an {@code #Iterable} containing all cells of this {@code #House},
     * whose cell index is >= startIndex.
     */
    public Iterable<Cell> cells(int startIndex) {
        return owner.getCells(cells, startIndex);
    }

    /**
     * Returns an {@code #Iterable} containing all assigned cells of this {@code #House}.
     */
    public Iterable<Cell> assignedCells() {
        return assignedCells(0);
    }

    /**
     * Returns an {@code #Iterable} containing all assigned cells of this {@code #House},
     * whose cell index is >= startIndex.
     */
    public Iterable<Cell> assignedCells(int startIndex) {
        return owner.getCells(cells, (cell) -> cell.isAssigned(), startIndex);
    }

    /**
     * Returns an {@code #Iterable} containing all unassigned cells of this {@code #House}.
     */
    public Iterable<Cell> unassignedCells() {
        return unassignedCells(0);
    }

    /**
     * Returns an {@code #Iterable} containing all unassigned cells of this {@code #House},
     * whose cell index is >= startIndex.
     */
    public Iterable<Cell> unassignedCells(int startIndex) {
        return owner.getCells(cells, (cell) -> !cell.isAssigned(), startIndex);
    }

    /**
     * Returns an {@code #Iterable} containing all cells of this {@code #House}
     * excluding all cells contained in the provided houses.
     */
    public Iterable<Cell> cellsExcluding(House... excludedHouses) {
        BitSet ownCells = (BitSet) cells.clone();
        for (House house : excludedHouses) {
            ownCells.andNot(house.cells);
        }
        return owner.getCells(ownCells);
    }

    /**
     * Returns an {@code #Iterable} containing all cells of this {@code #House}
     * excluding all cells contained in the provided bitset.
     */
    public Iterable<Cell> cellsExcluding(BitSet excludedCells) {
        BitSet ownCells = (BitSet) cells.clone();
        ownCells.andNot(excludedCells);
        return owner.getCells(ownCells);
    }

    /**
     * Checks whether all cells in this {@code #House} have assigned unique values.
     */
    public boolean isValid() {
        BitSet assignedValues = new BitSet();
        for (Cell cell : assignedCells()) {
            if (!assignedValues.get(cell.getValue())) {
                assignedValues.set(cell.getValue());
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether all cells in this {@code #House} have unique values assigned.
     */
    public boolean isSolved() {
        return getAssignedValues().cardinality() == owner.getGridSize();
    }

    /**
     * Returns the assigned values of all cells contained in this {@code #House}
     * as {@code #BitSet}.
     */
    public BitSet getAssignedValues() {
        owner.throwIfStateIsInvalid();
        return assignedValues;
    }

    public Iterable<Integer> assignedValues() {
        owner.throwIfStateIsInvalid();
        return () -> new Grid.ValueIterator(assignedValues, 1, owner.getGridSize(), false);
    }

    public Iterable<Integer> unassignedValues() {
        owner.throwIfStateIsInvalid();
        return () -> new Grid.ValueIterator(assignedValues, 1, owner.getGridSize(), true);
    }

    public Iterable<Integer> unassignedValues(int startValue) {
        owner.throwIfStateIsInvalid();
        return () -> new Grid.ValueIterator(assignedValues, startValue, owner.getGridSize(), true);
    }

    /**
     * Returns an {@code #Iterable} containing all cells of this {@code #House}
     * that can potentially be assigned to the given value.
     *
     * @param value the value to check for
     */
    public Iterable<Cell> potentialCells(int value) {
        return owner.getCells(getPotentialPositions(value));
    }

    public BitSet getPotentialPositions(int value) {
        owner.throwIfStateIsInvalid();
        BitSet possiblePositions = owner.potentialPositions[value - 1];

        BitSet cloned = (BitSet) possiblePositions.clone();
        cloned.and(cells);

        return cloned;
    }

    void updateAssignedValues() {
        assignedValues.clear();
        for (Cell cell : assignedCells()) {
            assignedValues.set(cell.getValue());
        }
    }

    void updatePossibleValuesInCells() {
        for (Cell cell : unassignedCells()) {
            cell.updatePossibleValues(assignedValues);
        }
    }

    void clear() {
        assignedValues.clear();
    }

    // Concrete classes representing the different regions.

    /**
     * An enumeration of possible types of houses (regions) as contained
     * in a sudoku grid.
     */
    public enum Type {
        ROW,
        COLUMN,
        BLOCK
    }

    public static class Row extends House {
        Row(Grid owner, int rowIndex) {
            super(owner, rowIndex);
        }

        @Override
        public Type getType() {
            return Type.ROW;
        }

        public int getRowNumber() {
            return getRegionIndex() + 1;
        }

        @Override
        public String toString() {
            return String.format("r%d = %s", getRowNumber(), getAssignedValues());
        }
    }

    public static class Column extends House {
        Column(Grid owner, int columnIndex) {
            super(owner, columnIndex);
        }

        @Override
        public Type getType() {
            return Type.COLUMN;
        }

        public int getColumnNumber() {
            return getRegionIndex() + 1;
        }

        @Override
        public String toString() {
            return String.format("c%d = %s", getColumnNumber(), getAssignedValues());
        }
    }

    public static class Block extends House {
        Block(Grid owner, int blockIndex) {
            super(owner, blockIndex);
        }

        @Override
        public Type getType() {
            return Type.BLOCK;
        }

        public int getBlockNumber() {
            return getRegionIndex() + 1;
        }

        @Override
        public String toString() {
            return String.format("b%d = %s", getBlockNumber(), getAssignedValues());
        }
    }
}
