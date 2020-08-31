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
public abstract class House
{
    // Immutable properties.
    private final Grid    owner;
    private final int     regionIndex;
    private final CellSet cells;

    // Mutable properties.
    private final ValueSet assignedValues;

    House(Grid owner, int regionIndex) {
        this.owner          = owner;
        this.regionIndex    = regionIndex;
        this.cells          = CellSet.empty(owner);
        this.assignedValues = ValueSet.empty(owner);
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

    CellSet getCells() {
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
     * @param otherCells a {@code #CellSet} whose bits represent cells in the grid
     */
    public boolean containsAllCells(CellSet otherCells) {
        for (int index : otherCells.allSetBits()) {
            if (!containsCell(index)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns an {@code #Iterable} containing all cells of this {@code #House}.
     */
    public Iterable<Cell> cells() {
        return cells.allCells(owner);
    }

    /**
     * Returns an {@code #Iterable} containing all cells of this {@code #House},
     * whose cell index is >= startIndex.
     */
    public Iterable<Cell> cells(int startIndex) {
        return cells.allCells(owner, startIndex);
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
        return cells.filteredCells(owner, Cell::isAssigned, startIndex);
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
        return cells.filteredCells(owner, cell -> !cell.isAssigned(), startIndex);
    }

    /**
     * Returns an {@code #Iterable} containing all cells of this {@code #House}
     * excluding all cells contained in the provided houses.
     */
    public Iterable<Cell> cellsExcluding(House... excludedHouses) {
        CellSet ownCells = cells.copy();
        for (House house : excludedHouses) {
            ownCells.andNot(house.cells);
        }
        return ownCells.allCells(owner);
    }

    /**
     * Returns an {@code #Iterable} containing all cells of this {@code #House}
     * excluding all cells contained in the provided bitset.
     */
    public Iterable<Cell> cellsExcluding(CellSet excludedCells) {
        CellSet ownCells = cells.copy();
        ownCells.andNot(excludedCells);
        return ownCells.allCells(owner);
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
    public ValueSet getAssignedValues() {
        owner.throwIfStateIsInvalid();
        return assignedValues;
    }

    public Iterable<Integer> assignedValues() {
        owner.throwIfStateIsInvalid();
        return assignedValues.allSetBits();
    }

    public Iterable<Integer> unassignedValues() {
        owner.throwIfStateIsInvalid();
        return assignedValues.allUnsetBits();
    }

    public Iterable<Integer> unassignedValues(int startValue) {
        owner.throwIfStateIsInvalid();
        return assignedValues.allUnsetBits(startValue);
    }

    /**
     * Returns an {@code #Iterable} containing all cells of this {@code #House}
     * that can potentially be assigned to the given value.
     *
     * @param value the value to check for
     */
    public Iterable<Cell> potentialCells(int value) {
        return getPotentialPositions(value).allCells(owner);
    }

    public CellSet getPotentialPositions(int value) {
        owner.throwIfStateIsInvalid();
        CellSet possiblePositions = owner.potentialPositions.get(value - 1);

        CellSet result = possiblePositions.copy();
        result.and(cells);

        return result;
    }

    void updateAssignedValues() {
        assignedValues.clearAll();
        for (Cell cell : assignedCells()) {
            assignedValues.set(cell.getValue());
        }
    }

    void updatePossibleValuesInCell(Cell cell) {
        cell.updatePossibleValues(assignedValues);
    }

    void updatePossibleValuesInCells() {
        for (Cell cell : unassignedCells()) {
            cell.updatePossibleValues(assignedValues);
        }
    }

    void clear() {
        assignedValues.clearAll();
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
