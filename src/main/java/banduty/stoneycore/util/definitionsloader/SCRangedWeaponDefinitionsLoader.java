package banduty.stoneycore.util.definitionsloader;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.SCDamageCalculator;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.UseAction;
import net.minecraft.util.profiler.Profiler;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class SCRangedWeaponDefinitionsLoader implements IdentifiableResourceReloadListener {

    private static final Map<Identifier, DefinitionData> DEFINITIONS = new ConcurrentHashMap<>();

    private static final Identifier RELOAD_LISTENER_ID = new Identifier(StoneyCore.MOD_ID, "ranged_weapon_definitions_loader");

    @Override
    public Identifier getFabricId() {
        return RELOAD_LISTENER_ID;
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager resourceManager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.runAsync(() -> {
            DEFINITIONS.clear();

            Map<Identifier, Resource> resources = resourceManager.findResources("definitions/ranged_weapon", id -> id.getPath().endsWith(".json"));

            resources.forEach((id, resource) -> {
                try (InputStream stream = resource.getInputStream()) {
                    JsonObject json = new Gson().fromJson(new InputStreamReader(stream), JsonObject.class);

                    float baseDamage = 0f;
                    if (json.has("baseDamage")) {
                        baseDamage = json.get("baseDamage").getAsFloat();
                    }

                    float speed = 0f;
                    if (json.has("speed")) {
                        speed = json.get("speed").getAsFloat();
                    }

                    int maxUseTime = 0;
                    if (json.has("maxUseTime")) {
                        maxUseTime = json.get("maxUseTime").getAsInt();
                    }

                    int rechargeTime = 0;
                    if (json.has("rechargeTime")) {
                        rechargeTime = json.get("rechargeTime").getAsInt();
                    }

                    boolean needsFlintAndSteel = false;
                    if (json.has("needsFlintAndSteel")) {
                        needsFlintAndSteel = json.get("needsFlintAndSteel").getAsBoolean();
                    }

                    UseAction useAction = null;
                    if (json.has("useAction")) {
                        String value = json.get("useAction").getAsString();
                        useAction = UseAction.valueOf(value.toUpperCase());
                    }

                    SCDamageCalculator.DamageType damageType = null;
                    if (json.has("damageType")) {
                        String value = json.get("damageType").getAsString();
                        damageType = SCDamageCalculator.DamageType.valueOf(value.toUpperCase());
                    }

                    Map<String, AmmoRequirementData> ammoRequirement = new HashMap<>();
                    if (json.has("ammoRequirement")) {
                        JsonObject attributesJson = json.getAsJsonObject("ammoRequirement");
                        for (Map.Entry<String, JsonElement> entry : attributesJson.entrySet()) {
                            String key = entry.getKey();

                            JsonArray itemArray = entry.getValue().getAsJsonArray();
                            Set<String> items = new HashSet<>();
                            int amount = 0;

                            for (int i = 0; i < itemArray.size(); i++) {
                                JsonElement element = itemArray.get(i);
                                if (i < itemArray.size() - 1) {
                                    items.add(element.getAsString());
                                } else {
                                    amount = element.getAsInt();
                                }
                            }

                            ammoRequirement.put(key, new AmmoRequirementData(items, amount));
                        }
                    }

                    SoundEvent[] soundEvents = new SoundEvent[0];
                    if (json.has("soundEvents")) {
                        JsonArray soundEventsArray = json.getAsJsonArray("soundEvents");
                        soundEvents = new SoundEvent[soundEventsArray.size()];
                        for (int i = 0; i < soundEventsArray.size(); i++) {
                            String soundEventId = soundEventsArray.get(i).getAsString();
                            soundEvents[i] = Registries.SOUND_EVENT.get(new Identifier(soundEventId));
                        }
                    }

                    Identifier attributeId = Identifier.of(id.getNamespace(), id.getPath().substring("definitions/ranged_weapon/".length(), id.getPath().length() - 5));
                    DEFINITIONS.put(attributeId, new DefinitionData(baseDamage, damageType, maxUseTime, speed, rechargeTime,
                            needsFlintAndSteel, useAction, ammoRequirement, soundEvents));
                } catch (Exception e) {
                    StoneyCore.LOGGER.error("Failed to load definitions data from {}: {}", id, e.getMessage(), e);
                }
            });
        }, prepareExecutor).thenCompose(synchronizer::whenPrepared).thenRunAsync(() -> {
        }, applyExecutor);
    }

    public static DefinitionData getData(Identifier id) {
        return DEFINITIONS.getOrDefault(id, new DefinitionData(0f, SCDamageCalculator.DamageType.BLUDGEONING,
                0, 0, 0, false, UseAction.NONE, new HashMap<>(), null));
    }

    public record DefinitionData(float baseDamage, SCDamageCalculator.DamageType damageType, int maxUseTime, float speed,
                                 int rechargeTime, boolean needsFlintAndSteel, UseAction useAction,
                                 Map<String, AmmoRequirementData> ammoRequirement, SoundEvent[] soundEvents) {}

    public record AmmoRequirementData(Set<String> itemIds, int amount) {}
}