package com.manuel_3.teleportpad.block.custom;

import com.manuel_3.teleportpad.TeleportPad;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TeleportPadBlock extends CarpetBlock implements BlockEntityProvider {

    private static final Map<PlayerEntity, Boolean> sneakStates = new WeakHashMap<>();
    private final Map<Integer, PlayerEntity> pendingTransports = new HashMap<>();
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
                pendingTransports.put(pos.hashCode(), player);
                world.scheduleBlockTick(pos, state.getBlock(), 1); // delay teleport by 1 tick to fix weird desync issue that happens when crouching soon after stepping on
            }
            sneakStates.put(player, player.isSneaking());
        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, net.minecraft.util.math.random.Random random) {
        PlayerEntity player = pendingTransports.remove(pos.hashCode());
        if (player != null && player.isAlive()) {
            transport(world, pos, player);
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
                    to.setLinkedPos(pos, world);
                    to.markDirty();
                }
                world.playSound(null, pos, SoundEvents.ENTITY_PLAYER_TELEPORT, SoundCategory.BLOCKS, 0.35f, 1.7f);
                //world.sendEntityStatus(entity, EntityStatuses.ADD_PORTAL_PARTICLES);
                if (entity instanceof ServerPlayerEntity player) {
                    if (player.getWorld().getRegistryKey().equals(targetWorld.getRegistryKey())) {
                        player.networkHandler.requestTeleport(target.getX()+(player.getX()-pos.getX()), target.getY()+(player.getY()-pos.getY()), target.getZ()+(player.getZ()-pos.getZ()), player.getYaw(), player.getPitch());
                    }
                    else {
                        TeleportTarget teleportTarget = new TeleportTarget((ServerWorld) targetWorld, new Vec3d(target.getX()+(player.getX()-pos.getX()), target.getY()+(player.getY()-pos.getY()), target.getZ()+(player.getZ()-pos.getZ())), player.getVelocity(), player.getYaw(), player.getPitch(), TeleportTarget.NO_OP);
                        player.teleportTo(teleportTarget);
                    }
                }
                else {
                    entity.teleport((ServerWorld) targetWorld, target.getX()+(entity.getX()-pos.getX()), target.getY()+(entity.getY()-pos.getY()), target.getZ()+(entity.getZ()-pos.getZ()), EnumSet.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z), entity.getYaw(), entity.getPitch());
                }
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
