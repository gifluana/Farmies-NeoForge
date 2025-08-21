package com.lunazstudios.farmies.block.properties;

import net.minecraft.util.StringRepresentable;

public enum CabinetWood implements StringRepresentable {
    ACACIA("acacia"),
    BAMBOO("bamboo"),
    BIRCH("birch"),
    CHERRY("cherry"),
    CRIMSON("crimson"),
    DARK_OAK("dark_oak"),
    JUNGLE("jungle"),
    MANGROVE("mangrove"),
    OAK("oak"),
    PALE("pale"),
    WARPED("warped"),
    SPRUCE("spruce");

    private final String name;
    CabinetWood(String name) { this.name = name; }
    @Override public String getSerializedName() { return name; }
    @Override public String toString() { return name; }
}