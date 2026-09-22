package com.thirstwastaken2.buildlogic

/**
 * The names `-PwithoutOptional=<name,...>` asks to leave out, lowercased: a mod's Modrinth slug or its
 * mod id, or `all`. Empty when the flag is absent.
 */
fun parseWithoutOptional(value: String?): Set<String> =
    value?.split(',')?.map { it.trim().lowercase() }?.filter(String::isNotEmpty)?.toSet().orEmpty()

/**
 * `-PwithoutOptional=<name,...>` leaves those optional mods out of a node's dev clients, `all` every one
 * of them, so a dev client can show the game loading without them. That is the check 1.0.9 lacked:
 * runClient always had every optional mod, and runServer and runGametest never load a client entrypoint.
 *
 * Each loader script asks [include] for every mod it would add to its clients, and adds the dependency
 * its own way when the answer is yes. A mod that needs a library lists the library's names too, so
 * leaving the library out never leaves a mod that cannot load without it. A name no node offers fails
 * the build in stonecutter.gradle.kts rather than silently leaving the mod in.
 */
class OptionalRunMods(asked: String?) {
    private val without = parseWithoutOptional(asked)
    private val names = mutableSetOf("all")

    /** Every name `-PwithoutOptional` accepts on this node, filled in as [include] is asked. */
    val offered: Set<String> get() = names.toSet()

    /** Records [modNames] as offered, and answers whether the mod goes into the clients. */
    fun include(modNames: List<String>): Boolean {
        names += modNames
        return "all" !in without && modNames.none(without::contains)
    }
}
