package banduty.stoneycore.util.servertick;

import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import banduty.stoneycore.util.definitionsloader.AccessoriesDefinitionsStorage;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ArmorUtil {
    public static void startArmorCheck(ServerPlayer player) {
        ItemStack itemStack = player.getItemBySlot(EquipmentSlot.HEAD);
        for (ItemStack accessoryStack : SCUnderArmor.getAccessories(itemStack)) {
            // Get the armor slot this accessory is related to
            String slotFromJson = AccessoriesDefinitionsStorage.getData(accessoryStack.getItem()).armorSlot();
            if (slotFromJson.isBlank()) continue;

            // Convert to EquipmentSlot
            EquipmentSlot targetSlot = EquipmentSlot.valueOf(slotFromJson);

            // Check if the related armor piece is equipped
            ItemStack armorPiece = player.getItemBySlot(targetSlot);
            if (!armorPiece.isEmpty() && ArmorDefinitionsStorage.containsItem(armorPiece.getItem())) {
                // Armor is present → accessory can stay equipped
                continue;
            }

            player.displayClientMessage(
                    Component.translatable(
                            "component.warning.stoneycore.armor_needed_for_accessory",
                            accessoryStack.getDisplayName(),
                            targetSlot
                    ).withStyle(ChatFormatting.DARK_RED),
                    true
            );

            // Armor is missing → remove the accessory
            ItemStack copy = accessoryStack.copy();

            // Try to add to inventory
            boolean added = player.addItem(copy);

            // If inventory is full, drop it
            if (!added) {
                player.drop(copy, false);
            }

            accessoryStack.setCount(0);
        }
    }
}