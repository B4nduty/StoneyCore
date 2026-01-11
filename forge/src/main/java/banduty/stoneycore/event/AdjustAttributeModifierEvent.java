package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.data.playerdata.SCAttributes;
import banduty.stoneycore.util.definitionsloader.AccessoriesDefinitionsStorage;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AdjustAttributeModifierEvent {

    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();

        if (!AccessoriesDefinitionsStorage.containsItem(stack)) return;

        updatePlayerAttributes(stack, event);
    }

    private static void updatePlayerAttributes(ItemStack stack, ItemAttributeModifierEvent event) {
        var data = AccessoriesDefinitionsStorage.getData(stack);
        var armor = data.armor();
        var toughness = data.toughness();

        if (NBTDataHelper.get(stack, INBTKeys.VISOR_OPEN, false)) {
            armor -= 1;
            toughness -= 1;
        }

        if (armor != 0) {
            event.addModifier(Attributes.ARMOR, new AttributeModifier(
                    UUID.nameUUIDFromBytes((StoneyCore.MOD_ID + ".armor").getBytes()),
                    StoneyCore.MOD_ID + ".armor",
                    armor,
                    AttributeModifier.Operation.ADDITION
            ));
        }

        if (toughness != 0) {
            event.addModifier(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(
                    UUID.nameUUIDFromBytes((StoneyCore.MOD_ID + ".armor_toughness").getBytes()),
                    StoneyCore.MOD_ID + ".armor_toughness",
                    toughness,
                    AttributeModifier.Operation.ADDITION
            ));
        }

        if (data.hungerDrainMultiplier() != 0) {
            event.addModifier(Services.ATTRIBUTES.getHungerDrainMultiplier(), new AttributeModifier(
                    UUID.nameUUIDFromBytes((StoneyCore.MOD_ID + ".hunger_drain_multiplier").getBytes()),
                    StoneyCore.MOD_ID + ".hunger_drain_multiplier",
                    data.hungerDrainMultiplier(),
                    AttributeModifier.Operation.ADDITION
            ));
        }
    }
}