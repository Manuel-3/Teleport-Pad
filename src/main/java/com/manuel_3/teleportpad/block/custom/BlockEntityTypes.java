package com.manuel_3.teleportpad.block.custom;

import com.manuel_3.teleportpad.block.ModBlocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BlockEntityTypes {
    public static <T extends BlockEntityType<?>> T register(String path, T blockEntityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of("teleport-pad", path), blockEntityType);
    }

    public static final BlockEntityType<TeleportPadBlockEntity> TELEPORT_PAD = register(
            "teleport_pad",
            BlockEntityType.Builder.create(TeleportPadBlockEntity::new,
                    ModBlocks.TELEPORT_PAD
            ).build()
    );

    public static void initialize() {
    }
}
