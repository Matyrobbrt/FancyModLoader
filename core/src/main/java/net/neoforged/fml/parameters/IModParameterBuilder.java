/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.fml.parameters;

import java.util.Collection;

/**
 * A builder for providing parameters to mods.
 */
public interface IModParameterBuilder {
    /**
     * Adds a new parameter to the mod.
     *
     * @param value the parameter that is provided to the container
     * @param types the types of the parameters that can be provided to the container
     * @param <T>   the most common type of the parameter
     * @throws IllegalArgumentException      if the {@code value} isn't an instance of every type provided
     * @throws UnsupportedOperationException if a parameter of a provided type has already been registered
     */
    <T> void addParameter(T value, Collection<Class<? extends T>> types);
}
