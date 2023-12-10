package org.advancedhoppers.utils;

import org.advancedhoppers.AdvancedHoppers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

public class AskInput implements Listener {
    JavaPlugin main = AdvancedHoppers.getInstance();
    String question;
    Player player;
    Consumer<String> callback;

    AskInput(String question, Player player, Consumer<String> callback){
        this.question = question;
        this.player = player;
        this.callback = callback;
        player.sendMessage(question);
        Bukkit.getPluginManager().registerEvents(this, main);
    }

    @EventHandler
    public void PlayerMoveEvent(PlayerMoveEvent event){
        if (event.getPlayer().equals(player)){
            event.setCancelled(true);
        }

        player.sendMessage("§cYou can't move while answering a question!");
    }

    @EventHandler
    public void InventoryOpenEvent(InventoryOpenEvent event){
        if (event.getPlayer().equals(player)){
            event.setCancelled(true);
        }

        player.sendMessage("§cYou can't open your inventory while answering a question!");
    }

    @EventHandler
    public void BlockBreakEvent(BlockBreakEvent event){
        if (event.getPlayer().equals(player)){
            event.setCancelled(true);
        }

        player.sendMessage("§cYou can't break blocks while answering a question!");
    }

    @EventHandler
    public void BlockPlaceEvent(BlockPlaceEvent event){
        if (event.getPlayer().equals(player)){
            event.setCancelled(true);
        }

        player.sendMessage("§cYou can't place blocks while answering a question!");
    }

    @EventHandler
    public void PlayerQuitEvent(PlayerQuitEvent event){
        callback.accept("");
        unregister();
    }

    @EventHandler
    public void AsyncPlayerChatEvent(AsyncPlayerChatEvent event){
        if (event.getPlayer().equals(player)){
            callback.accept(event.getMessage());
            event.setCancelled(true);
        }
        unregister();
    }

    public void unregister(){
        PlayerMoveEvent.getHandlerList().unregister(this);
        InventoryOpenEvent.getHandlerList().unregister(this);
        BlockBreakEvent.getHandlerList().unregister(this);
        BlockPlaceEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
        AsyncPlayerChatEvent.getHandlerList().unregister(this);
    }

}
