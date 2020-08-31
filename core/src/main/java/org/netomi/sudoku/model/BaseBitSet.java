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

abstract class BaseBitSet<T extends BaseBitSet<?>>
{
    protected final int    size;
    protected final BitSet bits;

    BaseBitSet(int size) {
        this.size = size;
        this.bits = new BitSet(size);
    }

    protected int getOffset() {
        return 0;
    }

    protected void checkInput(int bit) {
        if (bit < 0 || bit >= size) {
            throw new IllegalArgumentException("illegal value " + bit);
        }
    }

    public int getFirstBitIndex() {
        return getOffset();
    }

    public int getLastBitIndex() {
        return size - 1;
    }

    public int cardinality() {
        return bits.cardinality();
    }

    public void setAll() {
        bits.set(getOffset(), size);
    }

    public void clearAll() {
        bits.clear(getOffset(), size);
    }

    public boolean get(int bit) {
        checkInput(bit);
        return bits.get(bit);
    }

    public void set(int bit) {
        checkInput(bit);
        bits.set(bit);
    }

    public void clear(int bit) {
        checkInput(bit);
        bits.clear(bit);
    }

    public int firstSetBit() {
        return bits.nextSetBit(getOffset());
    }

    public int firstUnsetBit() {
        return bits.nextClearBit(getOffset());
    }

    public int previousSetBit(int startBit) {
        return bits.previousSetBit(startBit);
    }

    public Iterable<Integer> allSetBits() {
        return () -> new BitIterator(getOffset(), size, false);
    }

    public Iterable<Integer> allUnsetBits() {
        return () -> new BitIterator(getOffset(), size, true);
    }

    public Iterable<Integer> allUnsetBits(int startBit) {
        return () -> new BitIterator(startBit, size, true);
    }

    public void and(T other) {
        bits.and(other.bits);
    }

    public void or(T other) {
        bits.or(other.bits);
    }

    public void andNot(T other) {
        bits.andNot(other.bits);
    }

    public Collection<Integer> toCollection() {
        List<Integer> result = new ArrayList<>(cardinality());
        for (int value : allSetBits()) {
            result.add(value);
        }
        return result;
    }

    public int[] toArray() {
        int idx = 0;
        int[] result = new int[cardinality()];
        for (int value : allSetBits()) {
            result[idx++] = value;
        }
        return result;
    }

    public abstract T copy();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseBitSet)) return false;
        BaseBitSet<?> that = (BaseBitSet<?>) o;
        return size == that.size &&
               Objects.equals(bits, that.bits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, bits);
    }

    @Override
    public String toString() {
        return bits.toString();
    }

    // inner helper classes.

    private class BitIterator implements Iterator<Integer> {
        private final int     toIndex;
        private final boolean inverse;
        private       int     nextOffset;

        BitIterator(int fromIndex, int toIndex, boolean inverse) {
            this.toIndex    = toIndex;
            this.inverse    = inverse;
            this.nextOffset = nextBit(fromIndex);
        }

        private int nextBit(int offset) {
            return inverse ? bits.nextClearBit(offset) : bits.nextSetBit(offset);
        }

        @Override
        public boolean hasNext() {
            return nextOffset >= 0 && nextOffset < toIndex;
        }

        @Override
        public Integer next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Integer bit = nextOffset;
            nextOffset = nextBit(nextOffset + 1);
            return bit;
        }
    }
}
