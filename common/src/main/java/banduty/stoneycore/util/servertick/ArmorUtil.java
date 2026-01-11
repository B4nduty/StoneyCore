package banduty.stoneycore.util.servertick;

import banduty.stoneycore.items.armor.SCAccessoryItem;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.util.definitionsloader.AccessoriesDefinitionsStorage;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ArmorUtil {
    public static void startArmorCheck(ServerPlayer player) {
        for (ItemStack equippedStack : Services.PLATFORM.getEquippedAccessories(player)) {
            if (!(equippedStack.getItem() instanceof SCAccessoryItem)) continue;

            // Get the armor slot this accessory is related to
            String slotFromJson = AccessoriesDefinitionsStorage.getData(equippedStack.getItem()).armorSlot();
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
                            equippedStack.getDisplayName(),
                            targetSlot
                    ).withStyle(ChatFormatting.DARK_RED),
                    true
            );

            // Armor is missing → remove the accessory
            player.addItem(equippedStack.copy());
            equippedStack.setCount(0);
        }
    }
}