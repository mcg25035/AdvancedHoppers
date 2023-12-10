package org.advancedhoppers;


import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.itemutils.ItemUtils;

import java.util.function.Consumer;

public class Events implements Listener {
    AdvancedHoppers main = AdvancedHoppers.getInstance();
    void eventPass(String eventName, Object ... args){
        for (String i : main.eventsFunctionMapping.keySet()){
            if (i.equals(eventName)){
                for (Consumer<Object[]> ii : main.eventsFunctionMapping.get(i)){
                    ii.accept(args);
                }
            }
        }
    }

    @EventHandler
    public void ChunkLoadEvent(ChunkLoadEvent event) {
        String eventName = new Object(){}.getClass().getEnclosingMethod().getName();
        eventPass(eventName, event);
    }

    @EventHandler
    public void ChunkUnloadEvent(ChunkUnloadEvent event) {
        String eventName = new Object(){}.getClass().getEnclosingMethod().getName();
        eventPass(eventName, event);
    }

    @EventHandler
    public void BlockPlaceEvent(BlockPlaceEvent event) {
        String eventName = new Object(){}.getClass().getEnclosingMethod().getName();
        eventPass(eventName, event);
    }

    @EventHandler
    public void BlockBreakEvent(BlockBreakEvent event){
        String eventName = new Object(){}.getClass().getEnclosingMethod().getName();
        eventPass(eventName, event);
    }
}
