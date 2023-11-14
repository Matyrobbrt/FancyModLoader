/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.fml.javafmlmod;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.parameters.IModParameter;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class EventBusParameter implements IModParameter<IEventBus> {
    @Nullable
    @Override
    public IEventBus provide(ModContainer container) {
        return container instanceof FMLModContainer modContainer ? modContainer.getEventBus() : null;
    }

    public static final Set<Class<? super IEventBus>> TYPES = Set.of(IEventBus.class);
    @Override
    public Set<Class<? super IEventBus>> types(ModContainer container) {
        return container instanceof FMLModContainer ? TYPES : Set.of();
    }
}
