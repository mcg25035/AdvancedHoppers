package org.advancedhoppers.hoppers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.advancedhoppers.AdvancedHoppers;
import org.advancedhoppers.utils.InventoryUtil;
import org.advancedhoppers.utils.LocationKey;
import org.advancedhoppers.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.itemutils.ItemUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class ChunkyHopper{
    private static HashMap<LocationKey, ChunkyHopper> hoppers = new HashMap<>();
    private static HashMap<Chunk, ChunkyHopper> hopperChunkMap = new HashMap<>();
    private static File chunkyHoppersDir = AdvancedHoppers.getHoppersDir("chunkyHopper");
    private Location location;
    private Chunk chunk;
    private BukkitTask collectTask;



    public static ItemStack chunkyHopperItem(){
        ItemStack itemStack = new ItemStack(Material.HOPPER);
        HashMap<String, String> nbt = new HashMap<>();
        nbt.put("Type", "chunky");
        itemStack = ItemUtils.itemSetNbtPath(itemStack, "AdvancedHoppers", nbt);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(AdvancedHoppers.getInstance().languageMapping.get("chunkyHopper"));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
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
        String hopperType = (String)(ItemUtils.itemGetNbtPath(currentItem, "AdvancedHoppers.Type"));

        if (hopperType == null) {
            return;
        }

        if (!hopperType.equals("chunky")) {
            return;
        }

        if (hopperChunkMap.containsKey(chunk)){
            event.setCancelled(true);
            event.setBuild(false);
            event.getPlayer().sendMessage(
                    AdvancedHoppers.getInstance().languageMapping.get("chunkyHopperExist")
                            +"§e"
                            +MessageUtils.locationToString(hopperChunkMap.get(chunk).location)
            );
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
        if (!event.isDropItems()){
            return;
        }
        Inventory hopperInventory = ((Hopper)(event.getBlock().getState())).getInventory();
        ItemStack[] hopperItem = hopperInventory.getContents();
        hopperInventory.clear();

        event.setDropItems(false);
        location.getWorld().dropItem(location, chunkyHopperItem());

        for (ItemStack i : hopperItem){
            if (InventoryUtil.isEmpty(i)){
                continue;
            }
            location.getWorld().dropItem(location, i);
        }
    }

    public static boolean recipeMatch(ItemStack craftingRecipe[]){
        boolean canCraft = true;
        canCraft = canCraft && InventoryUtil.isType(craftingRecipe[0], Material.IRON_BLOCK);
        canCraft = canCraft && InventoryUtil.isEmpty(craftingRecipe[1]);
        canCraft = canCraft && InventoryUtil.isType(craftingRecipe[2], Material.IRON_BLOCK);
        canCraft = canCraft && InventoryUtil.isType(craftingRecipe[3], Material.IRON_BLOCK);
        canCraft = canCraft && InventoryUtil.isType(craftingRecipe[4], Material.CHEST);
        canCraft = canCraft && InventoryUtil.containAmount(craftingRecipe[4], 16);
        canCraft = canCraft && InventoryUtil.isType(craftingRecipe[5], Material.IRON_BLOCK);
        canCraft = canCraft && InventoryUtil.isEmpty(craftingRecipe[6]);
        canCraft = canCraft && InventoryUtil.isType(craftingRecipe[7], Material.IRON_BLOCK);
        canCraft = canCraft && InventoryUtil.isEmpty(craftingRecipe[8]);
        return canCraft;
    }

    public static ItemStack[] afterCraft(ItemStack craftingRecipe[]){
        ItemStack[] afterCraft = new ItemStack[9];
        for (int i=0;i<9;i++){
            if (craftingRecipe[i] == null){
                afterCraft[i] = null;
                continue;
            }
            afterCraft[i] = craftingRecipe[i].clone();
            if (i == 4){
                continue;
            }
            afterCraft[i].setAmount(afterCraft[i].getAmount() - 1);
        }
        afterCraft[4].setAmount(afterCraft[4].getAmount() - 16);
        return afterCraft;
    }

    public static void PrepareItemCraftEvent(PrepareItemCraftEvent event){
        CraftingInventory inventory = event.getInventory();
        if (!recipeMatch(inventory.getMatrix())){
            return;
        }

        event.getInventory().setResult(chunkyHopperItem());
    }

    public static void InventoryClickEvent(InventoryClickEvent event){
        if (!event.getInventory().getType().equals(InventoryType.WORKBENCH)){
            return;
        }

        if (!event.getSlotType().equals(InventoryType.SlotType.RESULT)){
            return;
        }

        CraftingInventory inventory = (CraftingInventory) event.getInventory();
        ItemStack[] craftingMatrix = inventory.getMatrix();

        if (!recipeMatch(inventory.getMatrix())){
            return;
        }

        event.setCancelled(true);
        event.setResult(Event.Result.DENY);

        if (event.getClick().isRightClick()){
            return;
        }

        if (event.getClick().isShiftClick()){
            while (recipeMatch(craftingMatrix)){
                craftingMatrix = afterCraft(craftingMatrix);
                HashMap<Integer, ItemStack> leftItem = event.getWhoClicked().getInventory().addItem(chunkyHopperItem());
                if (!leftItem.isEmpty()){
                    for (ItemStack i : leftItem.values()){
                        event.getWhoClicked().getWorld().dropItem(event.getWhoClicked().getLocation(), i);
                    }
                    break;
                }
            }
            inventory.setMatrix(craftingMatrix);
            return;
        }

        ItemStack[] afterCraft = afterCraft(inventory.getMatrix());

        ItemStack cursor = event.getWhoClicked().getItemOnCursor();

        if (InventoryUtil.isEmpty(cursor)){
            event.getWhoClicked().setItemOnCursor(chunkyHopperItem());
            inventory.setMatrix(afterCraft);
            return;
        }

        if (!cursor.isSimilar(chunkyHopperItem())){
            return;
        }

        if (cursor.getAmount() == 64){
            return;
        }

        String hopperType = (String)(ItemUtils.itemGetNbtPath(cursor, "AdvancedHoppers.Type"));

        if (!hopperType.equals("chunky")){
            return;
        }

        inventory.setMatrix(afterCraft);
        cursor.setAmount(cursor.getAmount() + 1);
        event.getWhoClicked().setItemOnCursor(cursor);
    }

    public static void PluginDisableEvent(PluginDisableEvent event){
        for (ChunkyHopper i : hopperChunkMap.values()){
            i.saveHopper();
        }
    }

    public static void PluginEnableEvent(PluginEnableEvent event){
        for (World i : Bukkit.getWorlds()){
            for (Chunk j : i.getLoadedChunks()){
                ChunkyHopper.loadHopper(j);
            }
        }
    }
}
