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
package com.github.netomi.sudoku.trainer.view

import com.github.netomi.sudoku.trainer.Styles
import javafx.beans.binding.Bindings
import javafx.beans.binding.DoubleBinding
import javafx.beans.value.ObservableDoubleValue
import javafx.geometry.Bounds
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.shape.*
import javafx.scene.transform.Rotate
import javafx.scene.transform.Transform
import tornadofx.CssRule
import tornadofx.addClass
import tornadofx.removeClass
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

interface StylableNode {
    fun setStyle(css: String)
    fun setStyleClass(cssRule: CssRule)
    fun addStyleClass(cssRule: CssRule)
    fun removeStyleClass(cssRule: CssRule)
}

class StyleProxy(private val client: Shape) : StylableNode {
    override fun setStyle(css: String) {
        client.style = css
    }

    override fun setStyleClass(cssRule: CssRule) {
        client.styleClass.clear()
        client.style = null
        client.addClass(cssRule)
    }

    override fun addStyleClass(cssRule: CssRule) {
        client.addClass(cssRule)
    }

    override fun removeStyleClass(cssRule: CssRule) {
        client.removeClass(cssRule)
    }
}

interface Link : StylableNode {
    var attachedArrow: Arrow?
    fun attachArrow(arrow: Arrow)

    val styleProxy: StyleProxy

    override fun addStyleClass(cssRule: CssRule) {
        styleProxy.addStyleClass(cssRule)
    }

    override fun setStyleClass(cssRule: CssRule) {
        styleProxy.setStyleClass(cssRule)
    }

    override fun removeStyleClass(cssRule: CssRule) {
        styleProxy.removeStyleClass(cssRule)
    }
}

class Arrow : Path(), StylableNode {
    private val styleProxy: StyleProxy = StyleProxy(this)

    override fun addStyleClass(cssRule: CssRule) {
        styleProxy.addStyleClass(cssRule)
    }

    override fun setStyleClass(cssRule: CssRule) {
        styleProxy.setStyleClass(cssRule)
    }

    override fun removeStyleClass(cssRule: CssRule) {
        styleProxy.removeStyleClass(cssRule)
    }

    init {
        elements.add(MoveTo(0.0, 0.0))
        elements.add(LineTo(-5.0, 5.0))
        elements.add(MoveTo(0.0, 0.0))
        elements.add(LineTo(-5.0, -5.0))

        styleProxy.addStyleClass(Styles.chainLinkArrow)
    }
}

class CurvedLink(val from: Node, val to: Node, transform: Transform) : QuadCurve(), Link {
    private val outbound: Bounds
    private val inbound: Bounds

    override var attachedArrow: Arrow? = null
    override var styleProxy: StyleProxy = StyleProxy(this)

    private fun update() {
        val centerMidpointX: Double = (outbound.centerX + inbound.centerX) / 2
        val centerMidpointY: Double = (outbound.centerY + inbound.centerY) / 2
        val centerMidPoint = Point2D(centerMidpointX, centerMidpointY)

        val midpointX: Double = (startX + endX) / 2
        val midpointY: Double = (startY + endY) / 2

        var midPoint   = Point2D(midpointX, midpointY)
        val startPoint = Point2D(startX, startY)

        var angle = MAX_EDGE_CURVE_ANGLE

        val midPoint1 = rotate(midPoint, startPoint, -angle)
        val midPoint2 = rotate(midPoint, startPoint, angle)

        // select the mid point that is further away from the midpoint of the straight line
        // between the two nodes.
        midPoint = listOf(midPoint1, midPoint2).maxByOrNull { it.distance(centerMidPoint) }!!

        controlX = midPoint.x
        controlY = midPoint.y
    }

    override fun attachArrow(arrow: Arrow) {
        attachedArrow = arrow

        /* attach arrow to line's endpoint */
        arrow.translateXProperty().bind(endXProperty())
        arrow.translateYProperty().bind(endYProperty())

        /* rotate arrow around itself based on this line's angle */
        val rotation = Rotate()
        rotation.pivotXProperty().bind(translateXProperty())
        rotation.pivotYProperty().bind(translateYProperty())
        rotation.angleProperty().bind(toDegrees(atan2(endYProperty().subtract(controlYProperty()),
                                                      endXProperty().subtract(controlXProperty()))))
        arrow.transforms.add(rotation)
    }

    private fun getStartPoint(startBounds: Bounds, endBounds: Bounds): Point2D {
        val endPoint = Point2D(endBounds.centerX, endBounds.centerY)

        val deltaX = startBounds.width / 2.0
        val deltaY = startBounds.height / 2.0

        val possibleStartPoints = mutableListOf<Point2D>()
        for (addX in arrayOf(-deltaX, deltaX)) {
            for (addY in arrayOf(-deltaY, deltaY)) {
                possibleStartPoints.add(Point2D(startBounds.centerX + addX, startBounds.centerY + addY))
            }
        }

        return possibleStartPoints.minByOrNull { it.distance(endPoint) }!!
    }

    companion object {
        private const val MAX_EDGE_CURVE_ANGLE = 15.0
    }

    init {
        styleProxy.addStyleClass(Styles.chainLink)

        outbound = bounds(from, transform)
        inbound  = bounds(to, transform)

        val startPoint = getStartPoint(outbound, inbound)
        val endPoint = getStartPoint(inbound, outbound)

        startX = startPoint.x
        startY = startPoint.y

        endX = endPoint.x
        endY = endPoint.y

        update();
    }
}

class StraightLink(val from: Node, val to: Node, transform: Transform) : Line(), Link {
    private val outbound: Bounds
    private val inbound: Bounds

    override var attachedArrow: Arrow? = null
    override var styleProxy: StyleProxy = StyleProxy(this)

    override fun attachArrow(arrow: Arrow) {
        attachedArrow = arrow

        /* attach arrow to line's endpoint */
        arrow.translateXProperty().bind(endXProperty())
        arrow.translateYProperty().bind(endYProperty())

        /* rotate arrow around itself based on this line's angle */
        val rotation = Rotate()
        rotation.pivotXProperty().bind(translateXProperty())
        rotation.pivotYProperty().bind(translateYProperty())
        rotation.angleProperty().bind(toDegrees(atan2(endYProperty().subtract(startYProperty()),
                                                      endXProperty().subtract(startXProperty()))
        ))
        arrow.transforms.add(rotation)
    }

    private fun getStartPoint(startBounds: Bounds, endBounds: Bounds): Point2D {
        val endPoint = Point2D(endBounds.centerX, endBounds.centerY)

        val deltaX = startBounds.width / 2.0
        val deltaY = startBounds.height / 2.0

        val possibleStartPoints = mutableListOf<Point2D>()
        for (addX in arrayOf(-deltaX, 0.0, deltaX)) {
            for (addY in arrayOf(-deltaY, 0.0, deltaY)) {
                possibleStartPoints.add(Point2D(startBounds.centerX + addX, startBounds.centerY + addY))
            }
        }

        return possibleStartPoints.minByOrNull { it.distance(endPoint) }!!
    }

    init {
        styleProxy.addStyleClass(Styles.chainLink)

        outbound = bounds(from, transform)
        inbound  = bounds(to, transform)

        val startPoint = getStartPoint(outbound, inbound) //Point2D(outbound.centerX, outbound.centerY)
        val endPoint = getStartPoint(inbound, outbound) //Point2D(inbound.centerX, inbound.centerY)

        startX = startPoint.x
        startY = startPoint.y

        endX = endPoint.x
        endY = endPoint.y
    }
}

private fun bounds(node: Node, transform: Transform): Bounds {
    val bounds = node.localToScene(node.boundsInLocal)
    return transform.inverseTransform(bounds)
}

private fun atan2(y: ObservableDoubleValue, x: ObservableDoubleValue): DoubleBinding {
    return Bindings.createDoubleBinding({ atan2(y.get(), x.get()) }, y, x)
}

private fun toDegrees(angRad: ObservableDoubleValue): DoubleBinding {
    return Bindings.createDoubleBinding({ toDegrees(angRad.get()) }, angRad)
}

private fun rotate(point: Point2D, pivot: Point2D?, angleDegrees: Double): Point2D {
    val angle = toRadians(angleDegrees)
    val sin   = sin(angle)
    val cos   = cos(angle)

    // translate to origin
    var result = point.subtract(pivot)

    // rotate point
    val rotatedOrigin = Point2D(result.x * cos - result.y * sin, result.x * sin + result.y * cos)

    // translate point back
    result = rotatedOrigin.add(pivot)
    return result
}