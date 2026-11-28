package games.brennan.dungeontotems.fabric;

import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.morph.MorphFormTable;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/** {@link MorphFormTable} as Fabric wants a data reload listener: with an id. */
final class MorphFormReloader implements IdentifiableResourceReloadListener {

    @Override
    public ResourceLocation getFabricId() {
        return DungeonTotems.id("morph_forms");
    }

    @Override
    public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier barrier, ResourceManager manager,
                                          ProfilerFiller prepareProfiler, ProfilerFiller applyProfiler,
                                          Executor background, Executor game) {
        return MorphFormTable.INSTANCE.reload(barrier, manager, prepareProfiler, applyProfiler, background, game);
    }
}
