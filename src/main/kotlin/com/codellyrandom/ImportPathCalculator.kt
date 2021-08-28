package com.codellyrandom

import com.squareup.wire.schema.ProtoType

class ImportPathCalculator(sourceType: ProtoType, private val typeResolver: TypeResolver) {

    private val packageComponents: List<String>

    init {
        this.packageComponents = sourceType.packageComponents
    }

    fun importStatementFor(destinationType: ProtoType): String {
        // Convert package/directory structure to be relative to
        // the current package/directory.
        val destinationPackageComponents = destinationType.packageComponents
        val relativePackage = destinationPackageComponents.foldIndexed(mutableListOf<String>()) { index, acc, component ->
            if (acc.isNotEmpty() || index >= packageComponents.size || component != packageComponents[index]) {
                acc.add(component)
            }
            acc
        }

        // Turn the components into a relative path compared this file.
        val commonComponentCount = destinationPackageComponents.size - relativePackage.size
        var backingOutPath = (1 .. (packageComponents.size - commonComponentCount)).map { ".." }
        if (backingOutPath.isEmpty()) {
            backingOutPath = listOf(".")
        }
        val importPath = backingOutPath.plus(relativePackage).joinToString("/")

        val nameComponents = destinationType.nameComponents
        val typeName = typeResolver.nameFor(destinationType)
        return if (nameComponents.size == 1) {
            // Each top-level type gets its own file and is the default export from that file.
            "import $typeName from \"$importPath/${nameComponents[0]}\""
        } else {
            // This is a nested type.
            "import { $typeName } from \"$importPath/${nameComponents[0]}\""
        }
    }

}

// The nested type names, like ["Foo", "Bar"] in "com.codellyrandom.Foo.Bar"
private val ProtoType.nameComponents: List<String>
    get() {
        return this.toString()
            .split(".")
            .filter { it[0].isUpperCase() }
    }

// The package, like ["com", "codellyrandom"] in "com.codellyrandom.Foo.Bar"
private val ProtoType.packageComponents: List<String>
    get() {
        return this.toString()
            .split(".")
            .filter { it[0].isLowerCase() }
    }