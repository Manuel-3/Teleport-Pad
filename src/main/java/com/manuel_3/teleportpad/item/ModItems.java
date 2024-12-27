package com.manuel_3.teleportpad.item;

import com.manuel_3.teleportpad.TeleportPad;
import com.manuel_3.teleportpad.item.custom.TeleportPadConfigurator;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item TELEPORT_PAD_CONFIGURATOR = registerItem("teleport_pad_configurator", new TeleportPadConfigurator(new Item.Settings()));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(TeleportPad.MOD_ID, name), item);
    }

    public static void registerModItems() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(TELEPORT_PAD_CONFIGURATOR);
        });
    }
}
