package banduty.stoneycore.datagen;

import banduty.stoneycore.mobgear.data.MobGearArmorData;
import banduty.stoneycore.mobgear.data.MobGearAttachmentData;
import banduty.stoneycore.mobgear.data.MobGearWeaponData;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public abstract class MobGearDataProvider implements DataProvider {
    private final PackOutput.PathProvider weaponPath;
    private final PackOutput.PathProvider armorPath;
    private final PackOutput.PathProvider attachmentPath;

    private final Map<ResourceLocation, MobGearWeaponData> weapons = new LinkedHashMap<>();
    private final Map<ResourceLocation, MobGearArmorData> armor = new LinkedHashMap<>();
    private final Map<ResourceLocation, MobGearAttachmentData> attachments = new LinkedHashMap<>();

    protected MobGearDataProvider(PackOutput packOutput) {
        this.weaponPath = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "mob_gear/weapon");
        this.armorPath = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "mob_gear/armor");
        this.attachmentPath = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "mob_gear/attachment");
    }

    protected abstract void addEntries();

    protected final void weapon(Supplier<? extends Item> item, List<ResourceLocation> mobs) {
        weapon(item, mobs, false);
    }

    protected final void weapon(Supplier<? extends Item> item, List<ResourceLocation> mobs, boolean replace) {
        weapons.put(idOf(item), new MobGearWeaponData(mobs, replace));
    }

    @SafeVarargs
    protected final void weapons(List<ResourceLocation> mobs, Supplier<? extends Item>... items) {
        for (Supplier<? extends Item> item : items) weapon(item, mobs);
    }

    protected final void armor(EquipmentSlot slot, Supplier<? extends Item> item, List<ResourceLocation> mobs) {
        armor(slot, item, mobs, false);
    }

    protected final void armor(EquipmentSlot slot, Supplier<? extends Item> item,
                               List<ResourceLocation> mobs, boolean replace) {
        armor.put(idOf(item), new MobGearArmorData(slot, mobs, replace));
    }

    protected final void armorSet(Supplier<? extends Item> helmet, Supplier<? extends Item> chestplate,
                                  Supplier<? extends Item> leggings, Supplier<? extends Item> boots,
                                  List<ResourceLocation> mobs) {
        if (helmet != null) armor(EquipmentSlot.HEAD, helmet, mobs);
        if (chestplate != null) armor(EquipmentSlot.CHEST, chestplate, mobs);
        if (leggings != null) armor(EquipmentSlot.LEGS, leggings, mobs);
        if (boots != null) armor(EquipmentSlot.FEET, boots, mobs);
    }

    protected final void attachment(EquipmentSlot slot, Supplier<? extends Item> item, List<ResourceLocation> mobs) {
        attachment(slot, item, mobs, false);
    }

    protected final void attachment(EquipmentSlot slot, Supplier<? extends Item> item,
                                    List<ResourceLocation> mobs, boolean replace) {
        attachments.put(idOf(item), new MobGearAttachmentData(slot, mobs, replace));
    }

    @SafeVarargs
    protected final void attachments(EquipmentSlot slot, List<ResourceLocation> mobs,
                                     Supplier<? extends Item>... items) {
        for (Supplier<? extends Item> item : items) attachment(slot, item, mobs);
    }

    protected static ResourceLocation idOf(Supplier<? extends Item> item) {
        return BuiltInRegistries.ITEM.getKey(item.get());
    }

    protected static List<ResourceLocation> mobs(EntityType<?>... types) {
        List<ResourceLocation> ids = new ArrayList<>();
        for (EntityType<?> type : types) {
            ids.add(BuiltInRegistries.ENTITY_TYPE.getKey(type));
        }
        return List.copyOf(ids);
    }

    @Override
    public final CompletableFuture<?> run(CachedOutput cache) {
        weapons.clear();
        armor.clear();
        attachments.clear();
        addEntries();

        List<CompletableFuture<?>> futures = new ArrayList<>();
        weapons.forEach((id, data) -> futures.add(save(cache, weaponPath, id, MobGearWeaponData.CODEC, data)));
        armor.forEach((id, data) -> futures.add(save(cache, armorPath, id, MobGearArmorData.CODEC, data)));
        attachments.forEach((id, data) -> futures.add(save(cache, attachmentPath, id, MobGearAttachmentData.CODEC, data)));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private static <T> CompletableFuture<?> save(CachedOutput cache, PackOutput.PathProvider pathProvider,
                                                 ResourceLocation id, Codec<T> codec, T data) {
        JsonElement json = codec.encodeStart(JsonOps.INSTANCE, data).getOrThrow();
        return DataProvider.saveStable(cache, json, pathProvider.json(id));
    }

    @Override
    public String getName() {
        return "Mob Gear";
    }
}