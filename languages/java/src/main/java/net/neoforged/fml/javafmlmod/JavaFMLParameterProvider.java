/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.fml.javafmlmod;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.parameters.IModParameterBuilder;
import net.neoforged.fml.parameters.IModParameterProvider;

import java.util.Set;

public class JavaFMLParameterProvider implements IModParameterProvider {

    @Override
    public void provide(ModContainer container, IModParameterBuilder builder) {
        if (container instanceof FMLModContainer modContainer) {
            builder.addParameter(modContainer.getEventBus(), Set.of(IEventBus.class));
        }
    }
}
