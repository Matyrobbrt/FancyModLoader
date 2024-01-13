/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.fml.loading.moddiscovery;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import cpw.mods.jarhandling.SecureJar;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.LogMarkers;
import net.neoforged.neoforgespi.coremod.ICoreModFile;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.language.IModLanguageProvider;
import net.neoforged.neoforgespi.language.ModFileScanData;
import net.neoforged.neoforgespi.locating.IModFile;
import net.neoforged.neoforgespi.locating.IModFileController;
import net.neoforged.neoforgespi.locating.IModProvider;
import net.neoforged.neoforgespi.locating.ModFileFactory;
import net.neoforged.neoforgespi.locating.ModFileLoadingException;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Stream;

public class ModFile implements IModFile {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final String jarVersion;
    private final ModFileFactory.ModFileInfoParser parser;
    private Map<String, Object> fileProperties;
    private List<IModLanguageProvider> loaders;
    private Throwable scanError;
    private final SecureJar jar;
    private final Type modFileType;
    private final Manifest     manifest;
    private final IModProvider provider;
    private       IModFileInfo modFileInfo;
    private ModFileScanData fileModFileScanData;
    private volatile CompletableFuture<ModFileScanData> futureScanResult;
    private List<ICoreModFile> coreMods = List.of();
    private List<String> mixinConfigs = List.of();
    private List<Path> accessTransformers = List.of();
    private final Controller controller = new Controller();

    static final Attributes.Name TYPE = new Attributes.Name("FMLModType");
    private SecureJar.Status securityStatus;

    public ModFile(final SecureJar jar, final IModProvider provider, final ModFileFactory.ModFileInfoParser parser) {
        this(jar, provider, parser, parseType(jar));
    }

    public ModFile(final SecureJar jar, final IModProvider provider, final ModFileFactory.ModFileInfoParser parser, String type) {
        this.provider = provider;
        this.jar = jar;
        this.parser = parser;

        manifest = this.jar.moduleDataProvider().getManifest();
        modFileType = Type.valueOf(type);
        jarVersion = Optional.ofNullable(manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION)).orElse("0.0NONE");
        this.modFileInfo = ModFileParser.readModList(this, this.parser);
    }

    @Override
    public Supplier<Map<String,Object>> getSubstitutionMap() {
        return () -> ImmutableMap.<String,Object>builder().put("jarVersion", jarVersion).putAll(fileProperties).build();
    }
    @Override
    public Type getType() {
        return modFileType;
    }

    @Override
    public Path getFilePath() {
        return jar.getPrimaryPath();
    }

    @Override
    public SecureJar getSecureJar() {
        return this.jar;
    }

    @Override
    public List<IModInfo> getModInfos() {
        return modFileInfo.getMods();
    }

    public List<Path> getAccessTransformers() {
        return accessTransformers;
    }

    public List<ICoreModFile> getCoreMods() {
        return coreMods;
    }

    public List<String> getMixinConfigs() {
        return mixinConfigs;
    }

    /**
     * Run in an executor thread to harvest the class and annotation list
     */
    public ModFileScanData compileContent() {
        return new Scanner(this).scan();
    }

    public void scanFile(Consumer<Path> pathConsumer) {
        provider.scanFile(this, pathConsumer);
    }

    @Override
    public ModFileScanData getScanResult() {
        if (this.futureScanResult != null) {
            try {
                this.futureScanResult.get();
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Caught unexpected exception processing scan results", e);
            }
        }
        if (this.scanError != null) {
            throw new RuntimeException(this.scanError);
        }
        return this.fileModFileScanData;
    }

    public void setScanResult(final ModFileScanData modFileScanData, final Throwable throwable) {
        this.fileModFileScanData = modFileScanData;
        if (throwable != null) {
            this.scanError = throwable;
        }
        this.futureScanResult = null;
    }

    public void setFileProperties(Map<String, Object> fileProperties) {
        this.fileProperties = fileProperties;
    }

    @Override
    public List<IModLanguageProvider> getLoaders() {
        return loaders;
    }

    @Override
    public Path findResource(String... path) {
        if (path.length < 1) {
            throw new IllegalArgumentException("Missing path");
        }
        return getSecureJar().getPath(String.join("/", path));
    }

    @Override
    public String toString() {
        return "Mod File: " + Objects.toString(this.jar.getPrimaryPath());
    }

    @Override
    public String getFileName() {
        return getFilePath().getFileName().toString();
    }

    @Override
    public IModProvider getProvider() {
        return provider;
    }

    @Override
    public IModFileInfo getModFileInfo() {
        return modFileInfo;
    }

    @Override
    public void setSecurityStatus(final SecureJar.Status status) {
        this.securityStatus = status;
    }

    @Override
    public ArtifactVersion getVersion()
    {
        return new DefaultArtifactVersion(this.jarVersion);
    }

    private static String parseType(final SecureJar jar) {
        final Manifest m = jar.moduleDataProvider().getManifest();
        final Optional<String> value = Optional.ofNullable(m.getMainAttributes().getValue(TYPE));
        return value.orElse("MOD");
    }

    @Override
    public IModFileController getController() {
        return controller;
    }

    private class Controller implements IModFileController {
        @Override
        public void identify() {
            if (getType() == Type.MOD) {
                ModFile.this.modFileInfo = ModFileParser.readModList(ModFile.this, ModFile.this.parser);
                if (ModFile.this.modFileInfo == null) {
                    throw new ModFileLoadingException("Mod %s is missing metadata".formatted(getFilePath()));
                }
                LOGGER.debug(LogMarkers.LOADING, "Loading mod file {} with languages {}", ModFile.this.getFilePath(), ModFile.this.modFileInfo.requiredLanguageLoaders());
                ModFile.this.coreMods = ModFileParser.getCoreMods(ModFile.this);
                ModFile.this.coreMods.forEach(mi -> LOGGER.debug(LogMarkers.LOADING, "Found coremod {}", mi.getPath()));
                ModFile.this.mixinConfigs = ModFileParser.getMixinConfigs(ModFile.this.modFileInfo);
                ModFile.this.mixinConfigs.forEach(mc -> LOGGER.debug(LogMarkers.LOADING, "Found mixin config {}", mc));
                ModFile.this.accessTransformers = ModFileParser.getAccessTransformers(ModFile.this.modFileInfo)
                        .map(list -> list.stream().map(ModFile.this::findResource).filter(path -> {
                            if (Files.notExists(path)) {
                                LOGGER.error(LogMarkers.LOADING, "Access transformer file {} provided by mod {} does not exist!", path, modFileInfo.moduleName());
                                return false;
                            }
                            return true;
                        }))
                        .orElseGet(() -> Stream.of(findResource("META-INF", "accesstransformer.cfg"))
                                .filter(Files::exists))
                        .toList();
            }
        }

        @Override
        public void setLoaders(List<IModLanguageProvider> loaders) {
            ModFile.this.loaders = loaders;
        }

        @Override
        public CompletableFuture<?> submitForScanning(ExecutorService service) {
            return ModFile.this.futureScanResult = CompletableFuture.supplyAsync(ModFile.this::compileContent, service)
                    .whenComplete(ModFile.this::setScanResult);
        }
    }
}
