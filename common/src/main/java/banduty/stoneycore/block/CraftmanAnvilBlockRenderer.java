package banduty.stoneycore.block;

import banduty.stoneycore.recipes.CraftmanAnvilRecipe;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
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
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class CraftmanAnvilBlockRenderer implements BlockEntityRenderer<CraftmanAnvilBlockEntity> {

    public CraftmanAnvilBlockRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(CraftmanAnvilBlockEntity entity, float tickDelta, PoseStack poseStack,
                       MultiBufferSource vertexConsumers, int light, int overlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        NonNullList<ItemStack> itemStacks = entity.getItems();

        BlockState blockState = entity.getBlockState();
        Direction facing = blockState.getValue(CraftmanAnvilBlock.FACING);

        // Create a combined list with both input and output slots
        List<ItemStack> allItems = new ArrayList<>();
        List<Integer> allSlotIndices = new ArrayList<>();

        // Add output slot (index 0) - treat it as part of the grid
        ItemStack outputStack = itemStacks.getFirst();
        if (!outputStack.isEmpty()) {
            allItems.add(outputStack);
            allSlotIndices.add(0);
        }

        // Add input slots (indices 1-6)
        for (int i = 1; i < itemStacks.size(); i++) {
            ItemStack stack = itemStacks.get(i);
            if (!stack.isEmpty()) {
                allItems.add(stack);
                allSlotIndices.add(i);
            }
        }

        // Render all items using the same rendering logic
        renderItems(entity, allItems, allSlotIndices, facing, poseStack, vertexConsumers, itemRenderer);

        Optional<RecipeHolder<CraftmanAnvilRecipe>> recipe = entity.getRecipe();
        recipe.ifPresent(anvilRecipe -> renderHitSquares(entity, poseStack, vertexConsumers, recipe.get().value().hitTimes(), facing));
    }

    private void renderItems(CraftmanAnvilBlockEntity entity, List<ItemStack> itemsToRender,
                             List<Integer> slotIndices, Direction facing, PoseStack poseStack,
                             MultiBufferSource vertexConsumers, ItemRenderer itemRenderer) {
        // If no items to render, return early
        if (itemsToRender.isEmpty()) return;

        int itemCount = itemsToRender.size();

        // Fixed 2 rows, 3 columns max
        int maxCols = 3;
        int maxRows = 2;

        float spacingX = 0.3f;
        float spacingZ = 0.24f;

        // Render each item
        for (int idx = 0; idx < itemCount; idx++) {
            ItemStack itemStack = itemsToRender.get(idx);
            int originalSlot = slotIndices.get(idx);

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

            // Calculate position in 2x3 grid
            int row = idx / maxCols;
            int col = idx % maxCols;

            // Get the actual number of items in this row
            int itemsInCurrentRow = Math.min(maxCols, itemCount - (row * maxCols));

            // Calculate the number of rows actually used
            int usedRows = (int) Math.ceil(itemCount / (double) maxCols);

            // Center the grid horizontally and vertically
            float xOffset = (col - (itemsInCurrentRow - 1) / 2.0f) * spacingX;
            float zOffset = (row - (usedRows - 1) / 2.0f) * spacingZ;

            poseStack.translate(xOffset, 0f, zOffset);

            // Random rotation for variety (but consistent per slot)
            long seed = (long) BuiltInRegistries.ITEM.getKey(itemStack.getItem()).hashCode() + originalSlot * 37L;
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

    private record SquareData(float x, float y, float r, float g, float b, float a, int index) {
    }

    private float[] hsvToRgb(float hue, float saturation, float value) {
        float[] rgb = new float[3];

        int h = (int) (hue * 6);
        float f = hue * 6 - h;
        float p = value * (1 - saturation);
        float q = value * (1 - f * saturation);
        float t = value * (1 - (1 - f) * saturation);

        switch (h % 6) {
            case 0 -> {
                rgb[0] = value;
                rgb[1] = t;
                rgb[2] = p;
            }
            case 1 -> {
                rgb[0] = q;
                rgb[1] = value;
                rgb[2] = p;
            }
            case 2 -> {
                rgb[0] = p;
                rgb[1] = value;
                rgb[2] = t;
            }
            case 3 -> {
                rgb[0] = p;
                rgb[1] = q;
                rgb[2] = value;
            }
            case 4 -> {
                rgb[0] = t;
                rgb[1] = p;
                rgb[2] = value;
            }
            case 5 -> {
                rgb[0] = value;
                rgb[1] = p;
                rgb[2] = q;
            }
        }

        return rgb;
    }

    private float clampColor(float value) {
        return Math.max(0, Math.min(1, value));
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