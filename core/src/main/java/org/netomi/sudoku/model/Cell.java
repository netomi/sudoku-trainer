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

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

/**
 * Represents a cell in a sudoku grid.
 *
 * @author Thomas Neidhart
 */
public class Cell {
    // Immutable properties.
    private final Grid    owner;
    private final BitSet  peers;

    private final int     cellIndex;
    private final int     rowIndex;
    private final int     columnIndex;
    private final int     blockIndex;

    // Mutable properties.
    private       int     value;
    private       boolean given;
    private final BitSet  possibleValues;
    private final BitSet  excludedValues;

    Cell(Grid owner, int cellIndex, int rowIndex, int columnIndex, int blockIndex) {
        this.owner  = owner;
        this.peers  = new BitSet(owner.getCellCount());

        this.cellIndex   = cellIndex;
        this.rowIndex    = rowIndex;
        this.columnIndex = columnIndex;
        this.blockIndex  = blockIndex;

        this.value          = 0;
        this.given          = false;
        this.possibleValues = new BitSet(owner.getGridSize() + 1);
        this.possibleValues.set(1, owner.getGridSize() + 1);
        this.excludedValues = new BitSet(owner.getGridSize() + 1);
    }

    public Grid getOwner() {
        return owner;
    }

    /**
     * Returns the index of this cell in the contained grid (zero-based).
     */
    public int getCellIndex() {
        return cellIndex;
    };

    /**
     * Returns the index of the row this cell belongs to (zero-based).
     */
    public int getRowIndex() {
        return rowIndex;
    }

    /**
     * Returns the {@link House.Row} this cell belongs to.
     */
    public House.Row getRow() {
        return owner.getRow(rowIndex);
    }

    /**
     * Returns the index of the column this cell belongs to (zero-based).
     * @return
     */
    public int getColumnIndex() {
        return columnIndex;
    }

    /**
     * Returns the {@link House.Column} this cell belongs to.
     */
    public House.Column getColumn() {
        return owner.getColumn(columnIndex);
    }

    /**
     * Returns the index of the block this cell belongs to (zero-based).
     */
    public int getBlockIndex() {
        return blockIndex;
    }

    /**
     * Returns the {@link House.Block} this cell belongs to.
     */
    public House.Block getBlock() {
        return owner.getBlock(blockIndex);
    }

    /**
     * Returns the currently assigned value of this cell.
     * @return the value of the cell, or {@code 0} if not assigned.
     */
    public int getValue() {
        return value;
    }

    /**
     * Assigns the given value to the current cell.
     * <p>
     * Calling this method will update the internal state of the grid.
     *
     * @param value the value to assign this cell to
     * @throws IllegalArgumentException if the value is outside the allowed range [0, gridSize]
     * @throws IllegalStateException if the cell contains a given value (see {@link #isGiven()})
     */
    public void setValue(int value) {
        if (value < 0 || value > owner.getGridSize()) {
            throw new IllegalArgumentException("invalid value for cell: " + value +
                                               " outside allowed range [0," + owner.getGridSize() + "]");
        }
        setValue(value, true);
    }

    /**
     * Assigns the given value to the current cell.
     *
     * @param value the value to assign this cell to
     * @param updateGrid if the internal state of the grid should be updated
     * @throws IllegalArgumentException if the value is outside the allowed range [0, gridSize]
     * @throws IllegalStateException if the cell contains a given value (see {@link #isGiven()})
     */
    public void setValue(int value, boolean updateGrid) {
        if (isGiven()) {
            throw new IllegalStateException("cell value is fixed");
        }

        owner.invalidateState();
        int oldValue = this.value;
        this.value = value;

        if (updateGrid) {
            owner.notifyCellValueChanged(this, oldValue, value);
        }
    }

    /**
     * Returns whether a value has been assigned to this cell.
     */
    public boolean isAssigned() {
        return value > 0;
    }

    /**
     * Returns whether this cell has a given value.
     * <p>
     * If a cell contains a given value, it can not be modified.
     *
     * @see {@link #clear()}
     * @see {@link #setGiven(boolean)}
     */
    public boolean isGiven() {
        return given;
    }

    /**
     * Changes whether the cell shall have a fixed value.
     */
    public void setGiven(boolean given) {
        this.given = given;
    }

    void addPeers(BitSet cells) {
        peers.or(cells);
        peers.clear(cellIndex);
    }

    BitSet getPeers() {
        return peers;
    }

    /**
     * Returns an {@code #Iterable} containing all cell that are visible from
     * this cell, i.e. are contained in the same row, column or block.
     */
    public Iterable<Cell> peers() {
        return owner.getCells(peers);
    }

    /**
     * Returns the name of this cell in format rXcY, where X and Y are
     * respective row and column numbers this cell is contained in.
     */
    public String getName() {
        return String.format("r%dc%d", rowIndex + 1, columnIndex + 1);
    }

    /**
     * Returns the possible values that can be assigned to this cell considering
     * the already assigned values in peer cells.
     *
     * @throws RuntimeException if the internal state is not updated
     */
    public BitSet getPossibleValues() {
        owner.throwIfStateIsInvalid();
        return possibleValues;
    }

    /**
     * Excludes the given values from the set of possible values.
     * @param values the values to exclude
     */
    public void excludePossibleValues(int... values) {
        owner.invalidateState();
        for (int value : values) {
            excludedValues.set(value);
        }
        owner.notifyPossibleValuesChanged(this);
    }

    /**
     * Excludes the given values from the set of possible values.
     * @param values the values to exclude
     */
    public void excludePossibleValues(BitSet values) {
        owner.invalidateState();
        excludedValues.or(values);
        owner.notifyPossibleValuesChanged(this);
    }

    BitSet getExcludedValues() {
        return excludedValues;
    }

    /**
     * Clears the set of excluded values for this cell.
     */
    public void clearExcludedValues() {
        owner.invalidateState();
        excludedValues.clear();
        owner.notifyPossibleValuesChanged(this);
    }

    void resetPossibleValues() {
        possibleValues.clear();
        if (!isAssigned()) {
            possibleValues.set(1, owner.getGridSize() + 1);
            possibleValues.andNot(excludedValues);
        }
    }

    void updatePossibleValues(BitSet assignedValues) {
        possibleValues.andNot(assignedValues);
    }

    /**
     * Fully clears this cell, including its value and given status.
     * <p>
     * Calling this method will update the internal state of the grid.
     */
    public void clear() {
        clear(true);
    }

    /**
     * Fully clears this cell, including its value and given status.
     *
     * @param updateGrid whether the grid shall be updated
     */
    public void clear(boolean updateGrid) {
        owner.invalidateState();

        given = false;

        possibleValues.clear();
        possibleValues.set(1, owner.getGridSize() + 1);
        excludedValues.clear();

        setValue(0, updateGrid);
    }

    /**
     * Resets this cell to its initial state, retaining given values.
     * <p>
     * Calling this method will update the internal state of the grid.
     */
    public void reset() {
        reset(true);
    }

    /**
     * Resets this cell to its initial state, retaining given values.
     * <p>
     * @param updateGrid whether the grid shall be updated
     */
    public void reset(boolean updateGrid) {
        owner.invalidateState();

        possibleValues.clear();
        possibleValues.set(1, owner.getGridSize() + 1);
        excludedValues.clear();

        if (!given) {
            setValue(0, updateGrid);
        } else if (updateGrid) {
            owner.notifyPossibleValuesChanged(this);
        }
    }

    // Visitor methods.

    /**
     * Visit this class with the specified visitor.
     */
    public void accept(CellVisitor visitor) {
        visitor.visitCell(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return cellIndex == cell.cellIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cellIndex);
    }

    @Override
    public String toString() {
        return String.format("r%dc%d = %d (%s)", rowIndex + 1, columnIndex + 1, value, given ? "given" : possibleValues);
    }
}
