package org.advancedhoppers.utils;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtil {
    public static boolean isEmpty(ItemStack item){
        if (item == null){
            return true;
        }

        return item.getType().isAir();
    }

    public static boolean isType(ItemStack item, Material type){
        if (item == null){
            return false;
        }

        return item.getType().equals(type);
    }

    public static boolean containAmount(ItemStack item, int amount){
        if (item == null){
            return false;
        }

        return item.getAmount() >= amount;
    }
}
