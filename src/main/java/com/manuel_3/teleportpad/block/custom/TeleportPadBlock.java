package com.manuel_3.teleportpad.block.custom;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TeleportPadBlock extends CarpetBlock implements BlockEntityProvider {

    private static final Map<PlayerEntity, Boolean> sneakStates = new WeakHashMap<>();
    private static final BooleanProperty POWERED = BooleanProperty.of("powered");

    public TeleportPadBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(POWERED, false));
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.isClient()) return;
        if (entity instanceof PlayerEntity player) {
            if (!sneakStates.containsKey(player)) {
                sneakStates.put(player, player.isSneaking());
            }
            if (player.isSneaking() && !sneakStates.get(player)) {
                transport(world, pos, player);
            }
            sneakStates.put(player, player.isSneaking());
        }
    }

    protected void transport(World world, BlockPos pos, Entity entity) {
        if (world.isClient()) return;
        MinecraftServer server = world.getServer();
        if (server == null) return;
        BlockEntity from0 = world.getBlockEntity(pos);
        if (from0 instanceof TeleportPadBlockEntity from) {
            BlockPos target = from.getLinkedPos();
            World targetWorld = from.getLinkedDimension(server);
            if (target == null) {
                world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1f, 1.5f);
                return;
            }
            BlockEntity to0 = targetWorld.getBlockEntity(target);
            if (to0 instanceof TeleportPadBlockEntity to) {
                if (to.getLinkedPos() == null) {
                    to.setLinkedPos(pos, targetWorld);
                    to.markDirty();
                }
                world.playSound(null, pos, SoundEvents.ENTITY_PLAYER_TELEPORT, SoundCategory.BLOCKS, 0.35f, 1.7f);
                //world.sendEntityStatus(entity, EntityStatuses.ADD_PORTAL_PARTICLES);
                entity.teleport((ServerWorld) targetWorld, target.getX()+(entity.getX()-pos.getX()), target.getY()+(entity.getY()-pos.getY()), target.getZ()+(entity.getZ()-pos.getZ()), EnumSet.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z), entity.getYaw(), entity.getPitch());
                targetWorld.playSound(null, target, SoundEvents.ENTITY_PLAYER_TELEPORT, SoundCategory.BLOCKS, 0.35f, 1.7f);
            }
            else {
                world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1f, 1.5f);
            }
        }
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (world.isClient()) return;
        boolean powered = world.isReceivingRedstonePower(pos);
        boolean wasPowered = state.get(TeleportPadBlock.POWERED);
        if (powered != wasPowered) {
            if (powered && !wasPowered) {
                Box box = new Box(pos);
                List<Entity> entities = world.getEntitiesByClass(Entity.class, box, entity -> true);

                for (Entity entity : entities) {
                    transport(world, pos, entity);
                }
            }
            world.setBlockState(pos, state.with(TeleportPadBlock.POWERED, powered));
        }
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TeleportPadBlockEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(TeleportPadBlock.POWERED);
        super.appendProperties(builder);
    }
}
