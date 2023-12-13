package org.advancedhoppers.utils;

import org.bukkit.Location;

public class MessageUtils {
    public static String locationToString(Location location){
        return location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ() + " " + location.getWorld().getName();
    }
}
