package banduty.stoneycore.mixin;

import banduty.stoneycore.combat.range.RangedWeaponHandlers;
import banduty.stoneycore.items.armor.SCAccessoryItem;
import banduty.stoneycore.util.SCDamageCalculator;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsLoader;
import banduty.stoneycore.util.patterns.PatternHelper;
import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.StaminaData;
import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import banduty.stoneycore.util.weaponutil.SCWeaponUtil;
import banduty.stoneycore.util.weaponutil.TooltipClientSide;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;

import static banduty.stoneycore.util.weaponutil.SCWeaponUtil.getDamageValues;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Shadow
    public abstract UseAction getUseAction(ItemStack stack);

    @Inject(method = "getUseAction", at = @At("HEAD"), cancellable = true)
    public void stoneycore$getUseAction(ItemStack stack, CallbackInfoReturnable<UseAction> cir) {
        if (WeaponDefinitionsLoader.isMelee(stack)) {
            boolean isShield = stack.isIn(SCTags.WEAPONS_SHIELD.getTag());
            cir.setReturnValue(isShield ? UseAction.BLOCK : UseAction.NONE);
            return;
        }

        if (WeaponDefinitionsLoader.isRanged(stack)) {
            UseAction configured = WeaponDefinitionsLoader.getData(stack).ranged().useAction();
            cir.setReturnValue(configured == UseAction.BOW ? UseAction.BOW : UseAction.NONE);
        }
    }

    @Inject(method = "getMaxUseTime", at = @At("HEAD"), cancellable = true)
    public void stoneycore$getMaxUseTime(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (WeaponDefinitionsLoader.isRanged(stack)) {
            cir.setReturnValue(WeaponDefinitionsLoader.getData(stack).ranged().maxUseTime());
            return;
        }

        if (WeaponDefinitionsLoader.isMelee(stack) && stack.isIn(SCTags.WEAPONS_SHIELD.getTag())) {
            cir.setReturnValue(72000);
        }
    }

    @Inject(method = "usageTick", at = @At("HEAD"))
    public void stoneycore$usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks, CallbackInfo ci) {
        if (user instanceof PlayerEntity playerEntity) {
            IEntityDataSaver dataSaver = (IEntityDataSaver) playerEntity;
            if (StaminaData.isStaminaBlocked(dataSaver)) {
                playerEntity.clearActiveItem();
            }
        }

        if (world.isClient) return;
        if (!(user instanceof PlayerEntity player)) return;
        if (!WeaponDefinitionsLoader.isRanged(stack)) return;

        int useTime = WeaponDefinitionsLoader.getData(stack).ranged().maxUseTime() - remainingUseTicks;
        if (RangedWeaponHandlers.get(WeaponDefinitionsLoader.getData(stack).ranged().id()).isPresent())
            RangedWeaponHandlers.get(WeaponDefinitionsLoader.getData(stack).ranged().id()).get().handleUsageTick(world, stack, player, useTime);
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void stoneycore$use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = user.getStackInHand(hand);

        if ((getUseAction(stack) == UseAction.DRINK || getUseAction(stack) == UseAction.EAT)) {
            Optional.ofNullable(AccessoriesCapability.getOptionally(user))
                    .filter(Optional::isPresent)
                    .ifPresent(o -> {
                        for (SlotEntryReference equipped : AccessoriesCapability.get(user).getAllEquipped()) {
                            ItemStack itemStack = equipped.stack();
                            if (!NBTDataHelper.get(itemStack, INBTKeys.VISOR_OPEN, false) && itemStack.isIn(SCTags.VISORED_HELMET.getTag())) {
                                cir.setReturnValue(TypedActionResult.fail(stack));
                                return;
                            }
                        }
                    });
        }

        if (hand == Hand.MAIN_HAND && !user.getOffHandStack().isEmpty() && WeaponDefinitionsLoader.isMelee(stack)) {
            cir.setReturnValue(TypedActionResult.fail(stack));
            return;
        }

        if (hand == Hand.OFF_HAND) return;

        if (WeaponDefinitionsLoader.isMelee(stack)) {
            handleWeaponUse(world, user, hand, stack, cir);
        } else if (WeaponDefinitionsLoader.isRanged(stack)) {
            handleRangeWeaponUse(world, user, hand, stack, cir);
        }
    }

    @Unique
    private void handleWeaponUse(World world, PlayerEntity user, Hand hand, ItemStack stack, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        // Toggle bludgeoning mode when sneaking and server-side
        if (!world.isClient && user.isSneaking() && isBludgeoningWeapon(stack.getItem())) {
            toggleBludgeoningMode(stack);
            cir.setReturnValue(TypedActionResult.success(stack));
            return;
        }

        IEntityDataSaver dataSaver = (IEntityDataSaver) user;
        if (StaminaData.isStaminaBlocked(dataSaver)) {
            user.clearActiveItem();
            cir.setReturnValue(TypedActionResult.fail(stack));
            return;
        }

        if (stack.isIn(SCTags.WEAPONS_SHIELD.getTag())) {
            user.setCurrentHand(hand);
            cir.setReturnValue(TypedActionResult.consume(stack));
            return;
        }

        cir.setReturnValue(TypedActionResult.fail(stack));
    }

    @Unique
    private boolean isBludgeoningWeapon(Item item) {
        return getDamageValues(SCDamageCalculator.DamageType.BLUDGEONING, item) > 0;
    }

    @Unique
    private void toggleBludgeoningMode(ItemStack stack) {
        boolean current = NBTDataHelper.get(stack, INBTKeys.BLUDGEONING, false);
        NBTDataHelper.set(stack, INBTKeys.BLUDGEONING, !current);
    }

    @Unique
    private void handleRangeWeaponUse(World world, PlayerEntity user, Hand hand, ItemStack stack, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (world.isClient) return;

        if (SCRangeWeaponUtil.getAmmoRequirement(stack) != null) {
            handleAmmoBasedWeapon(world, user, hand, stack, cir);
        } else {
            handleProjectileWeapon(hand, user, stack, cir);
        }
    }

    @Unique
    private void handleAmmoBasedWeapon(World world, PlayerEntity user, Hand hand, ItemStack stack, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        SCRangeWeaponUtil.WeaponState weaponState = SCRangeWeaponUtil.getWeaponState(stack);

        if (!weaponState.isCharged()) {
            cir.setReturnValue(TypedActionResult.fail(stack));
            return;
        }

        ItemStack offHandStack = user.getOffHandStack();
        boolean needsFAS = WeaponDefinitionsLoader.getData(stack).ranged().needsFlintAndSteel();
        if (needsFAS && offHandStack.getItem() != Items.FLINT_AND_STEEL && !user.isCreative()) {
            cir.setReturnValue(TypedActionResult.fail(stack));
            return;
        }

        user.setCurrentHand(hand);
        SCRangeWeaponUtil.handleShoot(world, user, stack);
        SCRangeWeaponUtil.setWeaponState(stack, new SCRangeWeaponUtil.WeaponState(
                weaponState.isReloading(), false, true));

        if (!user.getAbilities().creativeMode && needsFAS && user instanceof ServerPlayerEntity serverPlayer) {
            offHandStack.damage(1, serverPlayer, p -> p.sendToolBreakStatus(hand));
        }

        cir.setReturnValue(TypedActionResult.consume(stack));
    }

    @Unique
    private void handleProjectileWeapon(Hand hand, PlayerEntity user, ItemStack stack, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        boolean hasAmmo = SCRangeWeaponUtil.getArrowFromInventory(user).isPresent();
        if (!hasAmmo) {
            cir.setReturnValue(TypedActionResult.fail(stack));
            return;
        }

        user.setCurrentHand(hand);

        if (WeaponDefinitionsLoader.getData(stack).ranged().useAction() == UseAction.CROSSBOW) {
            cir.setReturnValue(SCRangeWeaponUtil.handleCrossbowUse(user.getWorld(), user, hand, stack));
        } else {
            cir.setReturnValue(TypedActionResult.consume(stack));
        }
    }

    @Inject(method = "onStoppedUsing", at = @At("HEAD"))
    public void stoneycore$onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (world.isClient) return;
        if (!(user instanceof PlayerEntity player)) return;
        if (!WeaponDefinitionsLoader.isRanged(stack)) return;

        int useTime = WeaponDefinitionsLoader.getData(stack).ranged().maxUseTime() - remainingUseTicks;
        SCRangeWeaponUtil.getArrowFromInventory(player).ifPresent(arrowStack ->
                handleWeaponRelease(stack, world, player, useTime, arrowStack)
        );
    }

    @Unique
    private void handleWeaponRelease(ItemStack stack, World world, PlayerEntity player, int useTime, ItemStack arrowStack) {
        UseAction useAction = WeaponDefinitionsLoader.getData(stack).ranged().useAction();
        if (useAction == UseAction.BOW) {
            handleBowRelease(world, stack, player, arrowStack, useTime);
        } else if (useAction == UseAction.CROSSBOW) {
            handleCrossbowRelease(stack, useTime);
        }
    }

    @Unique
    private void handleBowRelease(World world, ItemStack stack, PlayerEntity player, ItemStack arrowStack, int useTime) {
        float pullProgress = SCRangeWeaponUtil.getBowPullProgress(useTime);
        if (pullProgress > 0.1f) {
            SCRangeWeaponUtil.shootArrow(world, stack, player, arrowStack, pullProgress);
        }
    }

    @Unique
    private void handleCrossbowRelease(ItemStack stack, int useTime) {
        float pullProgress = SCRangeWeaponUtil.getCrossbowPullProgress(useTime, stack);
        if (pullProgress < 1.0F) {
            SCRangeWeaponUtil.WeaponState currentState = SCRangeWeaponUtil.getWeaponState(stack);
            SCRangeWeaponUtil.setWeaponState(stack, new SCRangeWeaponUtil.WeaponState(
                    false, currentState.isCharged(), currentState.isShooting()));
        }
    }

    @Inject(method = "appendTooltip", at = @At("HEAD"))
    public void stoneycore$appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci) {
        if (world == null) return;

        Item item = stack.getItem();
        if (WeaponDefinitionsLoader.isMelee(stack)) {
            double slashing = getDamageValues(SCDamageCalculator.DamageType.SLASHING, item);
            double piercing = getDamageValues(SCDamageCalculator.DamageType.PIERCING, item);
            double bludgeoning = getDamageValues(SCDamageCalculator.DamageType.BLUDGEONING, item);

            if (slashing > 0 && bludgeoning > 0) {
                tooltip.add(Text.translatable("text.tooltip.stoneycore.shift-right_click-bludgeoning"));
            }

            if (slashing == 0 && bludgeoning > 0 && piercing > 0) {
                tooltip.add(Text.translatable("text.tooltip.stoneycore.shift-right_click-bludgeoning-piercing"));
            }

            if (stack.isIn(SCTags.WEAPONS_HARVEST.getTag())) {
                tooltip.add(Text.translatable("text.tooltip.stoneycore.right_click-replant"));
            }

            if (slashing != 0) tooltip.add(Text.translatable("text.tooltip.stoneycore.slashingDamage", slashing).formatted(Formatting.GREEN));
            if (bludgeoning != 0) tooltip.add(Text.translatable("text.tooltip.stoneycore.bludgeoningDamage", bludgeoning).formatted(Formatting.GREEN));
            if (piercing != 0) tooltip.add(Text.translatable("text.tooltip.stoneycore.piercingDamage", piercing).formatted(Formatting.GREEN));
        }

        if (world.isClient()) {
            TooltipClientSide.setTooltip(tooltip, stack);
        }
    }

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    public void stoneycore$useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = context.getStack();
        if (!WeaponDefinitionsLoader.isMelee(stack) || !stack.isIn(SCTags.WEAPONS_HARVEST.getTag())) return;

        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        BlockState state = world.getBlockState(pos);

        if (!world.isClient && player != null) {
            handleCropHarvest(world, pos, state, player, stack, context.getHand(), cir);
        }
    }

    @Unique
    private void handleCropHarvest(World world, BlockPos pos, BlockState state, PlayerEntity player, ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        Block block = state.getBlock();
        if (block instanceof CropBlock crop && crop.isMature(state) && world.breakBlock(pos, true, player)) {
            SCWeaponUtil.replantCrop(world, pos, crop, player, stack, hand);
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }

    @Inject(method = "onCraft", at = @At("TAIL"))
    public void onCraft(ItemStack stack, World world, PlayerEntity player, CallbackInfo ci) {
        if (!(stack.getItem() instanceof SCAccessoryItem)) return;

        if (player.currentScreenHandler instanceof CraftingScreenHandler craftingInventory) {
            applyCraftingModifiers(stack, craftingInventory.getCraftingSlotCount(), craftingInventory::getSlot);
        } else if (player.currentScreenHandler instanceof PlayerScreenHandler playerInventory) {
            applyCraftingModifiers(stack, 4, playerInventory::getSlot);
        }
    }

    @Unique
    private void applyCraftingModifiers(ItemStack resultStack, int slotCount, IntFunction<Slot> slotSupplier) {
        ItemStack bannerStack = ItemStack.EMPTY;

        for (int i = 0; i < slotCount; i++) {
            ItemStack ingredient = slotSupplier.apply(i).getStack();
            if (ingredient.getItem() instanceof BannerItem) {
                bannerStack = ingredient;
                break;
            }
        }

        if (bannerStack.isEmpty() || !(resultStack.getItem() instanceof SCAccessoryItem) || !resultStack.isIn(SCTags.BANNER_COMPATIBLE.getTag())) return;

        List<Pair<Identifier, DyeColor>> bannerPatterns = getBannerPatterns(bannerStack, resultStack.getItem());
        PatternHelper.setBannerPatterns(resultStack, bannerPatterns);
        PatternHelper.setBannerDyeColor(resultStack, ((BannerItem) bannerStack.getItem()).getColor());
    }

    @Unique
    private static List<Pair<Identifier, DyeColor>> getBannerPatterns(ItemStack bannerStack, Item armor) {
        List<Pair<Identifier, DyeColor>> patterns = new ArrayList<>();

        if (bannerStack.isEmpty() || !(bannerStack.getItem() instanceof BannerItem)) return patterns;

        NbtCompound nbt = bannerStack.getNbt();
        if (nbt == null || !nbt.contains(INBTKeys.BLOCK_ENTITY_TAG)) return patterns;

        NbtCompound blockEntityTag = nbt.getCompound(INBTKeys.BLOCK_ENTITY_TAG);
        if (!blockEntityTag.contains(INBTKeys.PATTERNS)) return patterns;

        NbtList patternList = blockEntityTag.getList(INBTKeys.PATTERNS, NbtCompound.COMPOUND_TYPE);
        Identifier itemId = Registries.ITEM.getId(armor);

        for (int i = 0; i < patternList.size(); i++) {
            NbtCompound patternTag = patternList.getCompound(i);

            String pattern = NBTDataHelper.get(patternTag, INBTKeys.PATTERN, "");
            int colorId = NBTDataHelper.get(patternTag, INBTKeys.COLOR, 0);

            DyeColor color = DyeColor.byId(colorId);
            Identifier patternId = new Identifier(
                    itemId.getNamespace(),
                    "textures/banner_pattern/" + itemId.getPath() + "/" + pattern + ".png"
            );

            patterns.add(new Pair<>(patternId, color));
        }
        return patterns;
    }
}