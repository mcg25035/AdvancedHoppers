package org.advancedhoppers.utils;

import org.advancedhoppers.exceptions.TypeNotSupportException;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ItemUtils {
    public static <Type>  ItemStack addTag(ItemStack item, String key, Type value) throws TypeNotSupportException {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = NamespacedKey.minecraft(key);
        switch (value) {
            case Boolean b -> pdc.set(namespacedKey, PersistentDataType.BOOLEAN, b);
            case Byte b -> pdc.set(namespacedKey, PersistentDataType.BYTE, b);
            case Short i -> pdc.set(namespacedKey, PersistentDataType.SHORT, i);
            case Integer integer -> pdc.set(namespacedKey, PersistentDataType.INTEGER, integer);
            case Long l -> pdc.set(namespacedKey, PersistentDataType.LONG, l);
            case Float v -> pdc.set(namespacedKey, PersistentDataType.FLOAT, v);
            case Double v -> pdc.set(namespacedKey, PersistentDataType.DOUBLE, v);
            case String s -> pdc.set(namespacedKey, PersistentDataType.STRING, s);
            case byte[] bytes -> pdc.set(namespacedKey, PersistentDataType.BYTE_ARRAY, bytes);
            case int[] ints -> pdc.set(namespacedKey, PersistentDataType.INTEGER_ARRAY, ints);
            case long[] longs -> pdc.set(namespacedKey, PersistentDataType.LONG_ARRAY, longs);
            case null, default -> throw new TypeNotSupportException("Type not support");
        }
        item.setItemMeta(meta);
        return item;
    }

    public static <Type> Type getTag(ItemStack item, String key, Class<Type> type) throws TypeNotSupportException {
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();

        NamespacedKey namespacedKey = NamespacedKey.minecraft(key);
        Object value;
        if (type == Byte.class) value = pdc.get(namespacedKey, PersistentDataType.BYTE);
        else if (type == Boolean.class) value = pdc.get(namespacedKey, PersistentDataType.BOOLEAN);
        else if (type == Short.class) value = pdc.get(namespacedKey, PersistentDataType.SHORT);
        else if (type == Integer.class) value = pdc.get(namespacedKey, PersistentDataType.INTEGER);
        else if (type == Long.class) value = pdc.get(namespacedKey, PersistentDataType.LONG);
        else if (type == Float.class) value = pdc.get(namespacedKey, PersistentDataType.FLOAT);
        else if (type == Double.class) value = pdc.get(namespacedKey, PersistentDataType.DOUBLE);
        else if (type == String.class) value = pdc.get(namespacedKey, PersistentDataType.STRING);
        else if (type == byte[].class) value = pdc.get(namespacedKey, PersistentDataType.BYTE_ARRAY);
        else if (type == int[].class) value = pdc.get(namespacedKey, PersistentDataType.INTEGER_ARRAY);
        else if (type == long[].class) value = pdc.get(namespacedKey, PersistentDataType.LONG_ARRAY);
        else throw new TypeNotSupportException("Type not support");

        return type.cast(value);
    }

    public static boolean hasTag(ItemStack item, String key) {
        if (item.getItemMeta() == null) return false;
        boolean hasTag = item.getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft(key), PersistentDataType.BYTE);
        hasTag = hasTag || item.getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft(key), PersistentDataType.BOOLEAN);
        hasTag = hasTag || item.getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft(key), PersistentDataType.SHORT);
        hasTag = hasTag || item.getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft(key), PersistentDataType.INTEGER);
        hasTag = hasTag || item.getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft(key), PersistentDataType.LONG);
        hasTag = hasTag || item.getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft(key), PersistentDataType.FLOAT);
        hasTag = hasTag || item.getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft(key), PersistentDataType.DOUBLE);
        hasTag = hasTag || item.getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft(key), PersistentDataType.STRING);
        hasTag = hasTag || item.getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft(key), PersistentDataType.BYTE_ARRAY);
        hasTag = hasTag || item.getItemMeta().getPersistentDataContainer().has(NamespacedKey.minecraft(key), PersistentDataType.INTEGER_ARRAY);
        return hasTag;
    }

    public static boolean isAdvancedHopper(ItemStack item) {
        if (!hasTag(item, "hopper_type")) return false;
        try {
            getTag(item, "hopper_type", String.class);
            return true;
        }
        catch (TypeNotSupportException e) {
            return false;
        }
    }

    public static String getHopperType(ItemStack item) {
        try {
            return getTag(item, "hopper_type", String.class);
        }
        catch (TypeNotSupportException e) {
            return "";
        }
    }

    public static ItemStack setHopperType(ItemStack item, String type) {
        try {
            return addTag(item, "hopper_type", type);
        }
        catch (TypeNotSupportException e) {
            e.printStackTrace();
            return null;
        }
    }
}
