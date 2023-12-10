package org.advancedhoppers.hoppers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.advancedhoppers.AdvancedHoppers;
import org.advancedhoppers.utils.LocationKey;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RemoteHopper {
//    private static File remoteHoppersDir = AdvancedHoppers.getHoppersDir("remoteHopper");
//    private static HashMap<String, Recv> codeRecvMap = new HashMap<>();
//    private static HashMap<String, Send> codeSendMap = new HashMap<>();
//
//    private static class Send{
//        private String hopperCode;
//        private Location location;
//        private Recv connectedHopper;
//        public static HashMap<LocationKey, Send> hoppers = new HashMap<>();
//
//        public static void loadHoppers(Chunk chunk){
//
//        }
//
//        private Send(Location location, String hopperCode){
//            this.location = location;
//            this.hopperCode = hopperCode;
//            hoppers.put(new LocationKey(location.getBlockX(), location.getBlockY(), location.getBlockZ()), this);
//        }
//
//        private void sendItem(){
//            if (!location.getBlock().equals(Material.HOPPER)){
//                return;
//            }
//
//            if (!isConnected()){
//                return;
//            }
//
//            if (location.getBlock().isBlockPowered()){
//                return;
//            }
//
//            Hopper hopper = (Hopper)location.getBlock().getState();
//
//            ItemStack item0 = hopper.getInventory().getItem(0);
//            if (item0 == null){
//                return;
//            }
//            item0.setAmount(item0.getAmount()-1);
//
//            ItemStack itemToSend = item0.clone();
//            itemToSend.setAmount(1);
//            ItemStack leftItems = connectedHopper.recvItem(itemToSend);
//            if (leftItems == null){
//                return;
//            }
//
//            hopper.getInventory().setItem(0, itemToSend);
//        }
//
//        private void unload(){
//            if (this.isConnected()){
//                this.connectedHopper.disconnect();
//            }
//        }
//
//        private void removeHopper(){
//            unload();
//        }
//
//        private void saveHopper(){
//
//        }
//
//        public Send construct(Location location, String hopperCode){
//            for (Recv i : Recv.hoppers.values()){
//                if (i.hopperCode.equals(hopperCode) && i.isConnected()){
//                    return null;
//                }
//            }
//            return new Send(location, hopperCode);
//        }
//
//        public void connect(Recv recv){
//            this.connectedHopper = recv;
//        }
//
//        public boolean isConnected(){
//            return this.connectedHopper != null;
//        }
//
//        public void disconnect(){
//            this.connectedHopper = null;
//        }
//
//        public void onHopperBreak(){
//            removeHopper();
//        }
//
//    }
//
//    private static class Recv{
//        private String hopperCode;
//        private Location location;
//        private Send connectedHopper;
//        public static HashMap<LocationKey, Recv> hoppers = new HashMap<>();
//
//        public static void loadHoppers(Chunk chunk){
//
//        }
//
//        private static void loadHopper(JsonObject jsonObject, String filename, String worldName){
//            filename = filename.replace(".json", "");
//            String[] filenameSplit = filename.split("_");
//            int x = Integer.parseInt(filenameSplit[0]);
//            int y = Integer.parseInt(filenameSplit[1]);
//            int z = Integer.parseInt(filenameSplit[2]);
//            Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
//            String hopperCode = jsonObject.get("code").getAsString();
//            new Recv(location, hopperCode);
//        }
//
//        private Recv(Location location, String hopperCode){
//            this.location = location;
//            this.hopperCode = hopperCode;
//            hoppers.put(new LocationKey(location.getBlockX(), location.getBlockY(), location.getBlockZ()), this);
//        }
//
//        private void unload(){
//            if (this.isConnected()){
//                this.connectedHopper.disconnect();
//            }
//            hoppers.remove(new LocationKey(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
//        }
//
//        private void removeHopper(){
//            unload();
//            int x = location.getBlockX();
//            int y = location.getBlockY();
//            int z = location.getBlockZ();
//            int chunkX = location.getChunk().getX();
//            int chunkZ = location.getChunk().getZ();
//            String world = location.getWorld().getName();
//            File chunkDir = new File(remoteHoppersDir, chunkX + "_" + chunkZ + "_" + world);
//            if (!chunkDir.exists()){
//                return;
//            }
//            String filename = x + "_" + y + "_" + z + ".json";
//            File file = chunkDir.toPath().resolve(filename).toFile();
//            if (!file.exists()){
//                return;
//            }
//            file.delete();
//        }
//
//        private void saveHopper(){
//            unload();
//            int x = location.getBlockX();
//            int y = location.getBlockY();
//            int z = location.getBlockZ();
//            int chunkX = location.getChunk().getX();
//            int chunkZ = location.getChunk().getZ();
//            String world = location.getWorld().getName();
//            String code = hopperCode;
//            JsonObject jsonObject = new JsonObject();
//            jsonObject.addProperty("code", code);
//            String json = jsonObject.toString();
//            File chunkDir = new File(remoteHoppersDir, chunkX + "_" + chunkZ + "_" + world);
//            if (!chunkDir.exists()){
//                chunkDir.mkdirs();
//            }
//            String filename = x + "_" + y + "_" + z + ".json";
//            try {
//                File file = chunkDir.toPath().resolve(filename).toFile();
//                FileWriter fileWriter = new FileWriter(file);
//                fileWriter.write(json);
//                fileWriter.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//
//
//        }
//
//        public void connect(Send recv){
//            this.connectedHopper = recv;
//        }
//
//        public boolean isConnected(){
//            return this.connectedHopper != null;
//        }
//
//        public void disconnect(){
//            this.connectedHopper = null;
//        }
//
//        public Recv construct(Location location, String hopperCode){
//            List<Send> codeMatches = new ArrayList<>();
//            for (Send i : Send.hoppers.values()){
//                if (!i.hopperCode.equals(hopperCode)){
//                    continue;
//                }
//
//                if (i.isConnected()){
//                    return null;
//                }
//
//                codeMatches.add(i);
//            }
//
//            if (codeMatches.size() > 1){
//                return null;
//            }
//
//            Recv result = new Recv(location, hopperCode);
//            if (codeMatches.size() == 1){
//                result.connect(codeMatches.get(0));
//                codeMatches.get(0).connect(result);
//            }
//        }
//
//        public ItemStack recvItem(ItemStack item){
//            if (!location.getBlock().equals(Material.HOPPER)){
//                return item;
//            }
//            Hopper hopper = (Hopper)location.getBlock().getState();
//            HashMap<Integer, ItemStack> leftItems = hopper.getInventory().addItem(item);
//            return leftItems.get(0);
//        }
//
//        public void onHopperBreak(){
//            removeHopper();
//        }
//    }
//
//
//
//
//    private boolean connected = false;
//    private String hopperCode;
//    private Location targetHopper;
//    private Location location;
//    private BukkitTask transferTask;
//
//
//
//    public RemoteHopper(Location location, String hopperCode) {
//        this.location = location;
//        this.hopperCode = hopperCode;
//        recvHoppers.put(new LocationKey(location.getBlockX(), location.getBlockY(), location.getBlockZ()), this);
//        for (RemoteHopper i : recvHoppers.values()){
//            if (i.connected){
//
//            }
//        }
//
//
//        transferTask = Bukkit.getScheduler().runTaskTimer(AdvancedHoppers.getInstance(), () -> {
//            for (Entity entity : location.getChunk().getEntities()) {
//                if (!(entity instanceof Item)) {
//                    continue;
//                }
//                Item item = (Item) entity;
//                if (!item.getLocation().getChunk().equals(location.getChunk())) {
//                    continue;
//                }
//                ItemStack leftItems = (((Hopper)(location.getBlock().getState())).getInventory().addItem(item.getItemStack())).get(0);
//                item.setItemStack(leftItems);
//            }
//        }, 0, 1);
//    }
//
//    public void collectItems(Item item) {
//        Location above = location.clone().add(0.5, 1, 0.5);
//        item.teleport(above);
//    }
//
//    public void unload(){
//        transferTask.cancel();
//        recvHoppers.remove(new LocationKey(this.location.getBlockX(), this.location.getBlockY(), this.location.getBlockZ()));
//        hopperChunkMap.remove(this.chunk);
//    }
//
//    public void saveHopper() {
//        this.unload();
//        String x = String.valueOf(this.chunk.getX());
//        String z = String.valueOf(this.chunk.getZ());
//        String worldName = this.chunk.getWorld().getName();
//        String filename = x + "_" + z + "_" + worldName + ".json";
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("x", this.location.getBlockX());
//        jsonObject.addProperty("y", this.location.getBlockY());
//        jsonObject.addProperty("z", this.location.getBlockZ());
//        String json = jsonObject.toString();
//        try {
//            File file = remoteHoppersDir.toPath().resolve(filename).toFile();
//            FileWriter fileWriter = new FileWriter(file);
//            fileWriter.write(json);
//            fileWriter.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void loadHopper(Chunk chunk){
//        String x = String.valueOf(chunk.getX());
//        String z = String.valueOf(chunk.getZ());
//        String worldName = chunk.getWorld().getName();
//        String filename = x + "_" + z + "_" + worldName + ".json";
//        File file = new File(remoteHoppersDir, filename);
//        if (!file.exists()){
//            return;
//        }
//        try {
//            FileReader fileReader = new FileReader(file);
//            Gson gson = new GsonBuilder().create();
//            JsonObject jsonObject = gson.fromJson(fileReader, JsonObject.class);
//            int x1 = jsonObject.get("x").getAsInt();
//            int y1 = jsonObject.get("y").getAsInt();
//            int z1 = jsonObject.get("z").getAsInt();
//            Location location = new Location(Bukkit.getWorld(worldName), x1, y1, z1);
//            new RemoteHopper(location);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void removeHopper(){
//        this.unload();
//        String x = String.valueOf(this.chunk.getX());
//        String z = String.valueOf(this.chunk.getZ());
//        String worldName = this.chunk.getWorld().getName();
//        String filename = x + "_" + z + "_" + worldName + ".json";
//        File file = new File(remoteHoppersDir, filename);
//        if (!file.exists()){
//            return;
//        }
//        file.delete();
//    }
//
//
//    public static void BlockPlaceEvent(BlockPlaceEvent event) {
//        ItemStack currentItem = event.getItemInHand();
//        Chunk chunk = event.getBlock().getChunk();
//        String hopperType = (String)(ItemUtils.itemGetNbtPath(currentItem, "AdvancedHoppers.type"));
//
//        if (hopperType == null) {
//            return;
//        }
//
//        if (!hopperType.equals("chunky")) {
//            return;
//        }
//
//        if (hopperChunkMap.containsKey(chunk)){
//            event.setCancelled(true);
//            event.setBuild(false);
//            event.getPlayer().sendMessage("§cThere is already a chunky hopper in this chunk at " + hopperChunkMap.get(chunk).location.toString() + "!");
//            return;
//        }
//
//        Block block = event.getBlockPlaced();
//        Location location = block.getLocation();
//        new RemoteHopper(location);
//    }
//
//    public static void ChunkLoadEvent(ChunkLoadEvent event) {
//        Chunk chunk = event.getChunk();
//        RemoteHopper.loadHopper(chunk);
//    }
//
//    public static void ChunkUnloadEvent(ChunkUnloadEvent event) {
//        Chunk chunk = event.getChunk();
//        if (hopperChunkMap.get(chunk) == null){
//            return;
//        }
//        hopperChunkMap.get(chunk).saveHopper();
//    }
//
//    public static void BlockBreakEvent(BlockBreakEvent event) {
//        Location location = event.getBlock().getLocation();
//        LocationKey locationKey = new LocationKey(
//                location.getBlockX(), location.getBlockY(), location.getBlockZ()
//        );
//        if (recvHoppers.get(locationKey) == null){
//            return;
//        }
//        recvHoppers.get(locationKey).removeHopper();
//    }
}
