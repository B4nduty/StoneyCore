package banduty.stoneycore.mixin;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.armor.SCTrinketsItem;
import banduty.stoneycore.util.SCDamageCalculator;
import banduty.stoneycore.util.definitionsloader.SCMeleeWeaponDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.SCRangedWeaponDefinitionsLoader;
import banduty.stoneycore.util.itemdata.SCTags;
import banduty.stoneycore.util.patterns.PatternHelper;
import banduty.stoneycore.util.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.playerdata.StaminaData;
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

import static banduty.stoneycore.util.weaponutil.SCWeaponUtil.getDamageValues;

@Mixin(Item.class)
public class ItemMixin {
    @Unique
    private static final String NBT_BLUDGEONING_KEY = "sc_bludgeoning";

    @Inject(method = "getUseAction", at = @At("HEAD"), cancellable = true)
    public void stoneycore$getUseAction(ItemStack stack, CallbackInfoReturnable<UseAction> cir) {
        if (SCMeleeWeaponDefinitionsLoader.containsItem(stack.getItem())) {
            cir.setReturnValue(stack.isIn(SCTags.WEAPONS_SHIELD.getTag()) ? UseAction.BLOCK : UseAction.NONE);
        } else if (SCRangedWeaponDefinitionsLoader.containsItem(stack.getItem())) {
            cir.setReturnValue(SCRangedWeaponDefinitionsLoader.getData(stack.getItem()).useAction() == UseAction.BOW ? UseAction.BOW : UseAction.NONE);
        }
    }

    @Inject(method = "getMaxUseTime", at = @At("HEAD"), cancellable = true)
    public void stoneycore$getMaxUseTime(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (SCRangedWeaponDefinitionsLoader.containsItem(stack.getItem())) {
            cir.setReturnValue(SCRangedWeaponDefinitionsLoader.getData(stack.getItem()).maxUseTime());
        }
        if (SCMeleeWeaponDefinitionsLoader.containsItem(stack.getItem()) && stack.isIn(SCTags.WEAPONS_SHIELD.getTag())) {
            cir.setReturnValue(72000);
        }
    }

    @Inject(method = "usageTick", at = @At("HEAD"))
    public void stoneycore$usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks, CallbackInfo ci) {
        if (user instanceof PlayerEntity playerEntity) {
            IEntityDataSaver dataSaver = (IEntityDataSaver) playerEntity;
            boolean staminaBlocked = StaminaData.isStaminaBlocked(dataSaver);

            if (staminaBlocked) {
                playerEntity.clearActiveItem();
            }
        }

        if (world.isClient
                || !(user instanceof PlayerEntity player)
                || !(SCRangedWeaponDefinitionsLoader.containsItem(stack.getItem()))
                || SCRangeWeaponUtil.getAmmoRequirement(stack.getItem()) != null) {
            return;
        }

        int useTime = SCRangedWeaponDefinitionsLoader.getData(stack.getItem()).maxUseTime() - remainingUseTicks;
        if (SCRangedWeaponDefinitionsLoader.getData(stack.getItem()).useAction() == UseAction.CROSSBOW) {
            handleCrossbowCharging(world, stack, player, useTime);
        }
    }

    @Unique
    private void handleCrossbowCharging(World world, ItemStack stack, PlayerEntity player, int useTime) {
        float pullProgress = SCRangeWeaponUtil.getCrossbowPullProgress(useTime, stack.getItem());
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

        if (SCMeleeWeaponDefinitionsLoader.containsItem(stack.getItem())) {
            handleWeaponUse(world, user, hand, stack, cir);
        } else if (SCRangedWeaponDefinitionsLoader.containsItem(stack.getItem())) {
            handleRangeWeaponUse(world, user, hand, stack, cir);
        }
    }

    @Unique
    private void handleWeaponUse(World world, PlayerEntity user, Hand hand, ItemStack stack,
                                 CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (!world.isClient && user.isSneaking() && isBludgeoningWeapon(stack.getItem())) {
            toggleBludgeoningMode(stack);
            cir.setReturnValue(TypedActionResult.success(stack));
            return;
        }

        IEntityDataSaver dataSaver = (IEntityDataSaver) user;
        boolean staminaBlocked = StaminaData.isStaminaBlocked(dataSaver);

        if (staminaBlocked) {
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
        return getDamageValues(SCDamageCalculator.DamageType.BLUDGEONING.name(), item) > 0;
    }

    @Unique
    private void toggleBludgeoningMode(ItemStack stack) {
        stack.getOrCreateNbt().putBoolean(NBT_BLUDGEONING_KEY,
                !stack.getOrCreateNbt().getBoolean(NBT_BLUDGEONING_KEY));
    }

    @Unique
    private void handleRangeWeaponUse(World world, PlayerEntity user, Hand hand, ItemStack stack,
                                      CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (world.isClient) return;

        if (SCRangeWeaponUtil.getAmmoRequirement(stack.getItem()) != null) {
            handleAmmoBasedWeapon(world, user, hand, stack, cir);
        } else {
            handleProjectileWeapon(hand, user, stack, cir);
        }
    }

    @Unique
    private void handleAmmoBasedWeapon(World world, PlayerEntity user, Hand hand, ItemStack stack,
                                       CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        SCRangeWeaponUtil.WeaponState weaponState = SCRangeWeaponUtil.getWeaponState(stack);

        if (!weaponState.isCharged()) {
            cir.setReturnValue(TypedActionResult.fail(stack));
            return;
        }

        ItemStack offHandStack = user.getOffHandStack();
        if (SCRangedWeaponDefinitionsLoader.getData(stack.getItem()).needsFlintAndSteel() && offHandStack.getItem() != Items.FLINT_AND_STEEL && !user.isCreative()) {
            cir.setReturnValue(TypedActionResult.fail(stack));
            return;
        }

        user.setCurrentHand(hand);
        SCRangeWeaponUtil.shootBullet(world, stack, user);
        SCRangeWeaponUtil.setWeaponState(stack, new SCRangeWeaponUtil.WeaponState(
                weaponState.isReloading(), false, true));

        if (!user.getAbilities().creativeMode && SCRangedWeaponDefinitionsLoader.getData(stack.getItem()).needsFlintAndSteel() && user instanceof ServerPlayerEntity serverPlayer) {
            offHandStack.damage(1, serverPlayer, p -> p.sendToolBreakStatus(hand));
        }

        cir.setReturnValue(TypedActionResult.consume(stack));
    }

    @Unique
    private void handleProjectileWeapon(Hand hand, PlayerEntity user, ItemStack stack,
                                        CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        boolean hasAmmo = SCRangeWeaponUtil.getArrowFromInventory(user).isPresent();

        if (!hasAmmo) {
            cir.setReturnValue(TypedActionResult.fail(stack));
            return;
        }

        user.setCurrentHand(hand);
        if (SCRangedWeaponDefinitionsLoader.getData(stack.getItem()).useAction() == UseAction.CROSSBOW) {
            cir.setReturnValue(SCRangeWeaponUtil.handleCrossbowUse(user.getWorld(), user, hand, stack));
        } else {
            cir.setReturnValue(TypedActionResult.consume(stack));
        }
    }

    @Inject(method = "onStoppedUsing", at = @At("HEAD"))
    public void stoneycore$onStoppedUsing(ItemStack stack, World world, LivingEntity user,
                                               int remainingUseTicks, CallbackInfo ci) {
        if (world.isClient
                || !(user instanceof PlayerEntity player)
                || !(SCRangedWeaponDefinitionsLoader.containsItem(stack.getItem()))) {
            return;
        }

        int useTime = SCRangedWeaponDefinitionsLoader.getData(stack.getItem()).maxUseTime() - remainingUseTicks;
        SCRangeWeaponUtil.getArrowFromInventory(player).ifPresent(arrowStack ->
                handleWeaponRelease(stack, world, player, useTime, arrowStack)
        );
    }

    @Unique
    private void handleWeaponRelease(ItemStack stack, World world, PlayerEntity player,
                                     int useTime, ItemStack arrowStack) {
        if (SCRangedWeaponDefinitionsLoader.getData(stack.getItem()).useAction() == UseAction.BOW) {
            handleBowRelease(world, stack, player, arrowStack, useTime);
        } else if (SCRangedWeaponDefinitionsLoader.getData(stack.getItem()).useAction() == UseAction.CROSSBOW) {
            handleCrossbowRelease(stack, useTime);
        }
    }

    @Unique
    private void handleBowRelease(World world, ItemStack stack,
                                  PlayerEntity player, ItemStack arrowStack, int useTime) {
        float pullProgress = SCRangeWeaponUtil.getBowPullProgress(useTime);
        if (pullProgress > 0.1f) {
            SCRangeWeaponUtil.shootArrow(world, stack, player, arrowStack, pullProgress);
        }
    }

    @Unique
    private void handleCrossbowRelease(ItemStack stack, int useTime) {
        float pullProgress = SCRangeWeaponUtil.getCrossbowPullProgress(useTime, stack.getItem());
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
        Item item = stack.getItem();
        if (SCMeleeWeaponDefinitionsLoader.containsItem(stack.getItem())) {
            float slashingDamage = getDamageValues(SCDamageCalculator.DamageType.SLASHING.name(), item);
            float piercingDamage = getDamageValues(SCDamageCalculator.DamageType.PIERCING.name(), item);
            float bludgeoningDamage = getDamageValues(SCDamageCalculator.DamageType.BLUDGEONING.name(), item);

            if (slashingDamage > 0 && bludgeoningDamage > 0) {
                tooltip.add(Text.translatable("text.tooltip.stoneycore.shift-right_click-bludgeoning"));
            }

            if (slashingDamage == 0 && bludgeoningDamage > 0 && piercingDamage > 0) {
                tooltip.add(Text.translatable("text.tooltip.stoneycore.shift-right_click-bludgeoning-piercing"));
            }

            if (stack.isIn(SCTags.WEAPONS_HARVEST.getTag())) {
                tooltip.add(Text.translatable("text.tooltip.stoneycore.right_click-replant"));
            }

            if (slashingDamage != 0) tooltip.add(Text.translatable("text.tooltip.stoneycore.slashingDamage", (int) slashingDamage).formatted(Formatting.GREEN));
            if (bludgeoningDamage != 0) tooltip.add(Text.translatable("text.tooltip.stoneycore.bludgeoningDamage", (int) bludgeoningDamage).formatted(Formatting.GREEN));
            if (piercingDamage != 0) tooltip.add(Text.translatable("text.tooltip.stoneycore.piercingDamage", (int) piercingDamage).formatted(Formatting.GREEN));
        }

        if (SCRangedWeaponDefinitionsLoader.containsItem(item) && SCRangeWeaponUtil.getAmmoRequirement(stack.getItem()) != null && world.isClient()) {
            TooltipClientSide.setTooltip(tooltip);
        }
    }

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    public void stoneycore$useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = context.getStack();
        if (!(SCMeleeWeaponDefinitionsLoader.containsItem(stack.getItem())) || !stack.isIn(SCTags.WEAPONS_HARVEST.getTag())) {
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

        if (!bannerStack.isEmpty() && stack.getItem() instanceof SCTrinketsItem && stack.isIn(SCTags.BANNER_COMPATIBLE.getTag())) {
            List<Pair<Identifier, DyeColor>> bannerPatterns = getBannerPatterns(bannerStack, stack.getItem());
            PatternHelper.setBannerPatterns(stack, bannerPatterns);
            PatternHelper.setBannerDyeColor(stack, ((BannerItem) bannerStack.getItem()).getColor());
        }
    }

    @Unique
    private List<Pair<Identifier, DyeColor>> getBannerPatterns(ItemStack bannerStack, Item armor) {
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
                        StoneyCore.LOGGER.info("Set Banner Pattern: {} Color: {}", patternId, color);
                    }
                }
            }
        }
        return patterns;
    }
}