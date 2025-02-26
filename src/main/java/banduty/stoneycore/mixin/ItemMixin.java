package banduty.stoneycore.mixin;

import banduty.stoneycore.items.armor.SCTrinketsItem;
import banduty.stoneycore.items.item.SCRangeWeapon;
import banduty.stoneycore.items.item.SCWeapon;
import banduty.stoneycore.util.itemdata.SCTags;
import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import banduty.stoneycore.util.weaponutil.SCWeaponUtil;
import banduty.stoneycore.util.weaponutil.TooltipClientSide;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(Item.class)
public class ItemMixin {
    @Unique
    private static final String NBT_BLUDGEONING_KEY = "sc_bludgeoning";

    @Inject(method = "getUseAction", at = @At("HEAD"), cancellable = true)
    public void stoneycore$getUseAction(ItemStack stack, CallbackInfoReturnable<UseAction> cir) {
        if (stack.getItem() instanceof SCWeapon) {
            cir.setReturnValue(stack.isIn(SCTags.WEAPONS_SHIELD.getTag()) ? UseAction.BLOCK : UseAction.NONE);
        } else if (stack.getItem() instanceof SCRangeWeapon scRangeWeapon) {
            cir.setReturnValue(scRangeWeapon.useAction() == UseAction.BOW ? UseAction.BOW : UseAction.NONE);
        }
    }

    @Inject(method = "getMaxUseTime", at = @At("HEAD"), cancellable = true)
    public void stoneycore$getMaxUseTime(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.getItem() instanceof SCRangeWeapon scRangeWeapon) {
            cir.setReturnValue(scRangeWeapon.maxUseTime());
        }
    }

    @Inject(method = "usageTick", at = @At("HEAD"))
    public void stoneycore$usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks, CallbackInfo ci) {
        if (world.isClient
                || !(user instanceof PlayerEntity player)
                || !(stack.getItem() instanceof SCRangeWeapon scRangeWeapon)
                || scRangeWeapon.ammoRequirement() != null) {
            return;
        }

        int useTime = scRangeWeapon.maxUseTime() - remainingUseTicks;
        if (scRangeWeapon.useAction() == UseAction.CROSSBOW) {
            handleCrossbowCharging(world, stack, player, scRangeWeapon, useTime);
        }
    }

    @Unique
    private void handleCrossbowCharging(World world, ItemStack stack, PlayerEntity player,
                                        SCRangeWeapon weapon, int useTime) {
        float pullProgress = SCRangeWeaponUtil.getCrossbowPullProgress(useTime, weapon);
        SCRangeWeaponUtil.WeaponState weaponState = SCRangeWeaponUtil.getWeaponState(stack);

        if (pullProgress >= 1.0F && !weaponState.isCharged()) {
            SCRangeWeaponUtil.getArrowFromInventory(player).ifPresent(arrowStack ->
                    SCRangeWeaponUtil.loadAndPlayCrossbowSound(world, stack, player, arrowStack)
            );
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void stoneycore$use(World world, PlayerEntity user, Hand hand,
                                    CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = user.getStackInHand(hand);

        if (stack.getItem() instanceof SCWeapon scWeapon) {
            handlescWeaponUse(world, user, hand, stack, scWeapon, cir);
        } else if (stack.getItem() instanceof SCRangeWeapon scRangeWeapon) {
            handleRangeWeaponUse(world, user, hand, stack, scRangeWeapon, cir);
        }
    }

    @Unique
    private void handlescWeaponUse(World world, PlayerEntity user, Hand hand, ItemStack stack,
                                   SCWeapon scWeapon, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (!world.isClient && user.isSneaking() && isBludgeoningWeapon(scWeapon)) {
            toggleBludgeoningMode(stack);
            cir.setReturnValue(TypedActionResult.success(stack));
            return;
        }

        user.setCurrentHand(hand);
        cir.setReturnValue(stack.isIn(SCTags.WEAPONS_SHIELD.getTag())
                ? TypedActionResult.consume(stack)
                : TypedActionResult.fail(stack));
    }

    @Unique
    private boolean isBludgeoningWeapon(SCWeapon scWeapon) {
        return scWeapon.getAttackDamageValues()[2] > 0;
    }

    @Unique
    private void toggleBludgeoningMode(ItemStack stack) {
        stack.getOrCreateNbt().putBoolean(NBT_BLUDGEONING_KEY,
                !stack.getOrCreateNbt().getBoolean(NBT_BLUDGEONING_KEY));
    }

    @Unique
    private void handleRangeWeaponUse(World world, PlayerEntity user, Hand hand, ItemStack stack,
                                      SCRangeWeapon weapon, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (world.isClient) return;

        if (weapon.ammoRequirement() != null) {
            handleAmmoBasedWeapon(world, user, hand, stack, weapon, cir);
        } else {
            handleProjectileWeapon(hand, user, stack, weapon, cir);
        }
    }

    @Unique
    private void handleAmmoBasedWeapon(World world, PlayerEntity user, Hand hand, ItemStack stack,
                                       SCRangeWeapon weapon, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        SCRangeWeaponUtil.WeaponState weaponState = SCRangeWeaponUtil.getWeaponState(stack);

        if (!weaponState.isCharged()) {
            cir.setReturnValue(TypedActionResult.fail(stack));
            return;
        }

        ItemStack offHandStack = user.getOffHandStack();
        if (weapon.needsFlintAndSteel() && offHandStack.getItem() != Items.FLINT_AND_STEEL && !user.isCreative()) {
            cir.setReturnValue(TypedActionResult.fail(stack));
            return;
        }

        user.setCurrentHand(hand);
        SCRangeWeaponUtil.shootBullet(world, stack, weapon, user);
        SCRangeWeaponUtil.setWeaponState(stack, new SCRangeWeaponUtil.WeaponState(
                weaponState.isReloading(), false, true));

        if (!user.getAbilities().creativeMode && weapon.needsFlintAndSteel() && user instanceof ServerPlayerEntity serverPlayer) {
            offHandStack.damage(1, serverPlayer, p -> p.sendToolBreakStatus(hand));
        }

        cir.setReturnValue(TypedActionResult.consume(stack));
    }

    @Unique
    private void handleProjectileWeapon(Hand hand, PlayerEntity user, ItemStack stack, SCRangeWeapon weapon,
                                        CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        boolean hasAmmo = SCRangeWeaponUtil.getArrowFromInventory(user).isPresent();

        if (!hasAmmo) {
            cir.setReturnValue(TypedActionResult.fail(stack));
            return;
        }

        user.setCurrentHand(hand);
        if (weapon.useAction() == UseAction.CROSSBOW) {
            cir.setReturnValue(SCRangeWeaponUtil.handleCrossbowUse(user.getWorld(), user, hand, weapon, stack));
        } else {
            cir.setReturnValue(TypedActionResult.consume(stack));
        }
    }

    @Inject(method = "onStoppedUsing", at = @At("HEAD"))
    public void stoneycore$onStoppedUsing(ItemStack stack, World world, LivingEntity user,
                                               int remainingUseTicks, CallbackInfo ci) {
        if (world.isClient
                || !(user instanceof PlayerEntity player)
                || !(stack.getItem() instanceof SCRangeWeapon scRangeWeapon)) {
            return;
        }

        int useTime = scRangeWeapon.maxUseTime() - remainingUseTicks;
        SCRangeWeaponUtil.getArrowFromInventory(player).ifPresent(arrowStack ->
                handleWeaponRelease(stack, world, player, scRangeWeapon, useTime, arrowStack)
        );
    }

    @Unique
    private void handleWeaponRelease(ItemStack stack, World world, PlayerEntity player,
                                     SCRangeWeapon weapon, int useTime, ItemStack arrowStack) {
        if (weapon.useAction() == UseAction.BOW) {
            handleBowRelease(world, stack, weapon, player, arrowStack, useTime);
        } else if (weapon.useAction() == UseAction.CROSSBOW) {
            handleCrossbowRelease(stack, useTime, weapon);
        }
    }

    @Unique
    private void handleBowRelease(World world, ItemStack stack, SCRangeWeapon weapon,
                                  PlayerEntity player, ItemStack arrowStack, int useTime) {
        float pullProgress = SCRangeWeaponUtil.getBowPullProgress(useTime);
        if (pullProgress > 0.1f) {
            SCRangeWeaponUtil.shootArrow(world, stack, weapon, player, arrowStack, pullProgress);
        }
    }

    @Unique
    private void handleCrossbowRelease(ItemStack stack, int useTime, SCRangeWeapon weapon) {
        float pullProgress = SCRangeWeaponUtil.getCrossbowPullProgress(useTime, weapon);
        if (pullProgress < 1.0F) {
            SCRangeWeaponUtil.WeaponState currentState = SCRangeWeaponUtil.getWeaponState(stack);
            SCRangeWeaponUtil.setWeaponState(stack, new SCRangeWeaponUtil.WeaponState(
                    false, currentState.isCharged(), currentState.isShooting()));
        }
    }

    @Inject(method = "appendTooltip", at = @At("HEAD"))
    public void stoneycore$appendTooltip(ItemStack stack, World world, List<Text> tooltip,
                                              TooltipContext context, CallbackInfo ci) {
        if (world == null) return;
        if (stack.getItem() instanceof SCWeapon scWeapon) {
            float[] attackDamageValues = scWeapon.getAttackDamageValues();
            float slashingDamage = attackDamageValues[0];
            float piercingDamage = attackDamageValues[1];
            float bludgeoningDamage = attackDamageValues[2];

            if (slashingDamage > 0 && bludgeoningDamage > 0 && piercingDamage == 0) {
                tooltip.add(Text.translatable("tooltip.stoneycore.shift-right_click-bludgeoning"));
            }

            if (slashingDamage == 0 && bludgeoningDamage > 0 && piercingDamage > 0) {
                tooltip.add(Text.translatable("tooltip.stoneycore.shift-right_click-bludgeoning-piercing"));
            }

            if (stack.isIn(SCTags.WEAPONS_HARVEST.getTag())) {
                tooltip.add(Text.translatable("tooltip.stoneycore.right_click-replant"));
            }

            if (slashingDamage != 0) tooltip.add(Text.translatable("text.tooltip.stoneycore.slashingDamage", (int) slashingDamage).formatted(Formatting.GREEN));
            if (bludgeoningDamage != 0) tooltip.add(Text.translatable("text.tooltip.stoneycore.bludgeoningDamage", (int) bludgeoningDamage).formatted(Formatting.GREEN));
            if (piercingDamage != 0) tooltip.add(Text.translatable("text.tooltip.stoneycore.piercingDamage", (int) piercingDamage).formatted(Formatting.GREEN));
        }

        if (stack.getItem() instanceof SCRangeWeapon scRangeWeapon && scRangeWeapon.ammoRequirement() != null && world.isClient()) {
            TooltipClientSide.setTooltip(tooltip);
        }
    }

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    public void stoneycore$useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = context.getStack();
        if (!(stack.getItem() instanceof SCWeapon) || !stack.isIn(SCTags.WEAPONS_HARVEST.getTag())) {
            return;
        }

        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        BlockState state = world.getBlockState(pos);

        if (!world.isClient && player != null) {
            handleCropHarvest(world, pos, state, player, stack, context.getHand(), cir);
        }
    }

    @Unique
    private void handleCropHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player,
                                   ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        Block block = state.getBlock();
        if (block instanceof CropBlock crop && crop.isMature(state) && world.breakBlock(pos, true, player)) {
            SCWeaponUtil.replantCrop(world, pos, crop, player, stack, hand);
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }

    @Inject(method = "onCraft", at = @At("TAIL"))
    public void onCraft(ItemStack stack, World world, PlayerEntity player, CallbackInfo ci) {
        if (!(stack.getItem() instanceof SCTrinketsItem)) return;

        if (player.currentScreenHandler instanceof CraftingScreenHandler craftingInventory) {
            applyCraftingModifiers(stack, craftingInventory.getCraftingSlotCount(), craftingInventory::getSlot);
        } else if (player.currentScreenHandler instanceof PlayerScreenHandler playerInventory) {
            applyCraftingModifiers(stack, 4, playerInventory::getSlot);
        }
    }

    @Unique
    private void applyCraftingModifiers(ItemStack stack, int slotCount, java.util.function.IntFunction<Slot> slotSupplier) {
        ItemStack bannerStack = ItemStack.EMPTY;

        for (int i = 0; i < slotCount; i++) {
            ItemStack ingredient = slotSupplier.apply(i).getStack();
            if (ingredient.getItem() instanceof BannerItem) {
                bannerStack = ingredient;
                break;
            }
        }

        if (!bannerStack.isEmpty() && stack.getItem() instanceof SCTrinketsItem armorItem) {
            List<Identifier> bannerPatterns = getBannerPatternIdentifiers(bannerStack, stack.getItem());
            armorItem.setBannerPatterns(stack, bannerPatterns);
        }
    }

    @Unique
    private List<Identifier> getBannerPatternIdentifiers(ItemStack bannerStack, Item armor) {
        List<Identifier> patterns = new ArrayList<>();
        if (!bannerStack.isEmpty() && bannerStack.getItem() instanceof BannerItem) {
            NbtCompound nbt = bannerStack.getNbt();
            if (nbt != null && nbt.contains("BlockEntityTag")) {
                NbtCompound blockEntityTag = nbt.getCompound("BlockEntityTag");
                if (blockEntityTag.contains("Patterns")) {
                    NbtList patternList = blockEntityTag.getList("Patterns", NbtCompound.COMPOUND_TYPE);
                    for (int i = 0; i < patternList.size(); i++) {
                        NbtCompound patternTag = patternList.getCompound(i);
                        String pattern = patternTag.getString("Pattern");
                        int color = patternTag.getInt("Color");
                        Identifier itemId = Registries.ITEM.getId(armor);
                        String itemIdPath = itemId.getPath();
                        String modId = itemId.getNamespace();
                        patterns.add(new Identifier(modId, "textures/banner_pattern/" + itemIdPath + "/" + pattern + "_" + color));
                    }
                }
            }
        }
        return patterns;
    }
}