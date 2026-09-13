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
        //? if >=1.21.2 {
        UseItemCallback.EVENT.register(handler::use);
        //?} else {
        /*// Before 1.21.2 the callback also returns the stack left in the hand, which is whatever the
        // handler put there.
        UseItemCallback.EVENT.register((player, level, hand) -> new net.minecraft.world.InteractionResultHolder<>(
                handler.use(player, level, hand), player.getItemInHand(hand)));
        *///?}
    }

    /** Runs whenever the server builds its command tree, including on {@code /reload}. */
    public static void onRegisterCommands(Consumer<CommandDispatcher<CommandSourceStack>> handler) {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> handler.accept(dispatcher));
    }

    /**
     * Runs for every loot table as it loads, handing over the table's id and a way to append a pool,
     * whoever wrote the table: vanilla, a mod, or a data pack that replaced it.
     *
     * <p>Fabric API does say where a table came from, but not which pack, so vanilla's own experiment
     * packs cannot be told apart from a player's data pack, and its answer for them changed between
     * versions. Taking every table is the one rule that holds on every version and every loader.
     */
    public static void onLootTable(BiConsumer<ResourceKey<LootTable>, Consumer<LootPool.Builder>> handler) {
        LootTableEvents.MODIFY.register((key, table, source, registries) -> handler.accept(key, table::withPool));
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
