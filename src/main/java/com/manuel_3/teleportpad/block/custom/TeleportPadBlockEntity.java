package com.manuel_3.teleportpad.block.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;

public class TeleportPadBlockEntity extends BlockEntity {

    private int linkedX;
    private int linkedY;
    private int linkedZ;
    private RegistryKey<World> dimension;
    private boolean hasPos = false;

    public void setLinkedPos(BlockPos pos, World dimension) {
        this.linkedX = pos.getX();
        this.linkedY = pos.getY();
        this.linkedZ = pos.getZ();
        this.dimension = dimension.getRegistryKey();
        hasPos = true;
    }

    public BlockPos getLinkedPos() {
        if (!hasPos) return null;
        return new BlockPos(linkedX, linkedY, linkedZ);
    }

    public World getLinkedDimension(MinecraftServer server) {
        return server.getWorld(dimension);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        linkedX = nbt.getInt("linkedX");
        linkedY = nbt.getInt("linkedY");
        linkedZ = nbt.getInt("linkedZ");
        String dim = nbt.getString("dimension");
        if (!Objects.equals(dim, "")) {
            dimension = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(dim));
        }
        else {
            dimension = null;
        }
        hasPos = nbt.getBoolean("hasPos");
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt("linkedX", linkedX);
        nbt.putInt("linkedY", linkedY);
        nbt.putInt("linkedZ", linkedZ);
        if (dimension != null) {
            nbt.putString("dimension", dimension.getValue().toString());
        }
        else {
            nbt.putString("dimension", "");
        }
        nbt.putBoolean("hasPos", hasPos);
    }

    public TeleportPadBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypes.TELEPORT_PAD, pos, state);
    }
}
