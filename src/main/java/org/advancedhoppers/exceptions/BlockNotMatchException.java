package org.advancedhoppers.exceptions;

import org.bukkit.Material;

public class BlockNotMatchException extends Exception {
    public int x;
    public int y;
    public int z;
    public Material block;

    public BlockNotMatchException(String message, int x, int y, int z, Material block) {
        super(message);
        this.x = x;
        this.y = y;
        this.z = z;
        this.block = block;
    }
}
