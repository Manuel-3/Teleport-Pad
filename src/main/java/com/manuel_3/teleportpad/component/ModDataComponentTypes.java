package com.manuel_3.teleportpad.component;

import com.manuel_3.teleportpad.TeleportPad;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.UnaryOperator;

public class ModDataComponentTypes {

    public static final ComponentType<BlockPos> BLOCK_POS_COMPONENT_TYPE = register("coordinates", builder -> builder.codec(BlockPos.CODEC));
    public static final ComponentType<RegistryKey<World>> WORLD_COMPONENT_TYPE = register("dimension", builder -> builder.codec(World.CODEC));

    private static <T>ComponentType<T> register(String name, UnaryOperator<ComponentType.Builder<T>> builderUnaryOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(TeleportPad.MOD_ID, name),
                builderUnaryOperator.apply(ComponentType.builder()).build());
    }

    public static void registerDataComponentTypes() {

    }
}
