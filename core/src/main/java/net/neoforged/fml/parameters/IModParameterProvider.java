/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.fml.parameters;

import net.neoforged.fml.ModContainer;

import java.util.Collection;

/**
 * A provider for mod parameters. A mod parameter is an object injected into mod constructors, that can also be retrieved via {@link ModContainer#getParameter(Class)}.
 * <p>
 * This system provides basic dependency injection for objects that any mod should have access to trivially, such as the container or its mod-specific event bus.
 * <p>
 * Library mods can use this for exposing container-aware helpers.
 */
public interface IModParameterProvider {
    /**
     * Provides parameters to the given {@code container}. <br>
     * If a parameter can't be provided, then no parameter should be {@link IModParameterBuilder#addParameter(Object, Collection) added} to the builder.
     *
     * @param container the container to which to provide parameters
     * @param builder   the builder through which parameters are provided
     */
    void provide(ModContainer container, IModParameterBuilder builder);
}

