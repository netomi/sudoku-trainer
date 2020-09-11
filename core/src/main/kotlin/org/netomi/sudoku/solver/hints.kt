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
package org.netomi.sudoku.solver

import org.netomi.sudoku.model.*
import java.util.*

class AssignmentHint(type:             Grid.Type,
                     solvingTechnique: SolvingTechnique,
                     val cellIndex:    Int,
                     relatedCells:     CellSet,
                     val value:        Int) : Hint(type, solvingTechnique, relatedCells)
{
    override val description: String
        get() = "%s=%d".format(gridType.getCellName(cellIndex), value)

    override fun apply(targetGrid: Grid, updateGrid: Boolean) {
        targetGrid.getCell(cellIndex).setValue(value, updateGrid)
    }

    override fun revert(targetGrid: Grid, updateGrid: Boolean) {
        targetGrid.getCell(cellIndex).setValue(0, updateGrid)
    }

    override fun accept(visitor: HintVisitor) {
        visitor.visitAssignmentHint(this)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + Objects.hash(cellIndex, value)
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as AssignmentHint
        return super.equals(other) &&
               cellIndex    == that.cellIndex &&
               value        == that.value
    }
}

open class EliminationHint(type:               Grid.Type,
                           solvingTechnique:   SolvingTechnique,
                           val matchingCells:  CellSet,
                           val matchingValues: ValueSet,
                           relatedCells:       CellSet,
                           val affectedCells:  CellSet,
                           val excludedValues: Array<ValueSet>)
    : Hint(type, solvingTechnique, relatedCells)
{
    constructor(type:             Grid.Type,
                solvingTechnique: SolvingTechnique,
                matchingCells:    CellSet,
                matchingValues:   ValueSet,
                relatedCells:     CellSet,
                affectedCells:    CellSet,
                excludedValues:   ValueSet) :
            this(type,
                 solvingTechnique,
                 matchingCells,
                 matchingValues,
                 relatedCells,
                 affectedCells,
                 expand(excludedValues, affectedCells.cardinality()))

    override val description: String
        get() {
            val values = matchingValues.allSetBits().joinToString ("/") { it.toString() }
            val cells  = matchingCells.allSetBits().joinToString { gridType.getCellName(it) }

            val eliminations = StringBuilder()
            for ((index, cellIndex) in affectedCells.allSetBits().withIndex()) {
                eliminations.append(gridType.getCellName(cellIndex))
                eliminations.append("<>")
                eliminations.append(excludedValues[index].toCollection())
                eliminations.append(", ")
            }
            eliminations.delete(eliminations.length - 2, eliminations.length)
            return "%s in %s => %s".format(values, cells, eliminations)
        }

    override fun apply(targetGrid: Grid, updateGrid: Boolean) {
        for ((index, cell) in affectedCells.allCells(targetGrid).withIndex()) {
            cell.excludePossibleValues(excludedValues[index], updateGrid)
        }
    }

    override fun revert(targetGrid: Grid, updateGrid: Boolean) {
        for ((index, cell) in affectedCells.allCells(targetGrid).withIndex()) {
            cell.removeExcludedPossibleValues(excludedValues[index], updateGrid)
        }
    }

    override fun accept(visitor: HintVisitor) {
        visitor.visitEliminationHint(this)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + Objects.hash(affectedCells)
        result = 31 * result + excludedValues.contentHashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as EliminationHint
        return super.equals(other) &&
               affectedCells == that.affectedCells &&
               excludedValues.contentEquals(that.excludedValues)
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        private fun expand(values: ValueSet, copies: Int): Array<ValueSet> {
            val result = arrayOfNulls<ValueSet>(copies)
            for (i in 0 until copies) {
                result[i] = values
            }
            return result as Array<ValueSet>
        }
    }
}

class ChainEliminationHint(type:             Grid.Type,
                           solvingTechnique: SolvingTechnique,
                           matchingCells:    CellSet,
                           matchingValues:   ValueSet,
                           relatedCells:     CellSet,
                           var relatedChain: Chain,
                           affectedCells:    CellSet,
                           excludedValues:   Array<ValueSet>)
    : EliminationHint(type, solvingTechnique, matchingCells, matchingValues, relatedCells, affectedCells, excludedValues)
{
    override val description: String
        get() {
            val eliminations = StringBuilder()
            for ((index, cellIndex) in affectedCells.allSetBits().withIndex()) {
                eliminations.append(gridType.getCellName(cellIndex))
                eliminations.append("<>")
                eliminations.append(excludedValues[index].toCollection())
                eliminations.append(", ")
            }
            eliminations.delete(eliminations.length - 2, eliminations.length)
            return "%s => %s".format(relatedChain.toString(gridType), eliminations)
        }

    override fun accept(visitor: HintVisitor) {
        visitor.visitChainEliminationHint(this)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + Objects.hash(affectedCells)
        result = 31 * result + excludedValues.contentHashCode()
        result = 31 * result + relatedChain.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ChainEliminationHint
        return super.equals(other) &&
               affectedCells == that.affectedCells &&
               excludedValues.contentEquals(that.excludedValues) &&
               relatedChain == that.relatedChain
    }
}

class Chain() {
    lateinit var rootNode: ChainNode
        private set

    lateinit var lastNode: ChainNode
        private set

    lateinit var cellSet: MutableCellSet
        private set

    constructor(grid: Grid, cellIndex: Int, candidate: Int): this() {
        rootNode = ChainNode(cellIndex, candidate)
        lastNode = rootNode
        cellSet  = MutableCellSet.empty(grid)
        cellSet.set(cellIndex)
    }

    private constructor(other: Chain): this() {
        rootNode = other.rootNode.copy()
        cellSet  = other.cellSet.copy()

        var currentNode = rootNode
        var otherNode   = other.rootNode
        while (otherNode.nextLink != null) {
            val link = otherNode.nextLink!!

            val nextNode = link.node.copy()
            currentNode.nextLink = ChainLink(link.linkType, nextNode)
            nextNode.prevNode    = currentNode

            currentNode = nextNode
            otherNode   = link.node
        }

        lastNode = currentNode
    }

    fun lastLinkType(): LinkType? {
        return lastNode.prevNode?.nextLink?.linkType
    }

    fun addLink(linkType: LinkType, cellIndex: Int, candidate: Int) {
        cellSet.set(cellIndex)

        val nextNode = ChainNode(cellIndex, candidate)
        lastNode.nextLink = ChainLink(linkType, nextNode)
        nextNode.prevNode = lastNode
        lastNode = nextNode
    }

    fun removeLastLink() {
        require(lastNode !== rootNode) { "not allowed to remove root node from chain " }

        cellSet.clear(lastNode.cellIndex)

        lastNode.prevNode?.apply {
            lastNode = this
            lastNode.nextLink = null
        }
    }

    fun contains(cell: Cell): Boolean {
        return cellSet[cell.cellIndex]
    }

    val length: Int
        get() {
        var len = 0
        var currentNode: ChainNode? = rootNode
        while (currentNode != null) {
            len++
            currentNode = currentNode.nextLink?.node
        }
        return len
    }

    fun copy(): Chain {
        return Chain(this)
    }

    fun accept(grid: Grid, visitor: ChainVisitor) {
        var currentNode      = rootNode
        var currentCell      = grid.getCell(currentNode.cellIndex)
        var currentCandidate = currentNode.candidate

        var activeValues   = MutableValueSet.empty(grid)
        var inactiveValues = MutableValueSet.empty(grid)

        var active = currentNode.nextLink?.linkType == LinkType.WEAK

        if (active) activeValues.set(currentCandidate) else inactiveValues.set(currentCandidate)

        while (currentNode.nextLink != null) {
            val link          = currentNode.nextLink!!
            val nextNode      = link.node
            val nextCandidate = nextNode.candidate

            // ensure that the chain is correctly constructed,
            // strong links can only be followed if the current candidate is false
            // weak links can only be followed if the current candidate is true
            require(link.linkType == LinkType.STRONG && !active ||
                    link.linkType == LinkType.WEAK   && active)

            val nextCell   = grid.getCell(nextNode.cellIndex)
            val nextActive = !active

            if (currentCell != nextCell) {
                visitor.visitCell(grid, this, currentCell, activeValues, inactiveValues)
                visitor.visitCellLink(grid, this, currentCell, currentCandidate, nextCell, nextCandidate, link.linkType)

                activeValues   = MutableValueSet.empty(grid)
                inactiveValues = MutableValueSet.empty(grid)
            }

            if (nextActive) activeValues.set(nextCandidate) else inactiveValues.set(nextCandidate)

            active           = nextActive
            currentNode      = link.node
            currentCell      = nextCell
            currentCandidate = nextCandidate
        }

        visitor.visitCell(grid, this, currentCell, activeValues, inactiveValues)
    }

    fun toString(type: Grid.Type): String {
        return rootNode.toString(type)
    }
}

class ChainNode(val cellIndex: Int,
                val candidate: Int)
{
    internal var prevNode: ChainNode? = null
    internal var nextLink: ChainLink? = null

    fun copy(): ChainNode {
        return ChainNode(cellIndex, candidate)
    }

    fun toString(type: Grid.Type): String {
        val sb = StringBuilder()

        sb.append(type.getCellName(cellIndex))
        sb.append("=")
        sb.append(candidate.toString())

        nextLink?.apply {
            sb.append(this.toString(type))
        }

        return sb.toString()
    }
}

class ChainLink(val linkType: LinkType,
                val node:     ChainNode)
{
    fun toString(type: Grid.Type): String {
        return " %s %s".format(linkType.symbol, node.toString(type))
    }
}

interface ChainVisitor
{
    fun visitCell(grid:           Grid,
                  chain:          Chain,
                  cell:           Cell,
                  activeValues:   ValueSet,
                  inactiveValues: ValueSet)

    fun visitCellLink(grid:          Grid,
                      chain:         Chain,
                      fromCell:      Cell,
                      fromCandidate: Int,
                      toCell:        Cell,
                      toCandidate:   Int,
                      linkType:      LinkType)
}

enum class LinkType(val symbol: String)
{
    WEAK  ("->") {
        override fun opposite() = STRONG
    },
    STRONG("=>") {
        override fun opposite() = WEAK
    };

    abstract fun opposite(): LinkType
}
