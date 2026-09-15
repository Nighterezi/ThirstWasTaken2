package com.thirstwastaken2.platform;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.Codec;
import com.thirstwastaken2.ThirstWasTaken2;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Every call into the mod loader, for NeoForge.
 *
 * <p>Each loader has its own copy of this class, with the same name and the same signatures, in its
 * own source directory; the build compiles exactly one of them. Common code calls it without knowing
 * which one. See {@code platform/AGENTS.md}.
 *
 * <p>Registration listens on the mod's own event bus, everything else on {@link NeoForge#EVENT_BUS}.
 * The mod bus is looked up from {@link ModList} rather than handed over by the entrypoint: the
 * container exists before the mod is constructed, and a lookup keeps the entrypoint to one call, the
 * same as on Fabric.
 */
public final class Loader {
    /** Registrations waiting for their registry's {@link RegisterEvent}, by registry. */
    private static final Map<ResourceKey<? extends Registry<?>>, List<Runnable>> PENDING = new HashMap<>();
    /** Registries whose event has already fired; a registration for one of them would never run. */
    private static final Set<ResourceKey<? extends Registry<?>>> REGISTERED = new HashSet<>();
    private static boolean listening;

    private Loader() { }

    /** True under every development run task, false in the jar players install. */
    public static boolean isDevelopmentEnvironment() {
        return !FMLEnvironment.isProduction();
    }

    /** The directory config files are read from and written to. */
    public static Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    /**
     * Runs {@code registration} when {@code registry} accepts new entries.
     *
     * <p>NeoForge freezes the built-in registries before mods are constructed and opens them again
     * while it fires {@link RegisterEvent}, so the registration is queued and run from that event for
     * {@code registry}. NeoForge fires data component types ahead of items on purpose, which is the
     * order the mod needs.
     */
    public static void onRegister(ResourceKey<? extends Registry<?>> registry, Runnable registration) {
        if (REGISTERED.contains(registry)) {
            throw new IllegalStateException("Registration for " + registry.identifier() + " arrived after its event");
        }
        if (!listening) {
            modBus().addListener(Loader::register);
            listening = true;
        }
        PENDING.computeIfAbsent(registry, key -> new ArrayList<>()).add(registration);
    }

    /** Registers a per-player value, saved with {@code codec} and synced to its owner with {@code streamCodec}. */
    public static <T> PlayerData<T> playerData(Identifier id, Supplier<T> initial, Codec<T> codec,
                                               StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        // An attachment type takes no registry holder, so it can be built now and registered later.
        AttachmentType<T> type = AttachmentType.builder(initial)
                .serialize(codec.fieldOf("value"))
                .sync((holder, to) -> holder == to, streamCodec)
                .build();
        onRegister(NeoForgeRegistries.Keys.ATTACHMENT_TYPES,
                () -> Registry.register(NeoForgeRegistries.ATTACHMENT_TYPES, id, type));
        return new AttachmentPlayerData<>(type);
    }

    /** Builder for a creative tab that finds its own place in the tab list. */
    public static CreativeModeTab.Builder creativeTabBuilder() {
        return CreativeModeTab.builder();
    }

    /** Runs at the end of every server tick. */
    public static void onServerTickEnd(Consumer<MinecraftServer> handler) {
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> handler.accept(event.getServer()));
    }

    /**
     * On Fabric a non-{@code PASS} result stops the callback chain and becomes the interaction's result.
     * Cancelling with that result does both here: listeners added this way skip a cancelled event, and
     * the game returns the cancellation result instead of using the block.
     */
    public static void onUseBlock(UseBlockHandler handler) {
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> {
            InteractionResult result = handler.use(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
            if (result != InteractionResult.PASS) {
                event.setCancellationResult(result);
                event.setCanceled(true);
            }
        });
    }

    /** The same contract as {@link #onUseBlock}. */
    public static void onUseItem(UseItemHandler handler) {
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickItem event) -> {
            InteractionResult result = handler.use(event.getEntity(), event.getLevel(), event.getHand());
            if (result != InteractionResult.PASS) {
                event.setCancellationResult(result);
                event.setCanceled(true);
            }
        });
    }

    /**
     * Runs after tags are bound to their registries: on the server at startup and on {@code /reload},
     * and on a client each time it joins a server and receives that server's tags.
     */
    public static void onTagsLoaded(Runnable handler) {
        NeoForge.EVENT_BUS.addListener((TagsUpdatedEvent event) -> handler.run());
    }

    /** Runs whenever the server builds its command tree, including on {@code /reload}. */
    public static void onRegisterCommands(Consumer<CommandDispatcher<CommandSourceStack>> handler) {
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> handler.accept(event.getDispatcher()));
    }

    /**
     * Runs for every loot table as it loads, handing over the table's id and a way to append a pool,
     * whoever wrote the table: vanilla, a mod, or a data pack that replaced it.
     */
    public static void onLootTable(BiConsumer<ResourceKey<LootTable>, Consumer<LootPool.Builder>> handler) {
        NeoForge.EVENT_BUS.addListener((LootTableLoadEvent event) ->
                handler.accept(event.getKey(), pool -> event.getTable().addPool(pool.build())));
    }

    private static IEventBus modBus() {
        return ModList.get().getModContainerById(ThirstWasTaken2.MOD_ID).orElseThrow().getEventBus();
    }

    private static void register(RegisterEvent event) {
        REGISTERED.add(event.getRegistryKey());
        List<Runnable> registrations = PENDING.remove(event.getRegistryKey());
        if (registrations != null) registrations.forEach(Runnable::run);
    }

    private record AttachmentPlayerData<T>(AttachmentType<T> type) implements PlayerData<T> {
        @Override
        public T get(Player player) {
            return player.getData(type);
        }

        @Override
        public void set(Player player, T value) {
            player.setData(type, value);
        }
    }
}
