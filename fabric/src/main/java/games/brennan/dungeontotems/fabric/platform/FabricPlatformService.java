package games.brennan.dungeontotems.fabric.platform;

import games.brennan.dungeontotems.platform.PlatformService;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public final class FabricPlatformService implements PlatformService {

    @Override
    public String loaderName() {
        return "fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public boolean isGameTestServer() {
        return System.getProperty("fabric-api.gametest") != null;
    }
}
