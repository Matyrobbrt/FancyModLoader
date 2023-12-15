package net.neoforged.fml.loading.targets;

import net.neoforged.api.distmarker.Dist;

public class JUnitUserDevLaunchTarget extends ForgeUserdevLaunchHandler {
    @Override
    public Dist getDist() {
        return Dist.CLIENT;
    }

    @Override
    protected void runService(String[] arguments, ModuleLayer gameLayer) throws Throwable {
        Class.forName(gameLayer.findModule("neoforge").orElseThrow(), "net.neoforged.neoforge.junit.JUnitMain").getMethod("main", String[].class).invoke(null, (Object)arguments);
    }

    @Override
    public String name() {
        return "junitfmluserdev";
    }
}
