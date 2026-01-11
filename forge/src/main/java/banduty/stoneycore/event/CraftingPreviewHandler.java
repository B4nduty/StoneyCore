package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.SCItems;
import banduty.stoneycore.items.SmithingHammer;
import banduty.stoneycore.items.manuscript.Manuscript;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.patterns.PatternHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CraftingPreviewHandler {

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack crafted = event.getCrafting();
        Container container = event.getInventory();

        CraftingContainer craftingInventory = getCraftingInventory(container);
        if (craftingInventory == null) return;

        ItemStack manuscript = handleManuscriptCrafting(player, craftingInventory, crafted);
        if (!manuscript.isEmpty()) {
            ObfuscationReflectionHelper.setPrivateValue(PlayerEvent.ItemCraftedEvent.class, event, manuscript, "crafting");
            return;
        }

        applyBannerPatterns(crafted, craftingInventory);
    }

    private static ItemStack handleManuscriptCrafting(ServerPlayer player, CraftingContainer inventory, ItemStack original) {
        ItemStack paper = ItemStack.EMPTY;
        ItemStack manuscript = ItemStack.EMPTY;
        ItemStack hammer = ItemStack.EMPTY;
        ItemStack itemInput = ItemStack.EMPTY;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.is(Items.PAPER)) {
                paper = stack;
            } else if (stack.is(SCItems.MANUSCRIPT.get())) {
                manuscript = stack;
            } else if (stack.getItem() instanceof SmithingHammer) {
                hammer = stack;
            } else {
                itemInput = stack;
            }
        }

        // Check if existing manuscript already has a target
        if (manuscript.getItem() instanceof Manuscript && !Manuscript.hasTargetStack(manuscript)) {
            return ItemStack.EMPTY;
        }

        // Create new manuscript if we have paper + item + hammer
        if (!itemInput.isEmpty() && !paper.isEmpty() && !hammer.isEmpty() && manuscript.isEmpty()) {
            return Manuscript.createForStack(itemInput);
        }

        return original;
    }

    private static CraftingContainer getCraftingInventory(Container container) {
        if (container instanceof CraftingContainer) {
            return (CraftingContainer) container;
        } else if (container instanceof CraftingMenu craftingMenu) {
            return ObfuscationReflectionHelper.getPrivateValue(CraftingMenu.class, craftingMenu, "craftSlots");
        } else if (container instanceof InventoryMenu inventoryMenu) {
            return inventoryMenu.getCraftSlots();
        }
        return null;
    }

    private static void applyBannerPatterns(ItemStack crafted, CraftingContainer inventory) {
        if (!crafted.is(SCTags.BANNER_COMPATIBLE.getTag())) return;

        // Find banner in the crafting grid
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() instanceof BannerItem) {
                List<Tuple<ResourceLocation, DyeColor>> patterns = getBannerPatterns(stack, crafted.getItem());
                PatternHelper.setBannerPatterns(crafted, patterns);
                break; // Only use first banner found
            }
        }
    }

    public static List<Tuple<ResourceLocation, DyeColor>> getBannerPatterns(ItemStack banner, Item armor) {
        CompoundTag nbt = banner.getTag();
        if (banner.isEmpty() || !(banner.getItem() instanceof BannerItem) || nbt == null) return List.of();

        CompoundTag blockEntityTag = nbt.getCompound(INBTKeys.BLOCK_ENTITY_TAG);
        if (!blockEntityTag.contains(INBTKeys.PATTERNS)) return List.of();

        ResourceLocation armorId = BuiltInRegistries.ITEM.getKey(armor);
        ListTag patternList = blockEntityTag.getList(INBTKeys.PATTERNS, CompoundTag.TAG_COMPOUND);

        List<Tuple<ResourceLocation, DyeColor>> patterns = new ArrayList<>();
        for (int i = 0; i < patternList.size(); i++) {
            CompoundTag patternTag = patternList.getCompound(i);
            String pattern = NBTDataHelper.get(patternTag, INBTKeys.PATTERN, "");
            int colorId = NBTDataHelper.get(patternTag, INBTKeys.COLOR, 0);

            ResourceLocation patternId = new ResourceLocation(
                    armorId.getNamespace(),
                    "textures/banner_pattern/" + armorId.getPath() + "/" + pattern + ".png"
            );
            patterns.add(new Tuple<>(patternId, DyeColor.byId(colorId)));
        }
        return patterns;
    }
}