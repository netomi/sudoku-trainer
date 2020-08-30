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
package org.netomi.sudoku.ui.service;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.netomi.sudoku.model.Grid;
import org.netomi.sudoku.solver.Hint;

import javax.inject.Singleton;

@Singleton
public class ModelServiceImpl implements ModelService {
    private final ObjectProperty<Grid> modelProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Hint> displayedHint = new SimpleObjectProperty<>();

    @Override
    public Grid getModel() {
        return modelProperty.get();
    }

    @Override
    public void setModel(Grid model) {
        modelProperty.set(model);
    }

    @Override
    public ObjectProperty<Grid> modelProperty() {
        return modelProperty;
    }

    @Override
    public void setHint(Hint hint) {
        displayedHint.set(hint);
    }

    @Override
    public Hint getHint() {
        return displayedHint.get();
    }

    @Override
    public ObjectProperty<Hint> hintProperty() {
        return displayedHint;
    }
}
