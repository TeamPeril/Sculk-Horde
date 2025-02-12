package com.github.sculkhorde.common.screen;

import com.github.sculkhorde.core.SculkHorde;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SoulHarvesterScreen extends AbstractContainerScreen<SoulHarvesterMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(SculkHorde.MOD_ID, "textures/gui/soul_harvester_gui.png");

    public SoulHarvesterScreen(SoulHarvesterMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    @Override
    protected void renderBg(PoseStack guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        blit(guiGraphics, x, y, 0, 0, imageWidth, imageHeight);

        renderProgressArrow(guiGraphics, x, y);
        renderSoulsBar(guiGraphics, x, y);
    }

    private void renderProgressArrow(PoseStack guiGraphics, int x, int y) {
        if(menu.isCrafting()) {
            int arrowSourceX = 176; // The X Coordinate of the arrow on the texture
            int arrowSourceY = 0; // The Y Coordinate of the arrow on the texture
            int arrowSourceWidth = 8; // The Width of the arrow on the texture
            int arrowSourceHeight = menu.getScaledSoulProgress(); // The Height of the arrow on the texture (We change this to make it look like an animated texture)

            blit(guiGraphics, x + 85, y + 30, 176, 0, 8, arrowSourceHeight);
        }
    }

    private void renderSoulsBar(PoseStack guiGraphics, int x, int y) {
        int sourceX = 184; // The X Coordinate of the arrow on the texture
        int sourceY = 0; // The Y Coordinate of the arrow on the texture
        int sourceWidth = 20; // The Width of the arrow on the texture
        int sourceHeight = menu.getScaledSoulProgress(); // The Height of the arrow on the texture (We change this to make it look like an animated texture)

        blit(guiGraphics, x + 104, y + 27, sourceX, sourceY, sourceWidth, sourceHeight);
    }

    @Override
    public void render(PoseStack guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
