package com.lunazstudios.farmies.screen.renderer;

import com.lunazstudios.farmies.Farmies;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.List;

public class EnergyDisplayTooltipArea {
    private final int xPos;
    private final int yPos;
    private final int width;
    private final int height;
    private final IEnergyStorage energy;
    private static final ResourceLocation BATTERY_FULL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Farmies.MOD_ID,"textures/gui/battery_full.png");

    public EnergyDisplayTooltipArea(int xMin, int yMin, IEnergyStorage energy)  {
        this(xMin, yMin, energy,8,64);
    }

    public EnergyDisplayTooltipArea(int xMin, int yMin, IEnergyStorage energy, int width, int height)  {
        xPos = xMin;
        yPos = yMin;
        this.width = width;
        this.height = height;
        this.energy = energy;
    }

    public List<Component> getTooltips() {
        return List.of(Component.literal(energy.getEnergyStored()+" / "+energy.getMaxEnergyStored()+" FE"));
    }

    public void render(GuiGraphics guiGraphics) {
        int energyStored = energy.getEnergyStored();
        int maxEnergy = energy.getMaxEnergyStored();
        if (maxEnergy <= 0) return;

        int storedHeight = (int) (height * (energyStored / (float) maxEnergy));

        int yStart = yPos + (height - storedHeight);

        int vOffset = height - storedHeight;

        guiGraphics.blit(
                BATTERY_FULL_TEXTURE,
                xPos, yStart,
                0, vOffset,
                width, storedHeight,
                width, height
        );
    }
}