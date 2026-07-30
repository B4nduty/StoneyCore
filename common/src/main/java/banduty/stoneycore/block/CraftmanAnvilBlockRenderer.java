package banduty.stoneycore.block;

import banduty.stoneycore.recipes.CraftmanAnvilRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Optional;
import java.util.Random;

public class CraftmanAnvilBlockRenderer implements BlockEntityRenderer<CraftmanAnvilBlockEntity> {

    private static final RenderType SLOT_HIGHLIGHT = new RenderType(
            "stoneycore_slot_highlight",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false, // affectsCrumbling
            true,  // sortOnUpload - translucent geometry, so sort it
            () -> {
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableCull();
            },
            () -> {
                RenderSystem.disableBlend();
                RenderSystem.enableCull();
            }
    ) {};

    public CraftmanAnvilBlockRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(CraftmanAnvilBlockEntity entity, float tickDelta, PoseStack poseStack,
                       MultiBufferSource vertexConsumers, int light, int overlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        NonNullList<ItemStack> itemStacks = entity.getItems();

        BlockState blockState = entity.getBlockState();
        Direction facing = blockState.getValue(CraftmanAnvilBlock.FACING);

        ItemStack outputStack = itemStacks.getFirst();
        boolean outputPending = !outputStack.isEmpty();

        if (outputPending) {
            renderItemInSlot(entity, outputStack, 0, facing, poseStack, vertexConsumers, itemRenderer);
        } else {
            for (int i = 1; i < itemStacks.size(); i++) {
                ItemStack stack = itemStacks.get(i);
                if (!stack.isEmpty()) {
                    renderItemInSlot(entity, stack, i, facing, poseStack, vertexConsumers, itemRenderer);
                }
            }

            Optional<RecipeHolder<CraftmanAnvilRecipe>> recipe = entity.getRecipe();
            recipe.ifPresent(anvilRecipe -> renderHitSquares(entity, poseStack, vertexConsumers, recipe.get().value().hitTimes(), facing));

            if (isPlayerLookingAt(entity)) {
                renderEmptySlotIndicators(entity, itemStacks, facing, poseStack, vertexConsumers);
            }
        }
    }

    private boolean isPlayerLookingAt(CraftmanAnvilBlockEntity entity) {
        HitResult hitResult = Minecraft.getInstance().hitResult;
        return hitResult instanceof BlockHitResult blockHitResult
                && blockHitResult.getType() == HitResult.Type.BLOCK
                && blockHitResult.getBlockPos().equals(entity.getBlockPos());
    }

    private static final float EMPTY_SLOT_BOX_COLOR_R = 0.25f;
    private static final float EMPTY_SLOT_BOX_COLOR_G = 0.45f;
    private static final float EMPTY_SLOT_BOX_COLOR_B = 1.0f;
    private static final float EMPTY_SLOT_BOX_ALPHA = 0.35f;

    private void renderEmptySlotIndicators(CraftmanAnvilBlockEntity entity, NonNullList<ItemStack> itemStacks,
                                           Direction facing, PoseStack poseStack, MultiBufferSource vertexConsumers) {
        VertexConsumer bufferBuilder = vertexConsumers.getBuffer(SLOT_HIGHLIGHT);

        for (int slotIndex = 1; slotIndex < itemStacks.size(); slotIndex++) {
            if (!itemStacks.get(slotIndex).isEmpty()) continue;
            renderEmptySlotBox(bufferBuilder, slotIndex, facing, poseStack);
        }
    }

    private void renderEmptySlotBox(VertexConsumer bufferBuilder, int slotIndex, Direction facing, PoseStack poseStack) {
        float spacingX = 0.3f;
        float spacingZ = 0.24f;

        int gridIndex = slotIndex - 1;
        int row = gridIndex / CraftmanAnvilRecipe.GRID_WIDTH;
        int col = gridIndex % CraftmanAnvilRecipe.GRID_WIDTH;

        float xOffset = (col - (CraftmanAnvilRecipe.GRID_WIDTH - 1) / 2.0f) * spacingX;
        float zOffset = (row - (CraftmanAnvilRecipe.GRID_HEIGHT - 1) / 2.0f) * spacingZ;

        poseStack.pushPose();

        // Base position on the anvil surface.
        poseStack.translate(0.5f, 0.63f, 0.5f);

        switch (facing) {
            case NORTH -> poseStack.translate(-0.1f, 0f, 0f);
            case SOUTH -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.translate(-0.1f, 0f, 0f);
            }
            case WEST -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
                poseStack.translate(-0.1f, 0f, 0f);
            }
            case EAST -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(270));
                poseStack.translate(-0.1f, 0f, 0f);
            }
        }

        poseStack.translate(xOffset, 0f, zOffset);

        float halfWidth = 0.06f;
        float height = 0.05f;
        float halfDepth = 0.06f;

        renderBox(bufferBuilder, poseStack.last().pose(), poseStack.last().normal(),
                -halfWidth, 0f, -halfDepth, halfWidth, height, halfDepth,
                EMPTY_SLOT_BOX_COLOR_R, EMPTY_SLOT_BOX_COLOR_G, EMPTY_SLOT_BOX_COLOR_B, EMPTY_SLOT_BOX_ALPHA);

        poseStack.popPose();
    }

    private void renderBox(VertexConsumer bufferBuilder, Matrix4f matrix, Matrix3f normalMatrix,
                           float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                           float r, float g, float b, float a) {
        // -X face
        addQuad(bufferBuilder, matrix,
                minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, minX, minY, minZ, r, g, b, a);
        // +X face
        addQuad(bufferBuilder, matrix,
                maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, r, g, b, a);
        // -Y face (bottom)
        addQuad(bufferBuilder, matrix,
                maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);
        // +Y face (top)
        addQuad(bufferBuilder, matrix,
                maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY, minZ, minX, maxY, maxZ, r, g, b, a);
        // -Z face
        addQuad(bufferBuilder, matrix,
                minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, r, g, b, a);
        // +Z face
        addQuad(bufferBuilder, matrix,
                maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, minX, minY, maxZ, r, g, b, a);
    }

    private void addQuad(VertexConsumer bufferBuilder, Matrix4f matrix,
                         float x1, float y1, float z1,
                         float x2, float y2, float z2,
                         float x3, float y3, float z3,
                         float x4, float y4, float z4,
                         float r, float g, float b, float a) {
        bufferBuilder.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
        bufferBuilder.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a);
        bufferBuilder.addVertex(matrix, x3, y3, z3).setColor(r, g, b, a);
        bufferBuilder.addVertex(matrix, x4, y4, z4).setColor(r, g, b, a);
    }

    private void renderItemInSlot(CraftmanAnvilBlockEntity entity, ItemStack itemStack, int slotIndex,
                                  Direction facing, PoseStack poseStack,
                                  MultiBufferSource vertexConsumers, ItemRenderer itemRenderer) {
        float spacingX = 0.3f;
        float spacingZ = 0.24f;

        float xOffset;
        float zOffset;

        if (slotIndex == 0) {
            // Output slot - sits in the middle of the ingredient grid.
            xOffset = 0f;
            zOffset = 0f;
        } else {
            int gridIndex = slotIndex - 1;
            int row = gridIndex / CraftmanAnvilRecipe.GRID_WIDTH;
            int col = gridIndex % CraftmanAnvilRecipe.GRID_WIDTH;

            xOffset = (col - (CraftmanAnvilRecipe.GRID_WIDTH - 1) / 2.0f) * spacingX;
            zOffset = (row - (CraftmanAnvilRecipe.GRID_HEIGHT - 1) / 2.0f) * spacingZ;
        }

        poseStack.pushPose();

        // Base position on the anvil
        poseStack.translate(0.5f, 0.66f, 0.5f);

        // Apply facing rotation
        switch (facing) {
            case NORTH -> poseStack.translate(-0.1f, 0f, 0f);
            case SOUTH -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.translate(-0.1f, 0f, 0f);
            }
            case WEST -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
                poseStack.translate(-0.1f, 0f, 0f);
            }
            case EAST -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(270));
                poseStack.translate(-0.1f, 0f, 0f);
            }
        }

        poseStack.translate(xOffset, 0f, zOffset);

        // Random rotation for variety
        long seed = (long) BuiltInRegistries.ITEM.getKey(itemStack.getItem()).hashCode() + slotIndex * 37L;
        Random rand = new Random(seed);

        float offsetX = -0.06f + rand.nextFloat() * 0.12f;
        float offsetZ = -0.02f + rand.nextFloat() * 0.04f;
        float rotY = -50f + rand.nextFloat() * 100f;

        poseStack.translate(offsetX, 0f, offsetZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotY));

        // Same scale for all items
        poseStack.scale(0.25f, 0.25f, 0.25f);

        poseStack.mulPose(Axis.XP.rotationDegrees(270));

        itemRenderer.renderStatic(itemStack, ItemDisplayContext.GUI, getLightLevel(entity.getLevel(),
                entity.getBlockPos()), OverlayTexture.NO_OVERLAY, poseStack, vertexConsumers, entity.getLevel(), 1);
        poseStack.popPose();
    }

    private void renderHitSquares(CraftmanAnvilBlockEntity entity, PoseStack poseStack, MultiBufferSource vertexConsumers, int totalHits, Direction facing) {
        int hitsDone = entity.getHitCount();
        if (totalHits <= 0) return;

        poseStack.pushPose();

        poseStack.translate(0.55f, 0.655f, 0.3f);

        switch (facing) {
            case NORTH -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.translate(0.1f, 0.0f, -0.4f);
            }
            case WEST -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(270));
                poseStack.translate(0.25f, 0.0f, -0.15f);
            }
            case EAST -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
                poseStack.translate(-0.15f, 0.0f, -0.25f);
            }
        }

        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.scale(-0.005f, -0.005f, 0.005f);

        float squareSize = 8f;
        float spacing = 4f;

        int squaresPerRow = 10;
        int rows = (int) Math.ceil(totalHits / (float) squaresPerRow);
        int squaresInLastRow = totalHits % squaresPerRow;
        if (squaresInLastRow == 0) squaresInLastRow = squaresPerRow;

        VertexConsumer bufferBuilder = vertexConsumers.getBuffer(RenderType.gui());

        for (int row = 0; row < rows; row++) {
            int squaresInThisRow = (row == rows - 1) ? squaresInLastRow : squaresPerRow;

            // Calculate starting X position to center the row
            float totalWidth = (squaresInThisRow * squareSize) + ((squaresInThisRow - 1) * spacing);
            float startX = -totalWidth / 2f;

            // Y position: higher rows (lower index) are higher on screen
            float yPos = row * (squareSize + spacing);

            for (int col = 0; col < squaresInThisRow; col++) {
                float xPos = startX + col * (squareSize + spacing);

                // Calculate which absolute hit index this represents
                int absoluteIndex = row * squaresPerRow + col;

                float r, g, b, a = 1.0f;

                // Determine if this square should be visible (not yet hit)
                boolean isVisible = absoluteIndex >= hitsDone;

                if (isVisible) {
                    // White squares that haven't been hit yet
                    r = g = b = 1.0f;
                } else {
                    // Transparent/removed squares that have been hit
                    a = 0.0f;
                    r = g = b = 0.0f;
                }

                renderSingleSquare(bufferBuilder, poseStack.last().pose(), poseStack.last().normal(),
                        xPos, yPos, squareSize, r, g, b, a);
            }
        }

        poseStack.popPose();
    }

    private void renderSingleSquare(VertexConsumer bufferBuilder, Matrix4f matrix, Matrix3f normalMatrix,
                                    float x, float y, float squareSize, float r, float g, float b, float a) {
        bufferBuilder.addVertex(matrix, x, y, 0)
                .setColor(r, g, b, a)
                .setUv(0, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0, 0, 1);

        bufferBuilder.addVertex(matrix, x, y + squareSize, 0)
                .setColor(r, g, b, a)
                .setUv(0, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0, 0, 1);

        bufferBuilder.addVertex(matrix, x + squareSize, y + squareSize, 0)
                .setColor(r, g, b, a)
                .setUv(1, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0, 0, 1);

        bufferBuilder.addVertex(matrix, x + squareSize, y, 0)
                .setColor(r, g, b, a)
                .setUv(1, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0, 0, 1);
    }

    private int getLightLevel(Level level, BlockPos pos) {
        if (level == null) {
            return LightTexture.pack(15, 15);
        }
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}