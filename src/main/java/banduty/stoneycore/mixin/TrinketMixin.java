package banduty.stoneycore.mixin;

import banduty.stoneycore.event.custom.TrinketsModifiersEvents;
import banduty.stoneycore.items.armor.SCTrinketsItem;
import banduty.stoneycore.util.definitionsloader.SCUnderArmorDefinitionsLoader;
import banduty.stoneycore.util.itemdata.SCTags;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketItem;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Mixin(TrinketItem.class)
public class TrinketMixin implements Trinket {
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        var modifiers = this.getTrinketsModifiers(stack, slot);

        modifiers = TrinketsModifiersEvents.EVENT.invoker().getModifiers(modifiers, stack, slot, entity, uuid);

        return modifiers;
    }

    @Unique
    Multimap<EntityAttribute, EntityAttributeModifier> getTrinketsModifiers(ItemStack stack, SlotReference slot) {
        Multimap<EntityAttribute, EntityAttributeModifier> map = Multimaps.newMultimap(Maps.newLinkedHashMap(), ArrayList::new);
        if (stack.hasNbt() && stack.getNbt().contains("TrinketAttributeModifiers", 9)) {
            NbtList list = stack.getNbt().getList("TrinketAttributeModifiers", 10);

            for(int i = 0; i < list.size(); ++i) {
                NbtCompound tag = list.getCompound(i);
                if (tag.contains("Slot", 8)) {
                    String var10000 = tag.getString("Slot");
                    String var10001 = slot.inventory().getSlotType().getGroup();
                    if (!var10000.equals(var10001 + "/" + slot.inventory().getSlotType().getName())) {
                        continue;
                    }
                }

                Optional<EntityAttribute> optional = Registries.ATTRIBUTE.getOrEmpty(Identifier.tryParse(tag.getString("AttributeName")));
                if (optional.isPresent()) {
                    EntityAttributeModifier entityAttributeModifier = EntityAttributeModifier.fromNbt(tag);
                    if (entityAttributeModifier != null && entityAttributeModifier.getId().getLeastSignificantBits() != 0L && entityAttributeModifier.getId().getMostSignificantBits() != 0L) {
                        map.put(optional.get(), entityAttributeModifier);
                    }
                }
            }
        }

        return map;
    }

    @Override
    public boolean canEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (!(stack.getItem() instanceof SCTrinketsItem)) return true;

        return stack.isIn(SCTags.ALWAYS_WEARABLE.getTag()) || Arrays.stream(EquipmentSlot.values())
                .filter(this::isArmorSlot)
                .allMatch(slotType -> entity.getEquippedStack(slotType).getItem() instanceof ArmorItem armorItem
                                && SCUnderArmorDefinitionsLoader.containsItem(armorItem));
    }

    @Unique
    private boolean isArmorSlot(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD, CHEST, LEGS, FEET -> true;
            default -> false;
        };
    }
}
