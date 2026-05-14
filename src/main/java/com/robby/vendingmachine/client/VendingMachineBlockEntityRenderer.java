package com.robby.vendingmachine.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.robby.vendingmachine.VendingMachineMod;
import com.robby.vendingmachine.block.VendingMachineBlock;
import com.robby.vendingmachine.blockentity.VendingMachineBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.joml.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class VendingMachineBlockEntityRenderer implements BlockEntityRenderer<VendingMachineBlockEntity> {
    private static final ResourceLocation SIGN_INGOTS =
        ResourceLocation.fromNamespaceAndPath(VendingMachineMod.MOD_ID, "textures/sign/vendor_sign_ingots.png");

    private static final ResourceLocation SIGN_FOOD =
            ResourceLocation.fromNamespaceAndPath(VendingMachineMod.MOD_ID, "textures/sign/vendor_sign_food.png");

    private static final ResourceLocation SIGN_TOOLS =
            ResourceLocation.fromNamespaceAndPath(VendingMachineMod.MOD_ID, "textures/sign/vendor_sign_tools.png");

    private static final ResourceLocation SIGN_ORES =
            ResourceLocation.fromNamespaceAndPath(VendingMachineMod.MOD_ID, "textures/sign/vendor_sign_ores.png");

    private static final ResourceLocation SIGN_BLOCKS =
            ResourceLocation.fromNamespaceAndPath(VendingMachineMod.MOD_ID, "textures/sign/vendor_sign_blocks.png");

    private static final ResourceLocation SIGN_MAGIC =
            ResourceLocation.fromNamespaceAndPath(VendingMachineMod.MOD_ID, "textures/sign/vendor_sign_magic.png");

    private static final ResourceLocation SIGN_GENERAL =
            ResourceLocation.fromNamespaceAndPath(VendingMachineMod.MOD_ID, "textures/sign/vendor_sign_general.png");

    private static final ResourceLocation[] SIGN_TEXTURES = {
            SIGN_INGOTS,
            SIGN_FOOD,
            SIGN_TOOLS,
            SIGN_ORES,
            SIGN_BLOCKS,
            SIGN_MAGIC,
            SIGN_GENERAL
    };

    public VendingMachineBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            VendingMachineBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        BlockState state = blockEntity.getBlockState();

        if (!(state.getBlock() instanceof VendingMachineBlock)) {
            return;
        }

        if (state.getValue(VendingMachineBlock.HALF) != DoubleBlockHalf.LOWER) {
            return;
        }

        ResourceLocation texture = getSignTexture(blockEntity.getSignPreset());

        poseStack.pushPose();

        rotateToBlockFacing(state, poseStack);

        renderSignQuad(poseStack, bufferSource, texture);
        renderSaleItems(blockEntity, poseStack, bufferSource);

        poseStack.popPose();
    }

        private void renderSaleItems(
            VendingMachineBlockEntity blockEntity,
            PoseStack poseStack,
            MultiBufferSource bufferSource
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        /*
        * Item display layout based on your Blockbench measurements.
        *
        * X range: 6 to 14 pixels
        * 3 columns inside that space.
        *
        * Shelf Y positions:
        * Top shelf    around 18
        * Middle shelf around 14
        * Bottom shelf around 10
        *
        * Items are rendered slightly behind the glass/front face.
        */
        float[] xPositions = {
                7.4F / 16.0F,
                10.0F / 16.0F,
                12.6F / 16.0F
        };

        float[] yPositions = {
                19.55F / 16.0F, // top shelf items
                15.55F / 16.0F, // middle shelf items
                11.55F / 16.0F  // bottom shelf items
        };

        float z = 1.15F / 16.0F;

        float scale = 0.16F;

        for (int saleIndex = 0; saleIndex < VendingMachineBlockEntity.SALE_SLOT_COUNT; saleIndex++) {
            ItemStack stack = blockEntity.getConfiguredSellStack(saleIndex);

            if (stack.isEmpty()) {
                continue;
            }

            int row = saleIndex / 3;

            // Flip columns so slot 1 appears on the left, not the right.
            int col = 2 - (saleIndex % 3);

            poseStack.pushPose();

            poseStack.translate(xPositions[col], yPositions[row], z);

            // Face the front of the item toward the vending machine glass.
            poseStack.mulPose(Axis.YP.rotationDegrees(0.0F));

            // Small product-sized render.
            poseStack.scale(scale, scale, scale);

            minecraft.getItemRenderer().renderStatic(
                    stack,
                    ItemDisplayContext.FIXED,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    bufferSource,
                    blockEntity.getLevel(),
                    saleIndex
            );

            poseStack.popPose();
        }
    }

    private ResourceLocation getSignTexture(int signPreset) {
        if (signPreset < 0 || signPreset >= SIGN_TEXTURES.length) {
            return SIGN_TEXTURES[0];
        }

        return SIGN_TEXTURES[signPreset];
    }

    private void rotateToBlockFacing(BlockState state, PoseStack poseStack) {
        float rotation = switch (state.getValue(VendingMachineBlock.FACING)) {
            case SOUTH -> 180.0F;
            case WEST -> 270.0F;
            case EAST -> 90.0F;
            default -> 0.0F;
        };

        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.translate(-0.5D, 0.0D, -0.5D);
    }

    private void renderSignQuad(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));
        Matrix4f pose = poseStack.last().pose();

        int light = LightTexture.FULL_BRIGHT;
        int overlay = OverlayTexture.NO_OVERLAY;

        float x1 = 2.0F / 16.0F;
        float x2 = 10.0F / 16.0F;

        float y1 = 24.0F / 16.0F;
        float y2 = 27.0F / 16.0F;

        // Front side of your current model.
        float z = 15.55F / 16.0F;

        addVertex(vertexConsumer, pose, x1, y2, z, 0.0F, 0.0F, light, overlay);
        addVertex(vertexConsumer, pose, x2, y2, z, 1.0F, 0.0F, light, overlay);
        addVertex(vertexConsumer, pose, x2, y1, z, 1.0F, 1.0F, light, overlay);
        addVertex(vertexConsumer, pose, x1, y1, z, 0.0F, 1.0F, light, overlay);
    }

    private void addVertex(
            VertexConsumer vertexConsumer,
            Matrix4f pose,
            float x,
            float y,
            float z,
            float u,
            float v,
            int light,
            int overlay
    ) {
        vertexConsumer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(0.0F, 0.0F, 1.0F);
    }
}