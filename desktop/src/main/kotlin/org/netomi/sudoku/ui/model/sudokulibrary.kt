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
package org.netomi.sudoku.ui.model

import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList

interface TechniqueCategoryOrLibraryEntry
{
    fun isCategory(): Boolean
    fun getLibraryEntry(): LibraryEntry?
    fun toDisplayString(): String
}

object SudokuLibrary
{
    val entries = EnumMap<TechniqueCategory, MutableList<LibraryEntry>>(TechniqueCategory::class.java)

    init {
        this::class.java.getResourceAsStream("/reglib-1.4.txt")?.use { `is` ->
            BufferedReader(InputStreamReader(`is`)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (line!!.startsWith(":")) {
                        val entry = LibraryEntry.of(line!!)
                        val category = TechniqueCategory.of(entry.technique)

                        category?.apply {
                            val list = entries.getOrPut(category, { ArrayList() })
                            list.add(entry)
                        }
                    }
                }
            }
        }
    }
}

enum class TechniqueCategory constructor(private val displayName: String,
                                         private val prefix: String,
                                         private val parent: TechniqueCategory?)
    : TechniqueCategoryOrLibraryEntry
{
    All                 ("All solving techniques", "xxxx", null),
    Singles             ("Singles", "00xx", All),
    FullHouse           ("Full House", "0000", Singles),
    HiddenSingle        ("Hidden Single", "0002", Singles),
    NakedSingle         ("Naked Single", "0003", Singles),
    Intersections       ("Intersections", "01xx", All),
    LockedCandidateType1("Locked Candidate (pointing)", "0100", Intersections),
    LockedCandidateType2("Locked Candidate (claiming)", "0101", Intersections),
    LockedPair          ("Locked Pair", "0110", Intersections),
    LockedTriple        ("Locked Triple", "0111", Intersections),
    Subsets             ("Subsets", "02xx", All),
    NakedPair           ("Naked Pair", "0200", Subsets),
    NakedTriple         ("Naked Triple", "0201", Subsets),
    NakedQuadruple      ("Naked Quadruple", "0202", Subsets),
    HiddenPair          ("Hidden Pair", "0210", Subsets),
    HiddenTriple        ("Hidden Triple", "0211", Subsets),
    HiddenQuadruple     ("Hidden Quadruple", "0212", Subsets),
    BasicFish           ("Basic Fish", "03xx", All),
    XWing               ("X-Wing", "0300", BasicFish),
    Swordfish           ("Swordfish", "0301", BasicFish),
    Jellyfish           ("Jellyfish", "0302", BasicFish),
    SingleDigitPatterns ("Single Digit Patterns", "04xx", All),
    Skyscraper          ("Skyscraper", "0400", SingleDigitPatterns),
    TwoStringKite       ("2-String Kite", "0401", SingleDigitPatterns),
    Chains              ("Chains", "07xx", All),
    RemotePair          ("Remote Pair", "0703", Chains),
    XChain              ("X-Chain", "0701", Chains);

    override fun isCategory(): Boolean {
        return true
    }

    fun hasSubCategories(): Boolean {
        return prefix.contains("x")
    }

    override fun getLibraryEntry(): LibraryEntry? {
        return null
    }

    override fun toDisplayString(): String {
        return displayName
    }

    fun subCategories(): Iterable<TechniqueCategory> {
        return if (hasSubCategories()) {
            values().filter { category -> category.parent === this@TechniqueCategory }
        } else {
            emptyList()
        }
    }

    companion object {
        fun of(technique: String): TechniqueCategory? {
            for (category in values()) {
                if (technique.startsWith(category.prefix)) {
                    return category
                }
            }
            return null
        }
    }
}

class LibraryEntry private constructor(val technique: String,
                                       val candidate: String,
                                       val givens:    String,
                                       deletedCandidatesString: String)
    : TechniqueCategoryOrLibraryEntry
{
    private val deletedCandidates: MutableList<Candidate> = mutableListOf()

    fun getDeletedCandidates(): Collection<Candidate> {
        return deletedCandidates
    }

    override fun isCategory(): Boolean {
        return false
    }

    override fun getLibraryEntry(): LibraryEntry? {
        return this
    }

    override fun toDisplayString(): String {
        return toString()
    }

    override fun toString(): String {
        return ":%s:%s:%s:s".format(technique, candidate, givens, deletedCandidates)
    }

    init {
        for (str in deletedCandidatesString.split(" ").toTypedArray()) {
            if (str.isNotEmpty()) {
                deletedCandidates.add(Candidate.of(str))
            }
        }
    }

    companion object {
        fun of(line: String): LibraryEntry {
            val tokens = line.split(":").toTypedArray()
            return LibraryEntry(tokens[1], tokens[2], tokens[3], tokens[4])
        }
    }
}

class Candidate(val row: Int, val col: Int, val value: Int)
{
    override fun hashCode(): Int {
        return Objects.hash(row, col, value)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val candidate = other as Candidate
        return row == candidate.row && col == candidate.col && value == candidate.value
    }

    fun asPlacement(): String {
        return "r%dc%d=%d".format(row, col, value)
    }

    fun asElimination(): String {
        return toString()
    }

    override fun toString(): String {
        return "r%dc%d<>%d".format(row, col, value)
    }

    companion object {
        fun of(str: String): Candidate {
            val row = ("" + str[1]).toInt()
            val col = ("" + str[2]).toInt()
            val `val` = ("" + str[0]).toInt()
            return Candidate(row, col, `val`)
        }
    }
}