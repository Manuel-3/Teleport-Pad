package com.manuel_3.teleportpad.item.custom;

import com.manuel_3.teleportpad.block.custom.TeleportPadBlock;
import com.manuel_3.teleportpad.block.custom.TeleportPadBlockEntity;
import com.manuel_3.teleportpad.component.ModDataComponentTypes;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class TeleportPadConfigurator extends Item {

    public TeleportPadConfigurator(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        Block block = world.getBlockState(context.getBlockPos()).getBlock();
        if (block instanceof TeleportPadBlock) {
            BlockEntity blockEntity = world.getBlockEntity(context.getBlockPos());
            if (blockEntity instanceof TeleportPadBlockEntity) {
                if (context.getPlayer() == null) {
                    return ActionResult.FAIL;
                }
                BlockPos selectedLinkPos = context.getStack().get(ModDataComponentTypes.BLOCK_POS_COMPONENT_TYPE);
                if (context.getPlayer().isSneaking()) {
                    BlockPos pos = context.getBlockPos();
                    RegistryKey<World> worldKey = world.getRegistryKey();
                    context.getStack().set(ModDataComponentTypes.BLOCK_POS_COMPONENT_TYPE, pos);
                    context.getStack().set(ModDataComponentTypes.WORLD_COMPONENT_TYPE, worldKey);
                    if (!world.isClient()) {
                        Text message = Text.translatable("chat.teleport-pad.storing").formatted(Formatting.GRAY)
                                .append(Text.literal(" " + pos.getX() + " " + pos.getY() + " " + pos.getZ()).formatted(Formatting.YELLOW))
                                .append(Text.literal(" " + worldKey.getValue().toString()).formatted(Formatting.YELLOW));
                        context.getPlayer().sendMessage(message, false);
                    }
                    world.playSound(context.getPlayer(), context.getBlockPos(), SoundEvents.BLOCK_COPPER_BULB_TURN_OFF, SoundCategory.BLOCKS, 1f, 1f);
                    return ActionResult.SUCCESS;
                }
                else if (selectedLinkPos != null) {
                    if (!world.isClient()) {
                        RegistryKey<World> selectedLinkWorldKey = context.getStack().get(ModDataComponentTypes.WORLD_COMPONENT_TYPE);
                        if (selectedLinkWorldKey == null) return ActionResult.FAIL;
                        MinecraftServer server =  context.getWorld().getServer();
                        if (server == null) return ActionResult.FAIL;
                        World selectedLinkWorld = server.getWorld(selectedLinkWorldKey);
                        if (selectedLinkWorld == null) return ActionResult.FAIL;
                        BlockEntity blockEntity1 = world.getBlockEntity(context.getBlockPos());
                        if (blockEntity1 instanceof TeleportPadBlockEntity teleportPadBlockEntity1) {
                            teleportPadBlockEntity1.setLinkedPos(selectedLinkPos, selectedLinkWorld);
                            teleportPadBlockEntity1.markDirty();
                            Text message = Text.translatable("chat.teleport-pad.setting").formatted(Formatting.GRAY)
                                    .append(Text.literal(" " + selectedLinkPos.getX() + " " + selectedLinkPos.getY() + " " + selectedLinkPos.getZ()).formatted(Formatting.BLUE))
                                    .append(Text.literal(" " + selectedLinkWorld.getRegistryKey().getValue().toString()).formatted(Formatting.BLUE));
                            context.getPlayer().sendMessage(message, false);
                        }
                    }
                    world.playSound(context.getPlayer(), context.getBlockPos(), SoundEvents.BLOCK_COPPER_BULB_TURN_ON, SoundCategory.BLOCKS, 1f, 1f);
                    return ActionResult.SUCCESS;
                }
            }
        }

        return super.useOnBlock(context);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        BlockPos pos = stack.get(ModDataComponentTypes.BLOCK_POS_COMPONENT_TYPE);
        RegistryKey<World> worldKey = stack.get(ModDataComponentTypes.WORLD_COMPONENT_TYPE);
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.translatable("tooltip.teleport-pad.teleport_pad_configurator_save").formatted(Formatting.GRAY));
            tooltip.add(Text.translatable("tooltip.teleport-pad.teleport_pad_configurator_set").formatted(Formatting.GRAY));
        }
        else {
            tooltip.add(Text.translatable("tooltip.teleport-pad.collapsed").formatted(Formatting.GRAY));
        }
        if (pos != null) {
            tooltip.add(Text.literal(pos.getX() + " " + pos.getY() + " " + pos.getZ()).formatted(Formatting.YELLOW));
        }
        if (worldKey != null) {
            tooltip.add(Text.literal(worldKey.getValue().toString()).formatted(Formatting.YELLOW));
        }

        super.appendTooltip(stack, context, tooltip, type);
    }
}
