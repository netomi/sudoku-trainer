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

/**
 * An interface used to visit a {@code #House}.
 *
 * @author Thomas Neidhart
 */
public interface HouseVisitor {

    /**
     * By default, this method is called for any visited {@code #House}.
     */
    default void visitAnyHouse(House house) {};

    /**
     * Called when visiting a row.
     * <p>
     * By default it delegates to {@code #visitAnyHouse}.
     */
    default void visitRow(House.Row row) {
        visitAnyHouse(row);
    }

    /**
     * Called when visiting a column.
     * <p>
     * By default it delegates to {@code #visitAnyHouse}.
     */
    default void visitColumn(House.Column column) {
        visitAnyHouse(column);
    }

    /**
     * Called when visiting a block.
     * <p>
     * By default it delegates to {@code #visitAnyHouse}.
     */
    default void visitBlock(House.Block block) {
        visitAnyHouse(block);
    }
}
