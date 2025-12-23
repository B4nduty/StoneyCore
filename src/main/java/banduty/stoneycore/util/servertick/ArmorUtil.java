package banduty.stoneycore.util.servertick;

import banduty.stoneycore.items.armor.SCAccessoryItem;
import banduty.stoneycore.util.definitionsloader.AccessoriesDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsLoader;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ArmorUtil {
    public static void startArmorCheck(ServerPlayer player) {
        AccessoriesCapability.getOptionally(player).ifPresent(accessories -> {
            for (SlotEntryReference equipped : accessories.getAllEquipped()) {
                ItemStack stack = equipped.stack();
                if (!(stack.getItem() instanceof SCAccessoryItem)) continue;

                // Get the armor slot this accessory is related to
                String slotFromJson = AccessoriesDefinitionsLoader.getData(stack.getItem()).armorSlot();
                if (slotFromJson.isBlank()) continue;

                // Convert to EquipmentSlot
                EquipmentSlot targetSlot = EquipmentSlot.valueOf(slotFromJson);

                // Check if the related armor piece is equipped
                ItemStack armorPiece = player.getItemBySlot(targetSlot);
                if (!armorPiece.isEmpty() && ArmorDefinitionsLoader.containsItem(armorPiece.getItem())) {
                    // Armor is present → accessory can stay equipped
                    continue;
                }

                player.displayClientMessage(
                        Component.translatable(
                                "component.warning.stoneycore.armor_needed_for_accessory",
                                stack.getDisplayName(),
                                targetSlot
                        ).withStyle(ChatFormatting.DARK_RED),
                        true
                );

                // Armor is missing → remove the accessory
                player.addItem(stack.copy());
                stack.setCount(0);
            }
        });
    }
}