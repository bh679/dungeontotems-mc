package games.brennan.dungeontotems.forge.platform;

import games.brennan.dungeontotems.platform.PlatformService;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;

public final class ForgePlatformService implements PlatformService {

    @Override
    public String loaderName() {
        return "forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isClient() {
        return FMLEnvironment.dist.isClient();
    }

    @Override
    public boolean isGameTestServer() {
        return Boolean.getBoolean("forge.gameTestServer");
    }
}
