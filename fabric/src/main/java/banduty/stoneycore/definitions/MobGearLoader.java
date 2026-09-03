package banduty.stoneycore.definitions;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.mobgear.MobGearDataReader;
import banduty.stoneycore.mobgear.SCMobGearRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class MobGearLoader implements IdentifiableResourceReloadListener {
    private static final ResourceLocation RELOAD_LISTENER_ID =
            ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "mob_gear_loader");

    @Override
    public ResourceLocation getFabricId() {
        return RELOAD_LISTENER_ID;
    }

    @Override
    public @NotNull CompletableFuture<Void> reload(PreparationBarrier synchronizer,
                                                   @NotNull ResourceManager resourceManager,
                                                   @NotNull ProfilerFiller prepareProfiler,
                                                   @NotNull ProfilerFiller applyProfiler,
                                                   @NotNull Executor prepareExecutor,
                                                   @NotNull Executor applyExecutor) {
        return CompletableFuture.supplyAsync(() -> MobGearDataReader.readAll(resourceManager), prepareExecutor)
                .thenCompose(synchronizer::wait)
                .thenAcceptAsync(data -> {
                    SCMobGearRegistry.applyDatapackData(data.weapons(), data.armor(), data.attachments());
                    StoneyCore.LOG.debug("Loaded mob gear: {} weapons, {} armor pieces, {} attachments",
                            data.weapons().size(), data.armor().size(), data.attachments().size());
                }, applyExecutor);
    }
}