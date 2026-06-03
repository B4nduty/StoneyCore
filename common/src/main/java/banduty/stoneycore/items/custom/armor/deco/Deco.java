package banduty.stoneycore.items.custom.armor.deco;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.custom.armor.ArmorAttachment;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public record Deco(Item item, List<Integer> colors, int group, List<ArmorItem.Type> allowedArmorTypes) {
    private static final Map<Item, Deco> REGISTRY = new ConcurrentHashMap<>();

    private static final Codec<ArmorItem.Type> ARMOR_TYPE_CODEC =
            StringRepresentable.fromValues(ArmorItem.Type::values);

    private static final StreamCodec<RegistryFriendlyByteBuf, ArmorItem.Type> ARMOR_TYPE_STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public ArmorItem.Type decode(RegistryFriendlyByteBuf buffer) {
                    return ArmorItem.Type.values()[buffer.readVarInt()];
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, ArmorItem.Type value) {
                    buffer.writeVarInt(value.ordinal());
                }
            };


    public static final Codec<Deco> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(Deco::item),
            Codec.INT.listOf().fieldOf("colors").forGetter(Deco::colors),
            Codec.INT.fieldOf("group").forGetter(Deco::group),
            ARMOR_TYPE_CODEC.listOf().fieldOf("allowedArmorTypes").forGetter(Deco::allowedArmorTypes)
    ).apply(instance, Deco::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Deco> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(BuiltInRegistries.ITEM.key()), Deco::item,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), Deco::colors,
            ByteBufCodecs.VAR_INT, Deco::group,
            ARMOR_TYPE_STREAM_CODEC.apply(ByteBufCodecs.list()), Deco::allowedArmorTypes,
            Deco::new
    );

    public static void register(Item item, int group, ArmorItem.Type... allowedArmorTypes) {
        if (REGISTRY.containsKey(item)) {
            StoneyCore.LOG.warn("Item {} is already registered as a Deco!", item);
            return;
        }

        if (allowedArmorTypes.length == 0) {
            StoneyCore.LOG.warn("Item {} needs at least one armor slot!", item);
            return;
        }
        REGISTRY.put(item, new Deco(item, new ArrayList<>(), group, List.of(allowedArmorTypes)));
    }

    public boolean canApplyTo(Item targetItem) {
        if (targetItem instanceof ArmorAttachment armor) {
            return this.allowedArmorTypes.contains(armor.getArmorSlot());
        }
        return false;
    }

    public static Optional<Deco> getFromItem(Item item) {
        return Optional.ofNullable(REGISTRY.get(item));
    }

    public String getNbtKey() {
        return BuiltInRegistries.ITEM.getKey(item).getPath();
    }

    public static Collection<Deco> all() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    public static List<Item> getItemsInGroup(int group) {
        return REGISTRY.values().stream()
                .filter(deco -> deco.group() == group)
                .map(Deco::item)
                .toList();
    }

    public static List<ItemStack> getDeco(ItemStack stack) {
        DecoContents contents = stack.get(SCDataComponents.DECO_CONTENTS.get());
        return contents != null ? contents.items() : List.of();
    }
}