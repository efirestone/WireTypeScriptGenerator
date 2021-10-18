package com.codellyrandom.wiretypescriptgenerator

import com.codellyrandom.wiretypescriptgenerator.ImportPathCalculator
import com.codellyrandom.wiretypescriptgenerator.TypeResolver
import com.squareup.wire.schema.ProtoType
import kotlin.test.Test
import kotlin.test.assertEquals

class ImportPathCalculatorTests {

    @Test
    fun importTypeInSameDirectory() {
        val source = ProtoType.get("com.simpsons.kids.Bart")
        val typeResolver = TypeResolver()
        val calculator = ImportPathCalculator(source, typeResolver)

        val destination = ProtoType.get("com.simpsons.kids.Lisa")
        assertEquals("import Lisa from \"./Lisa\"", calculator.importStatementFor(destination))
    }

    @Test
    fun importTypeInPeerDirectory() {
        val source = ProtoType.get("com.simpsons.kids.Bart")
        val typeResolver = TypeResolver()
        val calculator = ImportPathCalculator(source, typeResolver)

        val destination = ProtoType.get("com.simpsons.babies.Maggie")
        assertEquals("import Maggie from \"../babies/Maggie\"", calculator.importStatementFor(destination))
    }

    @Test
    fun importTypeInShallowerDirectory() {
        val source = ProtoType.get("com.simpsons.kids.Bart")
        val typeResolver = TypeResolver()
        val calculator = ImportPathCalculator(source, typeResolver)

        val destination = ProtoType.get("com.simpsons.Homer")
        assertEquals("import Homer from \"../Homer\"", calculator.importStatementFor(destination))
    }

    @Test
    fun importTypeInDeeperDirectory() {
        val source = ProtoType.get("com.simpsons.kids.Bart")
        val typeResolver = TypeResolver()
        val calculator = ImportPathCalculator(source, typeResolver)

        val destination = ProtoType.get("com.simpsons.kids.toys.Krusty")
        assertEquals("import Krusty from \"./toys/Krusty\"", calculator.importStatementFor(destination))
    }

}