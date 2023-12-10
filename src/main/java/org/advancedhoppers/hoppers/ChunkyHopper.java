package org.advancedhoppers.hoppers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.advancedhoppers.AdvancedHoppers;
import org.advancedhoppers.utils.LocationKey;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.itemutils.ItemUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class ChunkyHopper{
    private static HashMap<LocationKey, ChunkyHopper> hoppers = new HashMap<>();
    private static HashMap<Chunk, ChunkyHopper> hopperChunkMap = new HashMap<>();
    private static File chunkyHoppersDir = AdvancedHoppers.getHoppersDir("chunkyHopper");
    private Location location;
    private Chunk chunk;
    private BukkitTask collectTask;



    public ChunkyHopper(Location location) {
        this.location = location;
        chunk = location.getChunk();
        hopperChunkMap.put(chunk, this);
        hoppers.put(new LocationKey(location.getBlockX(), location.getBlockY(), location.getBlockZ()), this);
        collectTask = Bukkit.getScheduler().runTaskTimer(AdvancedHoppers.getInstance(), () -> {
            if (location.getBlock().isBlockPowered()){
                return;
            }

            for (Entity entity : location.getChunk().getEntities()) {
                if (!(entity instanceof Item)) {
                    continue;
                }
                Item item = (Item) entity;
                if (!item.getLocation().getChunk().equals(location.getChunk())) {
                    continue;
                }
                ItemStack leftItems = (((Hopper)(location.getBlock().getState())).getInventory().addItem(item.getItemStack())).get(0);
                item.setItemStack(leftItems);
            }
        }, 0, 1);
    }

    public void collectItems(Item item) {
        Location above = location.clone().add(0.5, 1, 0.5);
        item.teleport(above);
    }

    public void unload(){
        collectTask.cancel();
        hoppers.remove(new LocationKey(this.location.getBlockX(), this.location.getBlockY(), this.location.getBlockZ()));
        hopperChunkMap.remove(this.chunk);
    }

    public void saveHopper() {
        this.unload();
        String x = String.valueOf(this.chunk.getX());
        String z = String.valueOf(this.chunk.getZ());
        String worldName = this.chunk.getWorld().getName();
        String filename = x + "_" + z + "_" + worldName + ".json";
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("x", this.location.getBlockX());
        jsonObject.addProperty("y", this.location.getBlockY());
        jsonObject.addProperty("z", this.location.getBlockZ());
        String json = jsonObject.toString();
        try {
            File file = chunkyHoppersDir.toPath().resolve(filename).toFile();
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(json);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadHopper(Chunk chunk){
        String x = String.valueOf(chunk.getX());
        String z = String.valueOf(chunk.getZ());
        String worldName = chunk.getWorld().getName();
        String filename = x + "_" + z + "_" + worldName + ".json";
        File file = new File(chunkyHoppersDir, filename);
        if (!file.exists()){
            return;
        }
        try {
            FileReader fileReader = new FileReader(file);
            Gson gson = new GsonBuilder().create();
            JsonObject jsonObject = gson.fromJson(fileReader, JsonObject.class);
            int x1 = jsonObject.get("x").getAsInt();
            int y1 = jsonObject.get("y").getAsInt();
            int z1 = jsonObject.get("z").getAsInt();
            Location location = new Location(Bukkit.getWorld(worldName), x1, y1, z1);
            new ChunkyHopper(location);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeHopper(){
        this.unload();
        String x = String.valueOf(this.chunk.getX());
        String z = String.valueOf(this.chunk.getZ());
        String worldName = this.chunk.getWorld().getName();
        String filename = x + "_" + z + "_" + worldName + ".json";
        File file = new File(chunkyHoppersDir, filename);
        if (!file.exists()){
            return;
        }
        file.delete();
    }


    public static void BlockPlaceEvent(BlockPlaceEvent event) {
        ItemStack currentItem = event.getItemInHand();
        Chunk chunk = event.getBlock().getChunk();
        String hopperType = (String)(ItemUtils.itemGetNbtPath(currentItem, "AdvancedHoppers.type"));

        if (hopperType == null) {
            return;
        }

        if (!hopperType.equals("chunky")) {
            return;
        }

        if (hopperChunkMap.containsKey(chunk)){
            event.setCancelled(true);
            event.setBuild(false);
            event.getPlayer().sendMessage("§cThere is already a chunky hopper in this chunk at " + hopperChunkMap.get(chunk).location.toString() + "!");
            return;
        }

        Block block = event.getBlockPlaced();
        Location location = block.getLocation();
        new ChunkyHopper(location);
    }

    public static void ChunkLoadEvent(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        ChunkyHopper.loadHopper(chunk);
    }

    public static void ChunkUnloadEvent(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        if (hopperChunkMap.get(chunk) == null){
            return;
        }
        hopperChunkMap.get(chunk).saveHopper();
    }

    public static void BlockBreakEvent(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        LocationKey locationKey = new LocationKey(
                location.getBlockX(), location.getBlockY(), location.getBlockZ()
        );
        if (hoppers.get(locationKey) == null){
            return;
        }
        hoppers.get(locationKey).removeHopper();
    }
}
