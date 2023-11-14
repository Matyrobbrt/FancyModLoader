/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.fml.parameters;

import net.neoforged.fml.ModContainer;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * A provider for mod parameters. A mod parameter is an object injected into mod constructors, that can also be retrieved via {@link ModContainer#getParameter(Class)}.
 * <p>
 * This system provides basic dependency injection for objects that any mod should have access to trivially, such as the container or its mod-specific event bus.
 * <p>
 * Library mods can use this for exposing container-aware helpers.
 *
 * @param <T> the most common type of the parameter
 */
public interface IModParameter<T> {
    /**
     * Provides a parameter to the given {@code container}. <br>
     * If a parameter can't be provided, then {@code null} should be returned.
     *
     * @param container the container to which to provide parameters
     * @return the parameter to provide
     */
    @Nullable
    T provide(ModContainer container);

    /**
     * {@return the types of the parameters that can be provided to the {@code container}}
     * Should be {@link Set#of() empty} if no parameters can be provided to the container.
     */
    Set<Class<? super T>> types(ModContainer container);
}

