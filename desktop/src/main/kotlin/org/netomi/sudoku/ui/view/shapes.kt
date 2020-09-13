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

import javafx.geometry.Point2D
import javafx.scene.Group
import javafx.scene.paint.Color
import javafx.scene.shape.*
import javafx.scene.transform.Rotate
import java.lang.Math.toDegrees
import kotlin.math.atan2
import kotlin.math.ceil

/**
 * An arrow shape.
 *
 * This is a [Node] subclass and can be added to the JavaFX scene graph in the usual way. Styling can be achieved
 * via the CSS classes *arrow-line* and *arrow-head*.
 *
 * Example:
 *
 * <pre>
 * `Arrow arrow = new Arrow();
 * arrow.setStart(10, 20);
 * arrow.setEnd(100, 150);
 * arrow.draw();`
 * </pre>
 */
class Arrow constructor(private var startX: Double = 0.0, private var startY: Double = 0.0, private var endX: Double = 0.0, private var endY: Double = 0.0): Group() {
    private val line = QuadCurve()
    private val head: ArrowHead = ArrowHead()

    /**
     * Sets the width of the arrow-head.
     *
     * @param width the width of the arrow-head
     */
    fun setHeadWidth(width: Double) {
        head.setWidth(width)
    }

    /**
     * Sets the length of the arrow-head.
     *
     * @param length the length of the arrow-head
     */
    fun setHeadLength(length: Double) {
        head.length = length
    }

    /**
     * Sets the radius of curvature of the [ArcTo] at the base of the arrow-head.
     *
     *
     *
     * If this value is less than or equal to zero, a straight line will be drawn instead. The default is -1.
     *
     *
     * @param radius the radius of curvature of the arc at the base of the arrow-head
     */
    fun setHeadRadius(radius: Double) {
        head.setRadiusOfCurvature(radius)
    }

    /**
     * Gets the start point of the arrow.
     *
     * @return the start [Point2D] of the arrow
     */
    val start: Point2D
        get() = Point2D(startX, startY)

    /**
     * Sets the start position of the arrow.
     *
     * @param startX the x-coordinate of the start position of the arrow
     * @param startY the y-coordinate of the start position of the arrow
     */
    fun setStart(startX: Double, startY: Double) {
        this.startX = startX
        this.startY = startY
    }

    /**
     * Gets the start point of the arrow.
     *
     * @return the start [Point2D] of the arrow
     */
    val end: Point2D
        get() = Point2D(endX, endY)

    /**
     * Sets the end position of the arrow.
     *
     * @param endX the x-coordinate of the end position of the arrow
     * @param endY the y-coordinate of the end position of the arrow
     */
    fun setEnd(endX: Double, endY: Double) {
        this.endX = endX
        this.endY = endY
    }

    /**
     * Draws the arrow for its current size and position values.
     */
    fun draw() {
        val deltaX = endX - startX
        val deltaY = endY - startY
        val angle = atan2(deltaX, deltaY)
        val headX: Double = endX - head.length / 2 * Math.sin(angle)
        val headY: Double = endY - head.length / 2 * Math.cos(angle)
        line.startX = moveOffPixel(startX)
        line.startY = moveOffPixel(startY)
        line.endX = moveOffPixel(headX)
        line.endY = moveOffPixel(headY)
        line.controlX = (line.startX + line.endX) / 2 - 20
        line.controlY = (line.startY + line.endY) / 2 - 20
        head.setCenter(headX, headY)
        head.setAngle(toDegrees(-angle))
        head.draw()
    }

    companion object {
        private const val STYLE_CLASS_LINE = "arrow-line"
        private const val STYLE_CLASS_HEAD = "arrow-head"
    }

    init {
        line.styleClass.add(STYLE_CLASS_LINE)
        head.styleClass.add(STYLE_CLASS_HEAD)

        line.fill = null
        line.stroke = Color.RED
        head.fill = Color.RED

        line.strokeWidth = 2.0

        children.addAll(line, head)
    }
}

/**
 * An arrow-head shape.
 *
 *
 *
 * This is used by the [Arrow] class.
 *
 */
class ArrowHead : Path() {
    private var x = 0.0
    private var y = 0.0

    var length = DEFAULT_LENGTH
    private var width = DEFAULT_WIDTH
    private var radius = -1.0
    private val rotate = Rotate()

    /**
     * Sets the center position of the arrow-head.
     *
     * @param x the x-coordinate of the center of the arrow-head
     * @param y the y-coordinate of the center of the arrow-head
     */
    fun setCenter(x: Double, y: Double) {
        this.x = x
        this.y = y
        rotate.pivotX = x
        rotate.pivotY = y
    }

    /**
     * Sets the width of the arrow-head.
     *
     * @param width the width of the arrow-head
     */
    fun setWidth(width: Double) {
        this.width = width
    }

    /**
     * Sets the radius of curvature of the [ArcTo] at the base of the arrow-head.
     *
     *
     *
     * If this value is less than or equal to zero, a straight line will be drawn instead. The default is -1.
     *
     *
     * @param radius the radius of curvature of the arc at the base of the arrow-head
     */
    fun setRadiusOfCurvature(radius: Double) {
        this.radius = radius
    }

    /**
     * Sets the rotation angle of the arrow-head.
     *
     * @param angle the rotation angle of the arrow-head, in degrees
     */
    fun setAngle(angle: Double) {
        rotate.angle = angle
    }

    /**
     * Draws the arrow-head for its current size and position values.
     */
    fun draw() {
        elements.clear()
        elements.add(MoveTo(x, y + length / 2))
        elements.add(LineTo(x + width / 2, y - length / 2))
        if (radius > 0) {
            val arcTo = ArcTo()
            arcTo.x = x - width / 2
            arcTo.y = y - length / 2
            arcTo.radiusX = radius
            arcTo.radiusY = radius
            arcTo.isSweepFlag = true
            elements.add(arcTo)
        } else {
            elements.add(LineTo(x - width / 2, y - length / 2))
        }
        elements.add(ClosePath())
    }

    companion object {
        private const val DEFAULT_LENGTH = 10.0
        private const val DEFAULT_WIDTH = 10.0
    }

    init {
        fill = Color.BLACK
        strokeType = StrokeType.INSIDE
        transforms.add(rotate)
    }
}

private const val HALF_A_PIXEL = 0.5

/**
 * Moves an x or y position value on-pixel.
 *
 * <p>
 * Lines drawn off-pixel look blurry. They should therefore have integer x and y values.
 * </p>
 *
 * @param position the position to move on-pixel
 *
 * @return the position rounded to the nearest integer
 */
fun moveOnPixel(position: Double): Double {
    return ceil(position)
}

/**
 * Moves an x or y position value off-pixel.
 *
 * <p>
 * This is for example useful for a 1-pixel-wide stroke with a stroke-type of centered. The x and y positions need
 * to be off-pixel so that the stroke is on-pixel.
 * </p>
 *
 * @param position the position to move off-pixel
 *
 * @return the position moved to the nearest value halfway between two integers
 */
fun moveOffPixel(position: Double): Double {
    return ceil(position) - HALF_A_PIXEL
}