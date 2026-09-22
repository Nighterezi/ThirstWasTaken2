/*
 * The part of the build every node shares, whatever mod loader it builds for: the Java toolchain,
 * the two seam checks, what never belongs in a jar, and the task that collects the jars.
 *
 * Applied by both `build.gradle.kts` (the Fabric nodes) and `build.neoforge.gradle.kts`. Each of
 * those sets the `thirst.requiredJava` extra property first, because the Java version a node needs
 * follows from its Minecraft version and only the node's own script can read that.
 *
 * Two things are deliberately not here:
 *
 * - **The source directory wiring.** Loom splits `main` and `client`; ModDevGradle has no split, so
 *   the NeoForge node compiles the client sources into `main`. The two scripts wire their source
 *   sets differently on purpose, as build.neoforge.gradle.kts explains.
 * - **What `buildAndCollect` copies.** The jar a node ships is Loom's remapped jar on Fabric and the
 *   plain `jar` on NeoForge, so each script adds its own inputs to the task registered below.
 */

/** Set by the applying script, from the Minecraft version of its node. */
val requiredJava: JavaVersion =
    JavaVersion.toVersion(project.extensions.extraProperties.get("thirst.requiredJava") as String)

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(requiredJava.majorVersion.toInt())
}

configure<JavaPluginExtension> {
    withSourcesJar()
    sourceCompatibility = requiredJava
    targetCompatibility = requiredJava

    toolchain {
        vendor.set(JvmVendorSpec.ADOPTIUM)
        languageVersion.set(JavaLanguageVersion.of(requiredJava.majorVersion))
    }
}

// Per-directory notes for contributors and backup copies of edited textures; they live next to the
// files they describe, not in the jars.
tasks.withType<ProcessResources>().configureEach {
    exclude("**/AGENTS.md", "**/*.bak")
}

/**
 * Prepares what `runBenchmark` writes into and works in: the directory the report and any flight
 * recording land in, and a world whose terrain is the same on every machine.
 *
 * The benchmark opens a world of its own, `-Pthirst.benchmark.world`, passed to the server as
 * `--world` by each node's own buildscript; only the seed it is generated with lives here, because
 * a seed is read from `server.properties` and nothing on the command line can set it.
 *
 * Only an empty or missing `level-seed` is filled in, so a seed someone put there by hand is left
 * alone, and only a world created afterwards is affected: the dev world `runServer` opens already
 * exists with a seed of its own inside its `level.dat`, and is untouched either way. The report's
 * `environment.levelSeed` says what a run actually measured, so a reader never has to trust this.
 */
// Not `prepareBenchmarkRun`: ModDevGradle already names a task of its own that, for the
// `benchmark` run it creates.
tasks.register("benchmarkRunDirectory") {
    description = "Makes the benchmark's output directory and fills in its seed when there is none"

    val seed = providers.gradleProperty("thirst.benchmark.seed").get()
    val properties = rootProject.file("run/${project.name}/server.properties")
    // JFR writes its recording as the JVM starts and does not create the directory for it.
    val output = rootProject.file("run/${project.name}/benchmark")
    // The run directory is shared with runServer and edited by the server itself, so this is never
    // up to date in Gradle's sense; it is a few lines of text either way.
    outputs.upToDateWhen { false }

    doLast {
        output.mkdirs()
        val key = "level-seed="
        if (!properties.isFile) {
            properties.parentFile.mkdirs()
            // The server fills in every other property with its default on the first start and keeps
            // this one. It still writes eula.txt and refuses to start until that is accepted by hand.
            properties.writeText(key + seed + System.lineSeparator())
            logger.lifecycle("Wrote $properties with the benchmark seed $seed")
            return@doLast
        }
        val lines = properties.readLines()
        val index = lines.indexOfFirst { it.startsWith(key) }
        if (index >= 0 && lines[index].substring(key.length).isNotBlank()) return@doLast
        val updated = if (index >= 0) lines.toMutableList().also { it[index] = key + seed }
        else lines + (key + seed)
        properties.writeText(updated.joinToString(System.lineSeparator(), postfix = System.lineSeparator()))
        logger.lifecycle("Set the benchmark seed $seed in $properties")
    }
}

// Both plugins create their run tasks while the buildscript is evaluated, and this file is applied at
// the end of it, so the task is matched by name rather than looked up.
tasks.matching { it.name == "runBenchmark" }.configureEach { dependsOn("benchmarkRunDirectory") }

// The NeoForge node has a third check, `checkNeoForgeResources`, in build.neoforge.gradle.kts beside the
// translation of datagen's Fabric-only JSON it guards.

/**
 * Fails when loader independent code names a mod loader. `src/main/java` and `src/client/java` are
 * compiled against Fabric API today, so the compiler cannot catch a Fabric import there; this can.
 * It cannot see the methods Fabric API injects into vanilla classes, such as `getAttachedOrCreate`,
 * which only the first NeoForge build will report.
 */
tasks.register("checkLoaderSeam") {
    group = "verification"
    description = "Fails when loader independent sources import a mod loader's API"

    val roots = listOf("src/main/java", "src/client/java").map(rootProject::file)
    val forbidden = Regex("""\b(net\.fabricmc|net\.neoforged)\.""")
    inputs.files(roots.map { fileTree(it) { include("**/*.java") } })

    doLast {
        val offenders = roots.flatMap { root ->
            root.walk().filter { it.extension == "java" }.flatMap { file ->
                file.readLines().withIndex()
                    .filter { (_, line) -> forbidden.containsMatchIn(line) }
                    .map { (index, line) ->
                        "${file.relativeTo(rootProject.projectDir).invariantSeparatorsPath}:${index + 1}: ${line.trim()}"
                    }
            }
        }
        check(offenders.isEmpty()) {
            "Loader API in loader independent code. Route it through platform/Loader or " +
                "client/platform/ClientLoader instead:\n" + offenders.joinToString("\n")
        }
    }
}

/**
 * Fails when core code carries a Stonecutter version conditional. Minecraft version differences belong
 * in `platform/` and, for injection signatures, `mixin/`; a `//?` block anywhere else in `src/main/java`
 * or `src/client/java` means a seam is missing. This replaced counting blocks as the exit ramp.
 * Loader directories, datagen, gametests and dev tools are outside it.
 */
tasks.register("checkVersionSeam") {
    group = "verification"
    description = "Fails when core sources outside platform/ and mixin/ contain a version conditional"

    val roots = listOf("src/main/java", "src/client/java").map(rootProject::file)
    val allowed = setOf("platform", "mixin")
    inputs.files(roots.map { fileTree(it) { include("**/*.java") } })

    doLast {
        val offenders = roots.flatMap { root ->
            root.walk()
                .filter { it.extension == "java" }
                .filterNot { file -> file.relativeTo(root).invariantSeparatorsPath.split('/').any(allowed::contains) }
                .flatMap { file ->
                    file.readLines().withIndex()
                        .filter { (_, line) -> line.contains("//?") }
                        .map { (index, line) ->
                            "${file.relativeTo(rootProject.projectDir).invariantSeparatorsPath}:${index + 1}: ${line.trim()}"
                        }
                }
        }
        check(offenders.isEmpty()) {
            "Version conditional in core code. Put the difference behind platform/Vanilla or " +
                "client/platform/ClientVanilla instead:\n" + offenders.joinToString("\n")
        }
    }
}

/**
 * Fails when the agent's loader independent half stops being loader independent.
 *
 * `dev/agent/core` is the queue, the envelope and the dispatch loop: plain Java and Gson, and nothing
 * else. That is what makes the settled decision in
 * src/dev/java/com/thirstwastaken2/dev/agent/AGENTS.md true rather than aspirational — one import of Minecraft, of a mod loader or of the mod itself and it no longer lifts
 * out of this project cleanly. `dev/agent/thirst` is the half that is free to name all three.
 */
tasks.register("checkAgentCore") {
    group = "verification"
    description = "Fails when the agent's core package imports Minecraft, a mod loader or the mod"

    val root = rootProject.file("src/dev/java/com/thirstwastaken2/dev/agent/core")
    val forbidden = Regex("""^import\s+(net\.minecraft|net\.fabricmc|net\.neoforged|"""
        + """com\.mojang|com\.thirstwastaken2(?!\.dev\.agent\.core))""")
    inputs.files(fileTree(root) { include("**/*.java") })

    doLast {
        val offenders = root.walk().filter { it.extension == "java" }.flatMap { file ->
            file.readLines().withIndex()
                .filter { (_, line) -> forbidden.containsMatchIn(line.trim()) }
                .map { (index, line) ->
                    "${file.relativeTo(rootProject.projectDir).invariantSeparatorsPath}:${index + 1}: ${line.trim()}"
                }
        }.toList()
        check(offenders.isEmpty()) {
            "The agent's core package is meant to know nothing of Minecraft, of a loader or of this " +
                "mod. Put what needs them in dev/agent/thirst instead:\n" + offenders.joinToString("\n")
        }
    }
}

/**
 * What `checkOptionalSeam` needs to know about one compiled class: its supertypes, what it names in
 * a way the verifier may resolve, and every string in its constant pool (annotations are found there).
 */
class ClassSummary(
    val name: String,
    val supertypes: List<String>,
    /** Classes named in a descriptor, or as a class constant that is not only the owner of a member. */
    val resolved: Set<String>,
    /** Every class it names at all, owners of the members it calls included. */
    val named: Set<String>,
    val strings: Set<String>,
)

/** The object types in a descriptor or an array class name, e.g. `(Lfoo/Bar;[Lbaz/Qux;)V`. */
fun descriptorTypes(descriptor: String): List<String> =
    Regex("""L([^;<]+)[;<]""").findAll(descriptor).map { it.groupValues[1] }.toList()

/**
 * Reads a class file far enough for `checkOptionalSeam`: the constant pool, the supertypes and the
 * field and method descriptors. Written out here rather than taken from ASM, which a script plugin
 * cannot declare a dependency on; the format has not changed in the parts this reads since Java 11.
 */
fun summarise(file: File): ClassSummary = java.io.DataInputStream(file.inputStream().buffered()).use { input ->
    check(input.readInt() == 0xCAFEBABE.toInt()) { "$file is not a class file" }
    input.readUnsignedShort(); input.readUnsignedShort()
    val count = input.readUnsignedShort()
    val utf8 = arrayOfNulls<String>(count)
    val classNameIndex = IntArray(count) { -1 }
    val memberOwners = mutableSetOf<Int>()
    val descriptorIndices = mutableListOf<Int>()
    var index = 1
    while (index < count) {
        when (val tag = input.readUnsignedByte()) {
            1 -> utf8[index] = input.readUTF()
            3, 4 -> input.readInt()
            5, 6 -> { input.readLong(); index++ }
            7 -> classNameIndex[index] = input.readUnsignedShort()
            8, 16, 19, 20 -> {
                val value = input.readUnsignedShort()
                if (tag == 16) descriptorIndices += value
            }
            9, 10, 11 -> { memberOwners += input.readUnsignedShort(); input.readUnsignedShort() }
            12 -> { input.readUnsignedShort(); descriptorIndices += input.readUnsignedShort() }
            15 -> { input.readUnsignedByte(); input.readUnsignedShort() }
            17, 18 -> { input.readUnsignedShort(); input.readUnsignedShort() }
            else -> error("$file: unknown constant pool tag $tag")
        }
        index++
    }
    fun className(classIndex: Int): String = utf8[classNameIndex[classIndex]]!!
    input.readUnsignedShort()
    val self = className(input.readUnsignedShort())
    val superIndex = input.readUnsignedShort()
    val supertypes = mutableListOf<String>()
    if (superIndex != 0) supertypes += className(superIndex)
    repeat(input.readUnsignedShort()) { supertypes += className(input.readUnsignedShort()) }
    repeat(2) {
        repeat(input.readUnsignedShort()) {
            input.readUnsignedShort(); input.readUnsignedShort()
            descriptorIndices += input.readUnsignedShort()
            repeat(input.readUnsignedShort()) {
                input.readUnsignedShort()
                input.skipNBytes(input.readInt().toLong() and 0xFFFFFFFFL)
            }
        }
    }

    fun typesOf(name: String) = if (name.startsWith("[")) descriptorTypes(name) else listOf(name)
    val classes = (1 until count).filter { classNameIndex[it] >= 0 }
    val named = classes.flatMap { typesOf(className(it)) }.toMutableSet()
    val resolved = classes.filterNot { it in memberOwners }.flatMap { typesOf(className(it)) }.toMutableSet()
    descriptorIndices.mapNotNull { utf8[it] }.flatMap(::descriptorTypes).forEach { named += it; resolved += it }
    ClassSummary(self, supertypes, resolved - self, named - self, utf8.filterNotNull().toSet())
}

/**
 * Fails when a class the game loads whether or not an optional mod is installed names that mod.
 *
 * **Why this exists.** 1.0.9 crashed every NeoForge client without Sophisticated Core, from an
 * entrypoint that asked the gate first. The JVM verifies a class whole when it links it, before any
 * code in it runs, and verifying the entrypoint's tab factories loaded a class whose superclass is
 * Sophisticated's. No run could see it: runClient always has every optional mod, and runServer and
 * runGametest never load a client entrypoint. This reads the compiled classes, so a lambda or a
 * generic argument counts the same as an import.
 *
 * **What is always loaded**, the roots: every entrypoint in `fabric.mod.json`, every class carrying
 * `@Mod`, `@EventBusSubscriber` or Jade's `@WailaPlugin`, and every mixin config's plugin. A root may
 * name Minecraft, the JDK, the loaders, the libraries every game ships and this mod's own classes. An
 * entrypoint another mod reads, `jade` or `modmenu`, may also name that mod, since only it loads the
 * class.
 *
 * **What a root must not do**, the two ways 1.0.9 could have happened:
 *
 * - name another mod's class anywhere, even in code a gate keeps from running;
 * - name one of this mod's own classes in a way the verifier resolves (a descriptor, a cast, a
 *   supertype) when that class, or a supertype of it, is another mod's. Calling a static method on
 *   such a class is fine, and is exactly how a gate hands over: the class is only loaded when the call
 *   runs. `DrinkingUpgradeTabs` is that shape.
 *
 * It also fails when core code, `src/main/java` and `src/client/java` and the loader directories, refers
 * to an integration package at all, since those are only compiled on some nodes and name other mods.
 */
tasks.register("checkOptionalSeam") {
    group = "verification"
    description = "Fails when a class loaded without an optional mod installed names that mod"

    val sourceSets = project.extensions.getByType<SourceSetContainer>()
    // `client` is Loom's split source set on Fabric. On NeoForge it only exists to be preprocessed and
    // its classes are compiled into `main`, so it is left out there.
    val checked = listOfNotNull(sourceSets.getByName("main"),
        sourceSets.findByName("client")?.takeIf { project.plugins.hasPlugin("fabric-loom") ||
            project.extensions.findByName("loom") != null })
    dependsOn(checked.map { it.classesTaskName })
    val classDirs = checked.flatMap { it.output.classesDirs.files }
    val resourceDirs = checked.mapNotNull { it.output.resourcesDir }
    inputs.files(classDirs, resourceDirs)

    val coreRoots = listOf("src/main/java", "src/client/java", "src/main/fabric", "src/main/neoforge",
        "src/client/fabric", "src/client/neoforge", "src/main/neoforge-fluidhandler", "src/main/neoforge-transfer")
        .map(rootProject::file)
    inputs.files(coreRoots.map { fileTree(it) { include("**/*.java") } })

    doLast {
        /** What every root may name. A new library the game always ships belongs here. */
        val always = listOf("java/", "javax/", "jdk/", "sun/", "net/minecraft/", "com/mojang/", "org/slf4j/",
            "org/apache/logging/", "com/google/", "it/unimi/", "org/jetbrains/", "org/jspecify/", "io/netty/",
            "org/joml/", "org/lwjgl/", "org/spongepowered/", "org/objectweb/", "com/llamalad7/",
            "net/fabricmc/", "net/neoforged/", "com/thirstwastaken2/")
        /** What a root another mod loads may name as well, keyed by what makes it a root. */
        val loadedBy = mapOf(
            "jade" to listOf("snownee/jade/"),
            "modmenu" to listOf("com/terraformersmc/modmenu/"),
        )

        val summaries = classDirs.filter(File::isDirectory)
            .flatMap { dir -> dir.walk().filter { it.isFile && it.extension == "class" }.toList() }
            .map(::summarise).associateBy { it.name }

        // Roots, each with the packages it may name.
        val roots = mutableMapOf<String, MutableSet<String>>()
        fun root(className: String, loader: String?) {
            roots.getOrPut(className.replace('.', '/')) { always.toMutableSet() }
                .addAll(loader?.let(loadedBy::get).orEmpty())
        }
        resourceDirs.filter(File::isDirectory).forEach { dir ->
            dir.resolve("fabric.mod.json").takeIf(File::isFile)?.let { manifest ->
                @Suppress("UNCHECKED_CAST")
                val entrypoints = (groovy.json.JsonSlurper().parse(manifest) as Map<String, Any?>)["entrypoints"]
                    as Map<String, List<Any?>>? ?: emptyMap()
                entrypoints.forEach { (key, values) ->
                    values.forEach { value ->
                        val name = (if (value is Map<*, *>) value["value"] else value) as String
                        root(name.substringBefore("::"), key)
                    }
                }
            }
            dir.walk().filter { it.isFile && it.name.endsWith(".mixins.json") }.forEach { config ->
                ((groovy.json.JsonSlurper().parse(config) as Map<*, *>)["plugin"] as String?)?.let { root(it, null) }
            }
        }
        summaries.values.forEach { summary ->
            val strings = summary.strings
            if ("Lnet/neoforged/fml/common/Mod;" in strings || "Lnet/neoforged/fml/common/EventBusSubscriber;" in strings) {
                root(summary.name, null)
            }
            if ("Lsnownee/jade/api/WailaPlugin;" in strings) root(summary.name, "jade")
        }

        roots.keys.sorted().forEach { logger.info("checkOptionalSeam root: ${it.replace('/', '.')}") }

        fun allowed(type: String, packages: Set<String>) = packages.any(type::startsWith)
        /** The first of the class's supertypes, all the way up, that a root with these packages may not name. */
        fun foreignAncestor(type: String, packages: Set<String>, seen: MutableSet<String> = mutableSetOf()): String? {
            if (!seen.add(type)) return null
            if (!allowed(type, packages)) return type
            val summary = summaries[type] ?: return null
            return summary.supertypes.firstNotNullOfOrNull { foreignAncestor(it, packages, seen) }
        }

        val offenders = mutableListOf<String>()
        roots.forEach { (name, packages) ->
            val summary = summaries[name]
            if (summary == null) {
                // Named by a manifest, compiled nowhere on this node: the loader would fail on it anyway.
                offenders += "$name: named as an entrypoint or mixin plugin, but no such class was compiled"
                return@forEach
            }
            // The root itself and every class nested in it that the verifier needs are the same file
            // only for lambdas; an inner class is its own root only once something loads it.
            summary.named.filterNot { allowed(it, packages) }.sorted().forEach {
                offenders += "$name names ${it.replace('/', '.')}"
            }
            (summary.resolved + summary.supertypes).filter { allowed(it, packages) }.sorted().forEach { type ->
                foreignAncestor(type, packages)?.takeIf { it != type }?.let { ancestor ->
                    offenders += "$name resolves ${type.replace('/', '.')}, which extends " +
                        "${ancestor.replace('/', '.')}; call into it through a method on another class instead"
                }
            }
        }

        val integration = Regex("""\bcom\.thirstwastaken2\.(client\.)?(create|createfly|sophisticated|supplementaries|kaleidoscope)\b""")
        coreRoots.filter(File::isDirectory).forEach { dir ->
            dir.walk().filter { it.extension == "java" }.forEach { file ->
                file.readLines().forEachIndexed { index, line ->
                    if (integration.containsMatchIn(line)) {
                        offenders += "${file.relativeTo(rootProject.projectDir).invariantSeparatorsPath}:${index + 1}: " +
                            "core code refers to an integration: ${line.trim()}"
                    }
                }
            }
        }

        check(offenders.isEmpty()) {
            "A class the game loads without an optional mod installed would load that mod's classes, which " +
                "crashes a game that does not have it (see the note on checkOptionalSeam in " +
                "gradle/shared.gradle.kts):\n" + offenders.joinToString("\n")
        }
        logger.lifecycle("checkOptionalSeam: ${roots.size} classes loaded without the optional mods, all clean")
    }
}

/** Collects the jars every node produces into one directory, for `chiseledBuild`. */
tasks.register<Copy>("buildAndCollect") {
    group = "build"
    description = "Builds the mod jar and copies it to build/libs/"
    into(rootProject.layout.buildDirectory.dir("libs"))
}
