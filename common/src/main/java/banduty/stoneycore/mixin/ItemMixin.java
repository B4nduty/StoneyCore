package banduty.stoneycore.mixin;

import banduty.stoneycore.combat.range.RangedWeaponHandlers;
import banduty.stoneycore.items.armor.SCAccessoryItem;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.util.SCDamageCalculator;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.StaminaData;
import banduty.stoneycore.util.definitionsloader.AccessoriesDefinitionsStorage;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionData;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import banduty.stoneycore.util.patterns.PatternHelper;
import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import banduty.stoneycore.util.weaponutil.SCWeaponUtil;
import banduty.stoneycore.util.weaponutil.TooltipClientSide;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
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
    public abstract UseAnim getUseAnimation(ItemStack stack);

    @Inject(method = "getUseAnimation", at = @At("HEAD"), cancellable = true)
    public void stoneycore$getUseAnimation(ItemStack stack, CallbackInfoReturnable<UseAnim> cir) {
        if (WeaponDefinitionsStorage.isMelee(stack)) {
            boolean isShield = stack.is(SCTags.WEAPONS_SHIELD.getTag());
            cir.setReturnValue(isShield ? UseAnim.BLOCK : UseAnim.NONE);
            return;
        }

        if (WeaponDefinitionsStorage.isRanged(stack)) {
            UseAnim configured = WeaponDefinitionsStorage.getData(stack).ranged().useAnim();
            cir.setReturnValue(configured == UseAnim.BOW ? UseAnim.BOW : UseAnim.NONE);
        }
    }

    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    public void stoneycore$getUseDuration(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (WeaponDefinitionsStorage.isRanged(stack)) {
            cir.setReturnValue(WeaponDefinitionsStorage.getData(stack).ranged().maxUseTime());
            return;
        }

        if (WeaponDefinitionsStorage.isMelee(stack) && stack.is(SCTags.WEAPONS_SHIELD.getTag())) {
            cir.setReturnValue(72000);
        }
    }

    @Inject(method = "onUseTick", at = @At("HEAD"))
    public void stoneycore$onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseTicks, CallbackInfo ci) {
        if (!(livingEntity instanceof Player player)) return;

        IEntityDataSaver dataSaver = (IEntityDataSaver) player;
        if (StaminaData.isStaminaBlocked(dataSaver)) {
            player.stopUsingItem();
        }

        if (level.isClientSide()) return;
        if (!WeaponDefinitionsStorage.isRanged(stack)) return;

        WeaponDefinitionData data = WeaponDefinitionsStorage.getData(stack);
        int useTime = data.ranged().maxUseTime() - remainingUseTicks;
        if (RangedWeaponHandlers.get(data.ranged().id()).isPresent()) {
            RangedWeaponHandlers.get(data.ranged().id()).get().handleUsageTick(level, stack, player, useTime);
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void stoneycore$use(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = player.getItemInHand(hand);

        if ((getUseAnimation(stack) == UseAnim.DRINK || getUseAnimation(stack) == UseAnim.EAT)) {
            for (ItemStack itemStack : Services.PLATFORM.getEquippedAccessories(player)) {
                if (player.isCreative()) break;
                if (!NBTDataHelper.get(itemStack, INBTKeys.VISOR_OPEN, false) && !AccessoriesDefinitionsStorage.getData(itemStack).visoredHelmet().getPath().isBlank()) {
                    player.displayClientMessage(Component.translatable("component.tooltip.stoneycore.openVisorEatDrink"), true);
                    cir.setReturnValue(InteractionResultHolder.fail(stack));
                    return;
                }
            }
        }

        if (hand == InteractionHand.MAIN_HAND && !player.getOffhandItem().isEmpty() && WeaponDefinitionsStorage.isMelee(stack)) {
            cir.setReturnValue(InteractionResultHolder.fail(stack));
            return;
        }

        if (hand == InteractionHand.OFF_HAND) return;

        if (WeaponDefinitionsStorage.isMelee(stack)) {
            stoneyCore$handleWeaponUse(level, player, hand, stack, cir);
        } else if (WeaponDefinitionsStorage.isRanged(stack)) {
            stoneyCore$handleRangeWeaponUse(level, player, hand, stack, cir);
        }
    }

    @Unique
    private void stoneyCore$handleWeaponUse(Level level, Player player, InteractionHand hand, ItemStack stack, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (!level.isClientSide() && player.isShiftKeyDown() && stoneyCore$isBludgeoningWeapon(stack.getItem())) {
            stoneyCore$toggleBludgeoningMode(stack);
            cir.setReturnValue(InteractionResultHolder.success(stack));
            return;
        }

        IEntityDataSaver dataSaver = (IEntityDataSaver) player;
        if (StaminaData.isStaminaBlocked(dataSaver)) {
            player.stopUsingItem();
            cir.setReturnValue(InteractionResultHolder.fail(stack));
            return;
        }

        if (stack.is(SCTags.WEAPONS_SHIELD.getTag())) {
            player.startUsingItem(hand);
            cir.setReturnValue(InteractionResultHolder.consume(stack));
            return;
        }

        cir.setReturnValue(InteractionResultHolder.fail(stack));
    }

    @Unique
    private boolean stoneyCore$isBludgeoningWeapon(Item item) {
        return getDamageValues(SCDamageCalculator.DamageType.BLUDGEONING, item) > 0;
    }

    @Unique
    private void stoneyCore$toggleBludgeoningMode(ItemStack stack) {
        boolean current = NBTDataHelper.get(stack, INBTKeys.BLUDGEONING, false);
        NBTDataHelper.set(stack, INBTKeys.BLUDGEONING, !current);
    }

    @Unique
    private void stoneyCore$handleRangeWeaponUse(Level level, Player player, InteractionHand hand, ItemStack stack, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (level.isClientSide()) return;

        if (SCRangeWeaponUtil.getAmmoRequirement(stack) != SCRangeWeaponUtil.AmmoRequirement.EMPTY) {
            stoneyCore$handleAmmoBasedWeapon(level, player, hand, stack, cir);
        } else {
            stoneyCore$handleProjectileWeapon(hand, player, stack, cir);
        }
    }

    @Unique
    private void stoneyCore$handleAmmoBasedWeapon(Level level, Player player, InteractionHand hand, ItemStack stack, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        SCRangeWeaponUtil.WeaponState weaponState = SCRangeWeaponUtil.getWeaponState(stack);

        if (!weaponState.isCharged()) {
            cir.setReturnValue(InteractionResultHolder.fail(stack));
            return;
        }

        ItemStack offHandStack = player.getOffhandItem();
        boolean needsFAS = WeaponDefinitionsStorage.getData(stack).ranged().needsFlintAndSteel();
        if (needsFAS && offHandStack.getItem() != Items.FLINT_AND_STEEL && !player.isCreative()) {
            cir.setReturnValue(InteractionResultHolder.fail(stack));
            return;
        }

        player.startUsingItem(hand);
        SCRangeWeaponUtil.handleShoot(level, player, stack);
        SCRangeWeaponUtil.setWeaponState(stack, new SCRangeWeaponUtil.WeaponState(
                weaponState.isReloading(), false, true));

        if (!player.getAbilities().instabuild && needsFAS && player instanceof ServerPlayer serverPlayer) {
            offHandStack.hurtAndBreak(1, serverPlayer, p -> p.broadcastBreakEvent(hand));
        }

        cir.setReturnValue(InteractionResultHolder.consume(stack));
    }

    @Unique
    private void stoneyCore$handleProjectileWeapon(InteractionHand hand, Player player, ItemStack stack, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        boolean hasAmmo = SCRangeWeaponUtil.getArrowFromInventory(player).isPresent();
        if (!hasAmmo) {
            cir.setReturnValue(InteractionResultHolder.fail(stack));
            return;
        }

        player.startUsingItem(hand);

        if (WeaponDefinitionsStorage.getData(stack).ranged().useAnim() == UseAnim.CROSSBOW) {
            cir.setReturnValue(SCRangeWeaponUtil.handleCrossbowUse(player.level(), player, hand, stack));
        } else {
            cir.setReturnValue(InteractionResultHolder.consume(stack));
        }
    }

    @Inject(method = "releaseUsing", at = @At("HEAD"))
    public void stoneycore$releaseUsing(ItemStack stack, Level level, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (level.isClientSide()) return;
        if (!(user instanceof Player player)) return;
        if (!WeaponDefinitionsStorage.isRanged(stack)) return;
        var def = WeaponDefinitionsStorage.getData(stack);
        if (def == null || def.ranged() == null) return;
        String type = def.ranged().id();

        int useTime = WeaponDefinitionsStorage.getData(stack).ranged().maxUseTime() - remainingUseTicks;
        SCRangeWeaponUtil.getArrowFromInventory(player).ifPresent(arrowStack ->
                RangedWeaponHandlers.get(type).ifPresent(h -> h.handleRelease(stack, level, player, useTime, arrowStack)
        ));
    }

    @Inject(method = "appendHoverText", at = @At("HEAD"))
    public void stoneycore$appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag tooltipFlag, CallbackInfo ci) {
        if (level == null) return;

        Item item = stack.getItem();
        if (WeaponDefinitionsStorage.isMelee(stack)) {
            double slashing = getDamageValues(SCDamageCalculator.DamageType.SLASHING, item);
            double piercing = getDamageValues(SCDamageCalculator.DamageType.PIERCING, item);
            double bludgeoning = getDamageValues(SCDamageCalculator.DamageType.BLUDGEONING, item);

            if (slashing > 0 && bludgeoning > 0) {
                tooltip.add(Component.translatable("component.tooltip.stoneycore.shift-right_click-bludgeoning"));
            }

            if (slashing == 0 && bludgeoning > 0 && piercing > 0) {
                tooltip.add(Component.translatable("component.tooltip.stoneycore.shift-right_click-bludgeoning-piercing"));
            }

            if (stack.is(SCTags.WEAPONS_HARVEST.getTag())) {
                tooltip.add(Component.translatable("component.tooltip.stoneycore.right_click-replant"));
            }

            if (stack.is(SCTags.BROKEN_WEAPONS.getTag()) && stack.getDamageValue() >= stack.getMaxDamage() * 0.9f){
                slashing *= 0.25f; bludgeoning *= 0.25f; piercing *= 0.25f;
            }

            if (slashing != 0) tooltip.add(Component.translatable("component.tooltip.stoneycore.slashingDamage", slashing).withStyle(ChatFormatting.GREEN));
            if (bludgeoning != 0) tooltip.add(Component.translatable("component.tooltip.stoneycore.bludgeoningDamage", bludgeoning).withStyle(ChatFormatting.GREEN));
            if (piercing != 0) tooltip.add(Component.translatable("component.tooltip.stoneycore.piercingDamage", piercing).withStyle(ChatFormatting.GREEN));
        }

        if (level.isClientSide()) {
            TooltipClientSide.setTooltip(tooltip, stack);
        }
    }

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    public void stoneycore$useOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = context.getItemInHand();
        if (!WeaponDefinitionsStorage.isMelee(stack) || !stack.is(SCTags.WEAPONS_HARVEST.getTag())) return;

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState state = level.getBlockState(pos);

        if (!level.isClientSide() && player != null) {
            stoneyCore$handleCropHarvest(level, pos, state, player, stack, context.getHand(), cir);
        }
    }

    @Unique
    private void stoneyCore$handleCropHarvest(Level world, BlockPos pos, BlockState state, Player player, ItemStack stack, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        Block block = state.getBlock();
        if (block instanceof CropBlock crop && crop.isMaxAge(state) && world.destroyBlock(pos, true, player)) {
            SCWeaponUtil.replantCrop(world, pos, crop, player, stack, hand);
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }

    @Inject(method = "onCraftedBy", at = @At("TAIL"))
    public void onCraftedBy(ItemStack stack, Level level, Player player, CallbackInfo ci) {
        if (!(stack.getItem() instanceof SCAccessoryItem)) return;

        if (player.containerMenu instanceof CraftingMenu craftingInventory) {
            stoneyCore$applyCraftingModifiers(stack, craftingInventory.getSize(), craftingInventory::getSlot);
        } else if (player.containerMenu instanceof InventoryMenu inventoryMenu) {
            stoneyCore$applyCraftingModifiers(stack, 4, inventoryMenu::getSlot);
        }
    }

    @Unique
    private void stoneyCore$applyCraftingModifiers(ItemStack resultStack, int slotCount, IntFunction<Slot> slotSupplier) {
        ItemStack bannerStack = ItemStack.EMPTY;

        for (int i = 0; i < slotCount; i++) {
            ItemStack ingredient = slotSupplier.apply(i).getItem();
            if (ingredient.getItem() instanceof BannerItem) {
                bannerStack = ingredient;
                break;
            }
        }

        if (bannerStack.isEmpty() || !(resultStack.getItem() instanceof SCAccessoryItem) || !resultStack.is(SCTags.BANNER_COMPATIBLE.getTag())) return;

        List<Tuple<ResourceLocation, DyeColor>> bannerPatterns = stoneyCore$getBannerPatterns(bannerStack, resultStack.getItem());
        PatternHelper.setBannerPatterns(resultStack, bannerPatterns);
        PatternHelper.setBannerDyeColor(resultStack, ((BannerItem) bannerStack.getItem()).getColor());
    }

    @Unique
    private static List<Tuple<ResourceLocation, DyeColor>> stoneyCore$getBannerPatterns(ItemStack bannerStack, Item armor) {
        List<Tuple<ResourceLocation, DyeColor>> patterns = new ArrayList<>();

        if (bannerStack.isEmpty() || !(bannerStack.getItem() instanceof BannerItem)) return patterns;

        CompoundTag nbt = bannerStack.getTag();
        if (nbt == null || !nbt.contains(INBTKeys.BLOCK_ENTITY_TAG)) return patterns;

        CompoundTag blockEntityTag = nbt.getCompound(INBTKeys.BLOCK_ENTITY_TAG);
        if (!blockEntityTag.contains(INBTKeys.PATTERNS)) return patterns;

        ListTag patternList = blockEntityTag.getList(INBTKeys.PATTERNS, Tag.TAG_COMPOUND);
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(armor);

        for (int i = 0; i < patternList.size(); i++) {
            CompoundTag patternTag = patternList.getCompound(i);

            String pattern = NBTDataHelper.get(patternTag, INBTKeys.PATTERN, "");
            int colorId = NBTDataHelper.get(patternTag, INBTKeys.COLOR, 0);

            DyeColor color = DyeColor.byId(colorId);
            ResourceLocation patternId = new ResourceLocation(
                    itemId.getNamespace(),
                    "textures/banner_pattern/" + itemId.getPath() + "/" + pattern + ".png"
            );

            patterns.add(new Tuple<>(patternId, color));
        }
        return patterns;
    }
}