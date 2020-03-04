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

import java.util.*;

public class Grid {
    private final Type       type;
    private final List<Cell> cells;

    private final List<House.Row>    rows;
    private final List<House.Column> columns;
    private final List<House.Block>  blocks;

    protected final BitSet[] potentialPositions;

    private boolean stateValid;

    public static Grid of(PredefinedType type) {
        return new Grid(new Type(type.getGridSize(), type.getBlockFunction()));
    }

    public static Grid of(int gridSize, BlockFunction blockFunction) {
        return new Grid(new Type(gridSize, blockFunction));
    }

    Grid(Type type) {
        this.type = type;

        int gridSize  = type.getGridSize();
        int cellCount = gridSize * gridSize;

        this.cells = new ArrayList<>(cellCount);

        this.rows    = new ArrayList<>(gridSize);
        this.columns = new ArrayList<>(gridSize);
        this.blocks  = new ArrayList<>(gridSize);

        for (int i = 0; i < gridSize; i++) {
            rows.add(new House.Row(this, i));
            columns.add(new House.Column(this, i));
            blocks.add(new House.Block(this, i));
        }

        for (int i = 0; i < cellCount; i++) {
            int rowIndex    = type.getRowIndex(i);
            int columnIndex = type.getColumnIndex(i);
            int blockIndex  = type.getBlockIndex(i);

            Cell cell = new Cell(this, i, rowIndex, columnIndex, blockIndex);
            cells.add(cell);

            // row and column are 1-based.
            getRow(rowIndex).addCell(cell);
            getColumn(columnIndex).addCell(cell);

            if (blockIndex >= 0) {
                blocks.get(blockIndex).addCell(cell);
            }
        }

        // Initialize peers for each cell.
        for (House house : houses()) {
            for (Cell cell : house.cells()) {
                cell.addPeers(house.getCells());
            }
        }

        this.potentialPositions = new BitSet[getGridSize()];
        for (int i = 0; i < potentialPositions.length; i++) {
            potentialPositions[i] = new BitSet(getCellCount());
        }

        stateValid = false;
    }

    /**
     * Copy constructor for grids.
     */
    Grid(Grid other) {
        this(other.type);

        // Copy values
        for (Cell otherCell : other.cells) {
            Cell cell = getCell(otherCell.getCellIndex());
            cell.setValue(otherCell.getValue(), false);
            cell.setGiven(otherCell.isGiven());
            cell.getExcludedValues().or(otherCell.getExcludedValues());
        }

        updateState();
    }

    public Grid copy() {
        return new Grid(this);
    }

    public Type getType() {
        return type;
    }

    public int getGridSize() {
        return type.getGridSize();
    }

    public int getCellCount() {
        return type.getCellCount();
    }

    public Iterable<Cell> cells() {
        return cells;
    }

    public Iterable<House.Row> rows() {
        return rows;
    }

    public Iterable<House.Column> columns() {
        return columns;
    }

    public Iterable<House.Block> blocks() {
        return blocks;
    }

    public Iterable<House> houses() {
        return concat(rows, columns, blocks);
    }

    public Cell getCell(int cellIndex) {
        return cells.get(cellIndex);
    }

    public Cell getCell(int row, int column) {
        return cells.get(type.getCellIndex(row, column));
    }

    public House.Row getRow(int rowIndex) {
        return rows.get(rowIndex);
    }

    public House.Column getColumn(int columnIndex) {
        return columns.get(columnIndex);
    }

    public House.Block getBlock(int blockIndex) {
        return blocks.get(blockIndex);
    }

    Iterable<Cell> getCells(BitSet cells) {
        return () -> new CellIterator(cells);
    }

    public boolean isSolved() {
        for (Cell cell : cells) {
            if (cell.getValue() == 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isValid() {
        for (House house : houses()) {
            if (!house.isValid()) {
                return false;
            }
        }
        return true;
    }

    // Visitor methods.

    public <T> T accept(GridVisitor<T> visitor) {
        return visitor.visitGrid(this);
    }

    public void acceptCells(CellVisitor visitor) {
        for (Cell cell : cells()) {
            visitor.visitCell(cell);
        }
    }

    public void acceptRows(HouseVisitor visitor) {
        for (House.Row row : rows) {
            visitor.visitRow(row);
        }
    }

    public void acceptColumns(HouseVisitor visitor) {
        for (House.Column column : columns) {
            visitor.visitColumn(column);
        }
    }

    public void acceptBlocks(HouseVisitor visitor) {
        for (House.Block block : blocks) {
            visitor.visitBlock(block);
        }
    }

    public void acceptHouses(HouseVisitor visitor) {
        acceptRows(visitor);
        acceptColumns(visitor);
        acceptBlocks(visitor);
    }

    // Internal state related methods.

    void notifyCellValueChanged(Cell cell, int oldValue, int newValue) {
        stateValid = true;

        // If the value did not really change, there is nothing to do.
        if (oldValue == newValue) {
            return;
        }

        // First: update assigned values in affected houses.
        cell.getRow().updateAssignedValues(oldValue, newValue);
        cell.getColumn().updateAssignedValues(oldValue, newValue);
        cell.getBlock().updateAssignedValues(oldValue, newValue);

        // Second: update possible values in affected cells.
        for (Cell affectedCell : concat(cell, cell.peers())) {
            affectedCell.resetPossibleValues();
            affectedCell.updatePossibleValues(affectedCell.getRow().getAssignedValues());
            affectedCell.updatePossibleValues(affectedCell.getColumn().getAssignedValues());
            affectedCell.updatePossibleValues(affectedCell.getBlock().getAssignedValues());
        }

        // Third: update potential positions for affected cells.
        BitSet peers = cell.getPeers();
        for (BitSet positions : potentialPositions) {
            positions.clear(cell.getCellIndex());
            positions.andNot(peers);
        }

        for (Cell affectedCell : concat(cell, cell.peers())) {
            BitSet possibleValues = affectedCell.getPossibleValues();
            for (int i = possibleValues.nextSetBit(1); i >= 0; i = possibleValues.nextSetBit(i + 1)) {
                potentialPositions[i - 1].set(affectedCell.getCellIndex());
            }
        }
    }

    void notifyPossibleValuesChanged(Cell cell) {
        stateValid = true;

        for (BitSet potentialPositions : this.potentialPositions) {
            potentialPositions.clear(cell.getCellIndex());
        }

        BitSet possibleValues = cell.getPossibleValues();
        for (int i = possibleValues.nextSetBit(1); i >= 0; i = possibleValues.nextSetBit(i + 1)) {
            potentialPositions[i - 1].set(cell.getCellIndex());
        }
    }

    void invalidateState() {
        stateValid = false;
    }

    void throwIfStateIsInvalid() {
        if (!stateValid) {
            throw new RuntimeException("Cache data is invalidated, need to call refreshCache() before accessing cached data.");
        };
    }

    public void updateState() {
        // we are currently updating the internal state, ensure that we can safely
        // access any getters.
        stateValid = true;

        // First: reset the possible values in all cells.
        for (Cell cell : cells()) {
            cell.resetPossibleValues();
        }

        // Second: refresh all assigned values in each house.
        for (House house : houses()) {
            house.updateAssignedValues();
        }

        // Third: remove potential values in each cell which
        //        are already assigned in the houses it is contained.
        for (House house : houses()) {
            house.updatePossibleValuesInCells();
        }

        // Fourth: refresh all possible positions for each cell.
        for (BitSet positions : potentialPositions) {
            positions.clear();
        }

        for (Cell cell : cells()) {
            BitSet possibleValues = cell.getPossibleValues();
            for (int i = possibleValues.nextSetBit(1); i >= 0; i = possibleValues.nextSetBit(i + 1)) {
                potentialPositions[i - 1].set(cell.getCellIndex());
            }
        }
    }

    public void clear() {
        for (Cell cell : cells()) {
            cell.clear(false);
        }

        for (House house : houses()) {
            house.clear();
        }

        for (BitSet bitSet : potentialPositions) {
            bitSet.clear();
        }

        updateState();
    }

    public void reset() {
        for (Cell cell : cells()) {
            cell.reset();
        }

        updateState();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Grid [" + type + "]:\n");

        for (Cell cell : cells()) {
            sb.append("  " + cell.toString() + "\n");
        }

        return sb.toString();
    }

    // Inner helper classes.

    public interface BlockFunction {
        int getBlockIndex(int cellIndex);
    }

    public static class Type {
        private final int           gridSize;
        private final int           cellCount;
        private final BlockFunction blockFunction;

        private Type(int gridSize, BlockFunction blockFunction) {
            this.gridSize      = gridSize;
            this.cellCount     = gridSize * gridSize;
            this.blockFunction = blockFunction;
        }

        public int getGridSize() {
            return gridSize;
        }

        public int getCellCount() {
            return cellCount;
        }

        public int getRowIndex(int cellIndex) {
            return cellIndex / gridSize;
        }

        public int getColumnIndex(int cellIndex) {
            return cellIndex % gridSize;
        }

        public int getBlockIndex(int cellIndex) {
            return blockFunction.getBlockIndex(cellIndex);
        }

        public int getCellIndex(int row, int column) {
            return (row - 1) * gridSize + (column - 1);
        }

        public String getCellName(int cellIndex) {
            return String.format("r%dc%d", getRowIndex(cellIndex) + 1, getColumnIndex(cellIndex) + 1);
        }

        @Override
        public String toString() {
            return String.format("%dx%d", gridSize, gridSize);
        }
    }

    private class CellIterator implements Iterator<Cell> {

        private final BitSet cells;
        private       int    nextOffset;

        CellIterator(BitSet cells) {
            this.cells      = cells;
            this.nextOffset = cells.nextSetBit(0);
        }

        @Override
        public boolean hasNext() {
            return nextOffset >= 0;
        }

        @Override
        public Cell next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Cell cell  = getCell(nextOffset);
            nextOffset = cells.nextSetBit(nextOffset + 1);
            return cell;
        }
    }

    protected static class ValueIterator implements Iterator<Integer> {

        private final BitSet  bitSet;
        private final int     toIndex;
        private final boolean inverse;
        private       int     nextOffset;

        ValueIterator(BitSet bitSet, int fromIndex, int toIndex, boolean inverse) {
            this.bitSet     = bitSet;
            this.toIndex    = toIndex;
            this.inverse    = inverse;
            this.nextOffset = nextBit(fromIndex);
        }

        private int nextBit(int offset) {
            return inverse ? bitSet.nextClearBit(offset) :
                             bitSet.nextSetBit(offset);
        }

        @Override
        public boolean hasNext() {
            return nextOffset >= 0 && nextOffset <= toIndex;
        }

        @Override
        public Integer next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Integer value = nextOffset;
            nextOffset = nextBit(nextOffset + 1);
            return value;
        }
    }

    private <T> Iterable<T> concat(Iterable<? extends T>... iterables) {
        return () -> new ConcatIterator<>(iterables);
    }

    private <T> Iterable<T> concat(T element, Iterable<T> iterable) {
        return () -> new ConcatIterator<>(Arrays.asList(element), iterable);
    }

    private class ConcatIterator<T> implements Iterator<T> {
        private final Iterable<T>[] iterables;

        private int         currentIterableIndex;
        private Iterator<T> currentIterator;

        public ConcatIterator(Iterable<? extends T>... iterables) {
            this.iterables = (Iterable<T>[]) iterables;
        }

        public boolean hasNext() {
            updateCurrentIterator();
            return currentIterator.hasNext();
        }

        public T next() {
            updateCurrentIterator();
            return currentIterator.next();
        }

        protected void updateCurrentIterator() {
            if (currentIterator == null) {
                if(iterables == null || iterables.length == 0) {
                    currentIterator = Collections.emptyIterator();
                } else {
                    currentIterator = iterables[0].iterator();
                }
            }

            while (!currentIterator.hasNext() && currentIterableIndex < iterables.length - 1) {
                currentIterableIndex++;
                currentIterator = iterables[currentIterableIndex].iterator();
            }
        }
    }
}
