package org.advancedhoppers;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

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
    public void EntitySpawnEvent(EntitySpawnEvent event){
        String eventName = new Object(){}.getClass().getEnclosingMethod().getName();
        eventPass(eventName, event);
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

    @EventHandler
    public void PlayerInteractEvent(PlayerInteractEvent event){
        String eventName = new Object(){}.getClass().getEnclosingMethod().getName();
        eventPass(eventName, event);
    }

    @EventHandler
    public void PrepareItemCraftEvent(PrepareItemCraftEvent event){
        String eventName = new Object(){}.getClass().getEnclosingMethod().getName();
        eventPass(eventName, event);
    }

    @EventHandler
    public void InventoryClickEvent(InventoryClickEvent event){
        String eventName = new Object(){}.getClass().getEnclosingMethod().getName();
        eventPass(eventName, event);
    }

    @EventHandler
    public void PluginDisableEvent(PluginDisableEvent event){
        String eventName = new Object(){}.getClass().getEnclosingMethod().getName();
        eventPass(eventName, event);
    }

    @EventHandler
    public void ItemSpawnEvent(ItemSpawnEvent event) {
        String eventName = new Object(){}.getClass().getEnclosingMethod().getName();
        eventPass(eventName, event);
    }

    @EventHandler
    public void PluginEnableEvent(PluginEnableEvent event){
        String eventName = new Object(){}.getClass().getEnclosingMethod().getName();
        eventPass(eventName, event);
    }
}
