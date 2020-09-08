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

object SudokuLibrary
{
    val entries: MutableMap<Category, MutableList<LibraryEntry>> = EnumMap(Category::class.java)

    init {
        this::class.java.getResourceAsStream("/reglib-1.4.txt")?.use { `is` ->
            BufferedReader(InputStreamReader(`is`)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (line!!.startsWith(":")) {
                        val entry = LibraryEntry.of(line!!)
                        val category = Category.of(entry.technique)

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

enum class Category constructor(val prefix: String, val parent: Category?)
{
    Singles("00xx", null),
    FullHouse("0000", Singles),
    HiddenSingle("0002", Singles),
    NakedSingle("0003", Singles),
    Intersections("01xx", null),
    LockedCandidateType1("0100", Intersections),
    LockedCandidateType2("0101", Intersections),
    LockedPair("0110", Intersections),
    LockedTriple("0111", Intersections),
    Subsets("02xx", null),
    NakedPair("0200", Subsets),
    NakedTriple("0201", Subsets),
    NakedQuadruple("0202", Subsets),
    HiddenPair("0210", Subsets),
    HiddenTriple("0211", Subsets),
    HiddenQuadruple("0212", Subsets),
    Fish("03xx", null),
    XWing("0300", Fish),
    Swordfish("0301", Fish),
    Jellyfish("0302", Fish);

    companion object {
        fun of(technique: String): Category? {
            for (category in values()) {
                if (technique.startsWith(category.prefix)) {
                    return category
                }
            }
            return null
        }

        fun ofName(name: String): Category? {
            for (category in values()) {
                if (category.name == name) {
                    return category
                }
            }
            return null
        }
    }
}

class LibraryEntry private constructor(val technique: String,
                                       val candidate: String,
                                       val givens:    String)
{
    override fun toString(): String {
        return ":%s:%s:%s".format(technique, candidate, givens)
    }

    companion object {
        fun of(line: String): LibraryEntry {
            val tokens = line.split(":").toTypedArray()
            return LibraryEntry(tokens[1], tokens[2], tokens[3])
        }
    }
}
