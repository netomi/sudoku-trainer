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
package org.netomi.sudoku.ui.view

import javafx.scene.paint.Color
import javafx.scene.shape.LineTo
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Arrow constructor(startX: Double,
                        startY: Double,
                        endX: Double,
                        endY: Double,
                        arrowHeadSize: Double = defaultArrowHeadSize)
    : Path()
{
    companion object {
        private const val defaultArrowHeadSize = 15.0
    }

    init {
        strokeProperty().bind(fillProperty())
        fill = Color.RED

        // Line
        elements.add(MoveTo(startX, startY))
        elements.add(LineTo(endX, endY))

        //ArrowHead
        val angle = atan2(endY - startY, endX - startX) - Math.PI / 2.0
        val sin = sin(angle)
        val cos = cos(angle)
        //point1
        val x1 = (-1.0 / 2.0 * cos + sqrt(3.0) / 2 * sin) * arrowHeadSize + endX
        val y1 = (-1.0 / 2.0 * sin - sqrt(3.0) / 2 * cos) * arrowHeadSize + endY
        //point2
        val x2 = (1.0 / 2.0 * cos + sqrt(3.0) / 2 * sin) * arrowHeadSize + endX
        val y2 = (1.0 / 2.0 * sin - sqrt(3.0) / 2 * cos) * arrowHeadSize + endY
        elements.add(LineTo(x1, y1))
        elements.add(LineTo(x2, y2))
        elements.add(LineTo(endX, endY))
    }
}