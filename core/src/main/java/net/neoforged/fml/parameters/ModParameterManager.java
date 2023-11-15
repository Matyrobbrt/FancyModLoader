/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.fml.parameters;

import net.neoforged.fml.ModContainer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

public class ModParameterManager {
    private final List<IModParameterProvider> parameters;

    public ModParameterManager(ModuleLayer layer) {
        this.parameters = ServiceLoader.load(layer, IModParameterProvider.class).stream()
                .map(ServiceLoader.Provider::get)
                .toList();
    }

    public <T> T create(Map<Class<?>, Object> allowedConstructorArgs, Constructor<T> constructor) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        var parameterTypes = constructor.getParameterTypes();
        Object[] constructorArgs = new Object[parameterTypes.length];
        Set<Class<?>> foundArgs = new HashSet<>();

        for (int i = 0; i < parameterTypes.length; i++) {
            Object argInstance = allowedConstructorArgs.get(parameterTypes[i]);
            if (argInstance == null) {
                throw new RuntimeException("Mod constructor has unsupported argument " + parameterTypes[i] + ". Allowed optional argument classes: " +
                        allowedConstructorArgs.keySet().stream().map(Class::getSimpleName).collect(Collectors.joining(", ")));
            }

            if (foundArgs.contains(parameterTypes[i])) {
                throw new RuntimeException("Duplicate mod constructor argument type: " + parameterTypes[i]);
            }

            foundArgs.add(parameterTypes[i]);
            constructorArgs[i] = argInstance;
        }

        // All arguments are found
        return constructor.newInstance(constructorArgs);
    }

    public Map<Class<?>, Object> provideParameters(ModContainer container) {
        final Map<Class<?>, Object> parameters = new IdentityHashMap<>();
        final Map<Class<?>, IModParameterProvider> sources = new IdentityHashMap<>();
        this.parameters.forEach(param -> {
            final var builder = new IModParameterBuilder() {

                @Override
                public <T> void addParameter(T provided, Collection<Class<? extends T>> types) {
                    for (final var type : types) {
                        if (!type.isInstance(provided)) {
                            throw new IllegalArgumentException("%s attempted to provide parameter of type %s, that is not an instance of expected %s".formatted(param, provided.getClass(), type));
                        }

                        if (parameters.containsKey(type)) {
                            throw new UnsupportedOperationException("Two parameters of the same type %s were attempted to be provided by %s and %s".formatted(type, sources.get(type), param.getClass()));
                        }
                        sources.put(type, param);
                        parameters.put(type, provided);
                    }
                }
            };
            param.provide(container, builder);
        });
        return Map.copyOf(parameters);
    }
}
