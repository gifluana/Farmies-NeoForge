package com.lunazstudios.farmies.energy;

import net.neoforged.neoforge.energy.EnergyStorage;

public abstract class FEnergyStorage extends EnergyStorage {
    public FEnergyStorage(int capacity, int maxTransfer) {
        super(capacity, maxTransfer);
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        int extractedEnergy = super.extractEnergy(maxExtract, simulate);

        if (extractedEnergy != 0) {
            onEnergyChanged();
        }

        return extractedEnergy;
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        int receiveEnergy = super.receiveEnergy(maxReceive, simulate);

        if (receiveEnergy != 0) {
            onEnergyChanged();
        }

        return receiveEnergy;
    }

    public int setEnergy(int energy) {
        this.energy = energy;
        return energy;
    }

    public abstract void onEnergyChanged();
}
