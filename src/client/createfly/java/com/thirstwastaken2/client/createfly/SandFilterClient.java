package com.thirstwastaken2.client.createfly;

import com.thirstwastaken2.createfly.SandFilter;
import com.zurrtum.create.client.AllBlockEntityBehaviours;

final class SandFilterClient {
    private SandFilterClient() { }

    static void register() {
        // Create Fly keeps goggle tooltips in client-only behaviours, attached per block entity type.
        AllBlockEntityBehaviours.add(SandFilter.blockEntity(), SandFilterTooltipBehaviour::new);
    }
}
