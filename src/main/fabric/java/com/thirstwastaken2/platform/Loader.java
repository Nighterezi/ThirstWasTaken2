package com.thirstwastaken2.platform;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Every call into the mod loader, for Fabric.
 *
 * <p>Each loader has its own copy of this class, with the same name and the same signatures, in its
 * own source directory; the build compiles exactly one of them. Common code calls it without knowing
 * which one. See {@code platform/AGENTS.md}.
 */
public final class Loader {
    private Loader() { }

    /** True under every development run task, false in the jar players install. */
    public static boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    /** The directory config files are read from and written to. */
    public static Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    /** Registers a per-player value, saved with {@code codec} and synced to its owner with {@code streamCodec}. */
    public static <T> PlayerData<T> playerData(Identifier id, Supplier<T> initial, Codec<T> codec,
                                               StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        AttachmentType<T> type = AttachmentRegistry.create(id, builder -> builder
                .initializer(initial)
                .persistent(codec)
                .syncWith(streamCodec, AttachmentSyncPredicate.targetOnly()));
        return new AttachmentPlayerData<>(type);
    }

    /**
     * Builder for a creative tab that finds its own place in the tab list. Fabric API renamed this
     * entrypoint from {@code FabricItemGroup} to {@code FabricCreativeModeTab} in 26.1.
     */
    public static CreativeModeTab.Builder creativeTabBuilder() {
        //? if >=26.1 {
        return net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab.builder();
        //?} else {
        /*return net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup.builder();
        *///?}
    }

    /** Runs at the end of every server tick. */
    public static void onServerTickEnd(Consumer<MinecraftServer> handler) {
        ServerTickEvents.END_SERVER_TICK.register(handler::accept);
    }

    public static void onUseBlock(UseBlockHandler handler) {
        UseBlockCallback.EVENT.register(handler::use);
    }

    public static void onUseItem(UseItemHandler handler) {
        UseItemCallback.EVENT.register(handler::use);
    }

    /** Runs whenever the server builds its command tree, including on {@code /reload}. */
    public static void onRegisterCommands(Consumer<CommandDispatcher<CommandSourceStack>> handler) {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> handler.accept(dispatcher));
    }

    /**
     * Runs for every loot table as it loads, handing over the table's id and a way to append a pool.
     * Tables a datapack replaced are skipped, so a pack's own version is left exactly as written.
     */
    public static void onBuiltinLootTable(BiConsumer<ResourceKey<LootTable>, Consumer<LootPool.Builder>> handler) {
        LootTableEvents.MODIFY.register((key, table, source, registries) -> {
            if (source.isBuiltin()) handler.accept(key, table::withPool);
        });
    }

    private record AttachmentPlayerData<T>(AttachmentType<T> type) implements PlayerData<T> {
        @Override
        public T get(Player player) {
            return player.getAttachedOrCreate(type);
        }

        @Override
        public void set(Player player, T value) {
            player.setAttached(type, value);
        }
    }
}
