package banduty.stoneycore.util.definitionsloader;

import banduty.stoneycore.StoneyCore;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class WeaponDefinitionsLoader implements IdentifiableResourceReloadListener {
    private static final ResourceLocation RELOAD_LISTENER_ID =
            new ResourceLocation(StoneyCore.MOD_ID, "weapon_definitions_loader");

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
        return CompletableFuture.runAsync(() -> {
            WeaponDefinitionsStorage.clearDefinitions();

            Map<ResourceLocation, Resource> resources = resourceManager.listResources("definitions/weapon",
                    id -> id.getPath().endsWith(".json"));

            resources.forEach((id, resource) -> {
                try (InputStream stream = resource.open()) {
                    JsonElement element = JsonParser.parseReader(new InputStreamReader(stream));

                    DataResult<WeaponDefinitionData.WeaponDefinition> result =
                            WeaponDefinitionData.WeaponDefinition.CODEC.parse(JsonOps.INSTANCE, element);

                    result.resultOrPartial(StoneyCore.LOG::error)
                            .ifPresent(weaponDef -> {
                                ResourceLocation weaponId = ResourceLocation.tryBuild(
                                        id.getNamespace(),
                                        id.getPath().substring("definitions/weapon/".length(),
                                                id.getPath().length() - 5)
                                );

                                EnumSet<WeaponDefinitionData.Usage> usage = EnumSet.noneOf(WeaponDefinitionData.Usage.class);
                                WeaponDefinitionData.MeleeData meleeData = null;
                                WeaponDefinitionData.RangedData rangedData = null;
                                WeaponDefinitionData.AmmoData ammoData = null;

                                if (weaponDef.melee() != null) {
                                    usage.add(WeaponDefinitionData.Usage.MELEE);
                                    meleeData = weaponDef.melee();
                                }
                                if (weaponDef.ranged() != null) {
                                    usage.add(WeaponDefinitionData.Usage.RANGED);
                                    rangedData = weaponDef.ranged();
                                }
                                if (weaponDef.ammo() != null) {
                                    usage.add(WeaponDefinitionData.Usage.AMMO);
                                    ammoData = weaponDef.ammo();
                                }

                                WeaponDefinitionData data = new WeaponDefinitionData(usage, meleeData, rangedData, ammoData);
                                WeaponDefinitionsStorage.addDefinition(weaponId, data);
                            });

                } catch (Exception e) {
                    StoneyCore.LOG.error("Failed to load weapon definition from {}: {}", id, e.getMessage(), e);
                }
            });
        }, prepareExecutor).thenCompose(synchronizer::wait).thenRunAsync(() -> {
            StoneyCore.LOG.info("Loaded {} weapon definitions", WeaponDefinitionsStorage.DEFINITIONS.size());
        }, applyExecutor);
    }
}