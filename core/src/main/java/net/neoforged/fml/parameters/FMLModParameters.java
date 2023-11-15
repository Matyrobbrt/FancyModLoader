/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.fml.parameters;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLLoader;

import java.util.Set;

public class FMLModParameters implements IModParameterProvider {
    @Override
    public void provide(ModContainer container, IModParameterBuilder builder) {
        builder.addParameter(FMLLoader.getDist(), Set.of(Dist.class));
        builder.addParameter(container, Set.of(ModContainer.class, container.getClass()));
    }
}
