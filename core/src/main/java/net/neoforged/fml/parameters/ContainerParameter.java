/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.fml.parameters;

import net.neoforged.fml.ModContainer;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class ContainerParameter implements IModParameter<ModContainer> {
    @Nullable
    @Override
    public ModContainer provide(ModContainer container) {
        return container;
    }

    @Override
    public Set<Class<? super ModContainer>> types(ModContainer container) {
        return (Set<Class<? super ModContainer>>) Set.of(ModContainer.class, container.getClass());
    }
}
