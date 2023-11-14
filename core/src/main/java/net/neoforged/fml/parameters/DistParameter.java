/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.fml.parameters;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLLoader;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class DistParameter implements IModParameter<Dist> {
    @Nullable
    @Override
    public Dist provide(ModContainer container) {
        return FMLLoader.getDist();
    }

    private static final Set<Class<? super Dist>> TYPES = Set.of(Dist.class);
    @Override
    public Set<Class<? super Dist>> types(ModContainer container) {
        return TYPES;
    }
}
