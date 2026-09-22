package com.thirstwastaken2.api;

import com.thirstwastaken2.ThirstWasTaken2;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Callbacks another mod can register to change what drinking and exhaustion do. Plain listener lists,
 * the same on Fabric and NeoForge, so nothing here names a mod loader.
 *
 * <p>Both fire on the server only. A listener that throws is logged once and skipped; it cannot take the
 * tick down. Register during mod initialization; registering later works but is not free, since the
 * invoker is rebuilt each time.
 *
 * <pre>{@code
 * ThirstEvents.DRINK.register((player, stack, drink) -> {
 *     if (stack.is(MyItems.SALTY_SNACK)) drink.cancel();
 * });
 * ThirstEvents.EXHAUSTION.register((player, amount) -> player.isInWater() ? amount * 0.5F : amount);
 * }</pre>
 */
public final class ThirstEvents {
    /**
     * An item is drunk or eaten and is about to restore thirst: after its value is resolved, before it
     * is applied. Covers every way the mod drinks an item, integrations included. Drinking water by hand
     * from a block fires it too, with {@link ItemStack#EMPTY}. Salt water restores nothing and does not
     * fire it. {@link ThirstApi#drink} does not fire it either; that call already says how much.
     */
    public static final Event<Drink> DRINK = new Event<>((player, stack, drink) -> { }, listeners ->
            (player, stack, drink) -> {
                for (Drink listener : listeners) {
                    try {
                        listener.onDrink(player, stack, drink);
                    } catch (RuntimeException e) {
                        failed(listener, e);
                    }
                }
            });

    /**
     * The exhaustion a player built up this tick, before the climate, armour and Fire Resistance
     * modifier is applied. Fires at most once per player per tick, and only on a tick where the amount
     * is not zero. The amount can be negative: the Hunger effect's own charge is refunded here. Return
     * what should be charged instead; a result that is not a finite number is ignored.
     */
    public static final Event<Exhaustion> EXHAUSTION = new Event<>((player, amount) -> amount, listeners ->
            (player, amount) -> {
                float result = amount;
                for (Exhaustion listener : listeners) {
                    try {
                        float next = listener.onExhaustion(player, result);
                        if (Float.isFinite(next)) result = next;
                    } catch (RuntimeException e) {
                        failed(listener, e);
                    }
                }
                return result;
            });

    /** Listeners that already threw once, so a listener that fails every tick logs once. */
    private static final Set<Object> FAILED = ConcurrentHashMap.newKeySet();

    private ThirstEvents() { }

    @FunctionalInterface
    public interface Drink {
        /**
         * @param stack what was drunk, as it was before being used up; empty for water drunk by hand
         * @param drink what it restores, which the listener may change or cancel
         */
        void onDrink(Player player, ItemStack stack, DrinkAmounts drink);
    }

    @FunctionalInterface
    public interface Exhaustion {
        /** @return the exhaustion to charge instead of {@code amount} */
        float onExhaustion(Player player, float amount);
    }

    /**
     * What a drink restores, open to change by {@link #DRINK} listeners. Every listener sees the values
     * the listeners before it left, and every listener runs, a cancelled drink included.
     */
    public static final class DrinkAmounts {
        private int thirst;
        private int quenched;
        private boolean cancelled;

        public DrinkAmounts(int thirst, int quenched) {
            this.thirst = thirst;
            this.quenched = quenched;
        }

        public int thirst() {
            return thirst;
        }

        public int quenched() {
            return quenched;
        }

        /** Clamped to {@code 0..}{@link ThirstApi#maxThirst()}. */
        public void setThirst(int thirst) {
            this.thirst = clamp(thirst);
        }

        /** Clamped to {@code 0..}{@link ThirstApi#maxThirst()}; the result is capped at thirst as usual. */
        public void setQuenched(int quenched) {
            this.quenched = clamp(quenched);
        }

        /** The drink restores nothing. Its other effects, a potion's or a food's, still apply. */
        public void cancel() {
            cancelled = true;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        private static int clamp(int value) {
            return Math.max(0, Math.min(ThirstApi.maxThirst(), value));
        }
    }

    /**
     * A list of listeners of type {@code T}. {@link #invoker()} is what ThirstWasTaken2 calls; it runs
     * every listener in registration order. With none registered it is a constant that does nothing.
     */
    public static final class Event<T> {
        private final Function<List<T>, T> combine;
        private final List<T> listeners = new ArrayList<>();
        private volatile T invoker;
        private volatile boolean hasListeners;

        private Event(T empty, Function<List<T>, T> combine) {
            this.combine = combine;
            this.invoker = empty;
        }

        public synchronized void register(T listener) {
            if (listener == null) throw new NullPointerException("listener");
            listeners.add(listener);
            invoker = combine.apply(Collections.unmodifiableList(new ArrayList<>(listeners)));
            hasListeners = true;
        }

        public T invoker() {
            return invoker;
        }

        /** Whether anything is registered, so a hot path can skip building the event's arguments. */
        public boolean hasListeners() {
            return hasListeners;
        }
    }

    private static void failed(Object listener, RuntimeException e) {
        if (FAILED.add(listener)) {
            ThirstWasTaken2.LOGGER.error("A ThirstWasTaken2 event listener threw and was skipped: {}", listener, e);
        }
    }
}
