package com.thirstwastaken2.client.config;

import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * One tab of a page that is split in several, so a long page is a few short lists instead of one that
 * scrolls. A page that is not split has a single section with no key, and shows no tabs.
 *
 * @param key          {@code <page>.<section>}, naming {@code thirstwastaken2.config.group.<key>}; null for a page's only section
 * @param entries      the settings on this tab, less any whose mod is missing
 * @param trailingRows rows placed after the settings; the {@code Runnable} rebuilds the page, for a row that adds or removes rows
 */
record ConfigSection(String key, List<ConfigEntry<?>> entries, BiConsumer<List<ConfigRow>, Runnable> trailingRows) {
    private static final BiConsumer<List<ConfigRow>, Runnable> NONE = (rows, refresh) -> { };

    /** Drops the settings whose mod is missing, so neither the tab, the search nor Reset sees them. */
    ConfigSection {
        entries = entries.stream().filter(ConfigEntry::isShown).toList();
    }

    /** The only section of a page that is not split. */
    static ConfigSection whole(List<ConfigEntry<?>> entries) {
        return new ConfigSection(null, entries, NONE);
    }

    static ConfigSection of(String key, List<ConfigEntry<?>> entries) {
        return new ConfigSection(key, entries, NONE);
    }

    /** Nothing to show: no settings left, and no rows of its own. */
    boolean isEmpty() {
        return entries.isEmpty() && trailingRows == NONE;
    }

    Component title() {
        return Component.translatable("thirstwastaken2.config.group." + key);
    }
}
