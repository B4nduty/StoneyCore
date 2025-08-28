package banduty.stoneycore.mixin;

import banduty.stoneycore.items.armor.SCAccessoryItem;
import banduty.stoneycore.util.itemdata.SCTags;
import banduty.stoneycore.util.patterns.PatternHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.BannerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(CraftingScreenHandler.class)
public abstract class CraftingScreenHandlerMixin {
    @Inject(
            method = "updateResult",
            at = @At(value = "TAIL")
    )
    private static void onUpdateResult(ScreenHandler handler, World world, PlayerEntity player, RecipeInputInventory craftingInventory, CraftingResultInventory resultInventory, CallbackInfo ci) {
        if (world.isClient) return;
        if (!(player instanceof ServerPlayerEntity serverPlayerEntity)) return;
        ItemStack craftingRecipeItem = null;
        for (int i = 0; i < craftingInventory.size(); i++) {
            craftingRecipeItem = craftingInventory.getStack(i);
            if (!craftingRecipeItem.isEmpty() && craftingRecipeItem.isOf(resultInventory.getStack(0).getItem())) break;
        }

        if (craftingRecipeItem == null || craftingRecipeItem.isEmpty()) return;

        boolean shouldReturn = true;
        for (int i = 0; i < craftingInventory.size(); i++) {
            ItemStack ingredient = craftingInventory.getStack(i);
            if (ingredient.getItem() instanceof BannerItem) {
                shouldReturn = false;
                break;
            }
        }
        if (shouldReturn) return;

        ItemStack modified = craftingRecipeItem.copy();
        applyPreviewModifiers(modified, craftingInventory);
        resultInventory.setStack(0, modified);
        handler.setPreviousTrackedSlot(0, modified);
        serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), 0, modified));
    }

    @Unique
    private static void applyPreviewModifiers(ItemStack stack, RecipeInputInventory craftingInventory) {
        ItemStack bannerStack = ItemStack.EMPTY;
        for (int i = 0; i < craftingInventory.size(); i++) {
            ItemStack ingredient = craftingInventory.getStack(i);
            if (ingredient.getItem() instanceof BannerItem) {
                bannerStack = ingredient;
                break;
            }
        }

        if (!bannerStack.isEmpty() && stack.getItem() instanceof SCAccessoryItem && stack.isIn(SCTags.BANNER_COMPATIBLE.getTag())) {
            List<Pair<Identifier, DyeColor>> bannerPatterns = getBannerPatterns(bannerStack, stack.getItem());
            PatternHelper.setBannerPatterns(stack, bannerPatterns);
            PatternHelper.setBannerDyeColor(stack, ((BannerItem) bannerStack.getItem()).getColor());
        }
    }

    @Unique
    private static List<Pair<Identifier, DyeColor>> getBannerPatterns(ItemStack bannerStack, Item armor) {
        List<Pair<Identifier, DyeColor>> patterns = new ArrayList<>();

        if (!bannerStack.isEmpty() && bannerStack.getItem() instanceof BannerItem) {
            NbtCompound nbt = bannerStack.getNbt();
            if (nbt != null && nbt.contains("BlockEntityTag")) {
                NbtCompound blockEntityTag = nbt.getCompound("BlockEntityTag");
                if (blockEntityTag.contains("Patterns")) {
                    NbtList patternList = blockEntityTag.getList("Patterns", NbtCompound.COMPOUND_TYPE);
                    for (int i = 0; i < patternList.size(); i++) {
                        NbtCompound patternTag = patternList.getCompound(i);
                        String pattern = patternTag.getString("Pattern");
                        int colorId = patternTag.getInt("Color");
                        DyeColor color = DyeColor.byId(colorId);

                        Identifier itemId = Registries.ITEM.getId(armor);
                        Identifier patternId = new Identifier(
                                itemId.getNamespace(),
                                "textures/banner_pattern/" + itemId.getPath() + "/" + pattern + ".png"
                        );

                        patterns.add(new Pair<>(patternId, color));
                    }
                }
            }
        }
        return patterns;
    }
}