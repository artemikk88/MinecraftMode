package com.hasturian.repairforge.client;

import com.hasturian.repairforge.screen.RepairForgeScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class RepairForgeScreen extends HandledScreen<RepairForgeScreenHandler> {

    // ЗАГЛУШКА: текстура ванильной печки. Своя должна лежать в
    // assets/repairforge/textures/gui/repair_forge.png (176x166 + индикаторы справа, как у furnace.png).
    private static final Identifier TEXTURE = new Identifier("minecraft", "textures/gui/container/furnace.png");

    public RepairForgeScreen(RepairForgeScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

        // Огонёк
        if (handler.isBurning()) {
            int h = handler.getFuelProgress();
            context.drawTexture(TEXTURE, x + 56, y + 36 + 12 - h, 176, 12 - h, 14, h + 1);
        }

        // Стрелка = процент восстановленной прочности
        int w = handler.getRepairProgress();
        context.drawTexture(TEXTURE, x + 79, y + 34, 176, 14, w + 1, 16);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
