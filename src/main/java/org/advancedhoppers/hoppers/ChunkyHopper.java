package org.advancedhoppers.hoppers;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.advancedhoppers.AdvancedHoppers;
import org.advancedhoppers.exceptions.BlockNotMatchException;
import org.advancedhoppers.utils.InventoryUtil;
import org.advancedhoppers.utils.LocationKey;
import org.advancedhoppers.utils.MessageUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.advancedhoppers.utils.ItemUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ChunkyHopper{
    private enum HopperMode {
        SUCK_UP,
        SUCK_DOWN,
        SUCK_ALL
    }

    private static HashMap<LocationKey, ChunkyHopper> hoppers = new HashMap<>();
    private static HashMap<Chunk, ChunkyHopper> hopperChunkMap = new HashMap<>();
    private static File chunkyHoppersDir = AdvancedHoppers.getHoppersDir("chunkyHopper");
//    private static BukkitTask debugTask = Bukkit.getScheduler().runTaskTimer(AdvancedHoppers.getInstance(), () -> {
//        Bukkit.getLogger().info("ChunkyHopper count: " + hoppers.size());
//    }, 0, 20*60);

    private Location location;
    private Chunk chunk;
    private BukkitTask collectTask;
    private Hopper hopper;
    private HopperMode mode = HopperMode.SUCK_ALL;


    public static ItemStack chunkyHopperItem(){
        ItemStack itemStack = new ItemStack(Material.HOPPER);
        itemStack = ItemUtils.setHopperType(itemStack, "chunky");
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(AdvancedHoppers.getInstance().languageMapping.get("chunkyHopper"));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public ChunkyHopper(Location location, HopperMode mode) throws BlockNotMatchException {
        this.location = location;
        chunk = location.getChunk();
        hopperChunkMap.put(chunk, this);
        hoppers.put(new LocationKey(location.getBlockX(), location.getBlockY(), location.getBlockZ()), this);

        if (mode == null) mode = HopperMode.SUCK_ALL;
        else this.mode = mode;

        BlockState blockState = location.getBlock().getState();
        if (!(blockState instanceof Hopper hopper)) {
            this.removeHopper();
            throw new BlockNotMatchException(
                    "Block is not a hopper",
                    location.getBlockX(),
                    location.getBlockY(),
                    location.getBlockZ(),
                    location.getBlock().getType()
            );
        }
        this.hopper = hopper;

        AtomicInteger count = new AtomicInteger(0);

        collectTask = Bukkit.getScheduler().runTaskTimer(AdvancedHoppers.getInstance(), () -> {
            if (!location.isChunkLoaded()) {
                saveHopper();
                collectTask.cancel();
                return;
            }

            double tps = Bukkit.getTPS()[0];
            count.getAndIncrement();
            if (15 < tps && tps <= 18) {
                if (count.get() % 2 != 1) {
                    return;
                }
            }
            if (12 < tps && tps <= 15) {
                if (count.get() % 4 != 1) {
                    return;
                }
            }
            if (10 < tps && tps <= 12) {
                if (count.get() % 8 != 1) {
                    return;
                }
            }
            if (tps <= 10) {
                if (count.get() % 10 != 1) {
                    return;
                }
            }

            if (count.get() >= 20) {
                count.set(0);
            }

            hopperCollect();
        }, 0, 1);
    }

    public void hopperCollect() {
        if (!location.getBlock().getType().equals(Material.HOPPER)){
            collectTask.cancel();
            return;
        }

        if (location.getBlock().isBlockPowered()) return;
        boolean changed = false;

        for (Entity entity : location.getChunk().getEntities()) {
            if (mode == HopperMode.SUCK_UP) {
                if (entity.getLocation().getBlockY() < location.getBlockY()) continue;
            }
            if (mode == HopperMode.SUCK_DOWN) {
                if (entity.getLocation().getBlockY() > location.getBlockY()) continue;
            }


            if (!(entity instanceof Item item)) continue;
            if (!item.getLocation().getChunk().equals(location.getChunk())) continue;

            ItemStack itemStack = item.getItemStack();
            int amount = itemStack.getAmount();

            ItemStack leftItems = hopper.getInventory().addItem(itemStack).get(0);
            if (leftItems == null) leftItems = new ItemStack(Material.AIR);
            if (itemStack.isSimilar(leftItems) && leftItems.getAmount() == amount) continue;

            item.setItemStack(leftItems);
            changed = true;
        }

        if (!changed) return;
        location.getBlock().getState().update();
    }

    public void unload(){
        if (collectTask != null) collectTask.cancel();
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
        jsonObject.addProperty("mode", this.mode.name());
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
        if (!file.exists()) return;
        try {
            FileReader fileReader = new FileReader(file);
            Gson gson = new GsonBuilder().create();
            JsonObject jsonObject = gson.fromJson(fileReader, JsonObject.class);
            int x1 = jsonObject.get("x").getAsInt();
            int y1 = jsonObject.get("y").getAsInt();
            int z1 = jsonObject.get("z").getAsInt();
            String mode;
            if (!jsonObject.has("mode")) mode = null;
            else mode = jsonObject.get("mode").getAsString();
            Location location = new Location(Bukkit.getWorld(worldName), x1, y1, z1);
            new ChunkyHopper(location, mode == null ? null : HopperMode.valueOf(mode));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (BlockNotMatchException exception) {
            Bukkit.getLogger().warning(
                    "Expect a hopper block but got " +
                            exception.block + " at"+
                            " " + exception.x +
                            " " + exception.y +
                            " " + exception.z + "."
            );
            Bukkit.getLogger().warning("Deleting the wrong hopper file.");
        }

    }

    public void removeHopper(){
        this.unload();
        String x = String.valueOf(this.chunk.getX());
        String z = String.valueOf(this.chunk.getZ());
        String worldName = this.chunk.getWorld().getName();
        String filename = x + "_" + z + "_" + worldName + ".json";
        File file = new File(chunkyHoppersDir, filename);
        if (!file.exists()) return;
        file.delete();
    }


    public static void BlockPlaceEvent(BlockPlaceEvent event) {
        ItemStack currentItem = event.getItemInHand();
        Chunk chunk = event.getBlock().getChunk();
        if (!ItemUtils.isAdvancedHopper(currentItem)) return;
        String hopperType = ItemUtils.getHopperType(currentItem);
        if (hopperType == null) return;
        if (!hopperType.equals("chunky")) return;
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
        Hopper hopper = (Hopper) block.getState();
        hopper.setCustomName(AdvancedHoppers.getInstance().languageMapping.get("chunkyHopper").replaceAll("§6", ""));
        hopper.update();
        try{
            new ChunkyHopper(location, null);
        }
        catch (BlockNotMatchException exception) {
            Bukkit.getLogger().warning(
                    "Expect a hopper block but got " +
                            exception.block + " at"+
                            " " + exception.x +
                            " " + exception.y +
                            " " + exception.z + "."
            );
        }
    }

    public static void ItemSpawnEvent(ItemSpawnEvent event){
        Chunk chunk = event.getLocation().getChunk();
        if (hopperChunkMap.get(chunk) == null) return;
        hopperChunkMap.get(chunk).hopperCollect();
    }

    public static void EntitySpawnEvent(EntitySpawnEvent event) {
        if (!event.getEntityType().equals(EntityType.WANDERING_TRADER)){
            return;
        }

        WanderingTrader wanderingLeash = ((WanderingTrader)(event.getEntity()));

        MerchantRecipe merchant = new MerchantRecipe(chunkyHopperItem(), Integer.MAX_VALUE);
        merchant.addIngredient(new ItemStack(Material.HOPPER, (int) (3 + Math.round(4*Math.random()))));
        merchant.addIngredient(new ItemStack(Material.DIAMOND,(int) (8 + Math.round(4*Math.random()))));

        List<MerchantRecipe> tradeList = new ArrayList<>(wanderingLeash.getRecipes());
        tradeList.add(0, merchant);
        wanderingLeash.setRecipes(tradeList);
    }

    public static void ChunkLoadEvent(ChunkLoadEvent event) {
        try{
            Chunk chunk = event.getChunk();
            ChunkyHopper.loadHopper(chunk);
        }
        catch (Exception e){
            Bukkit.getLogger().warning("Error while passing ChunkLoadEvent");
            e.printStackTrace();
        }
    }

    public static void ChunkUnloadEvent(ChunkUnloadEvent event) {
        try{
            Chunk chunk = event.getChunk();
            if (hopperChunkMap.get(chunk) == null) return;
            hopperChunkMap.get(chunk).saveHopper();
        }
        catch (Exception e){
            Bukkit.getLogger().warning("Error while passing ChunkUnloadEvent");
            e.printStackTrace();
        }
    }

    public static void PlayerInteractEvent(PlayerInteractEvent event) {
        if (!event.getPlayer().isSneaking()) return;
        if (!event.hasBlock()) return;
        if (!event.getClickedBlock().getType().equals(Material.HOPPER)) return;
        if (!event.getAction().isRightClick()) return;
        if (!event.getPlayer().getEquipment().getItemInMainHand().isEmpty()) return;
        Location location = event.getClickedBlock().getLocation();
        LocationKey locationKey = new LocationKey(
                location.getBlockX(), location.getBlockY(), location.getBlockZ()
        );
        if (hoppers.get(locationKey) == null) return;
        hoppers.get(locationKey).mode = HopperMode.values()[(hoppers.get(locationKey).mode.ordinal() + 1) % 3];
        event.getPlayer().sendMessage(
                AdvancedHoppers.getInstance().languageMapping.get("chunkyHopperMode")
                        .replaceAll("§6", "")
                        .replaceAll("%mode%", AdvancedHoppers.getInstance().languageMapping.get(hoppers.get(locationKey).mode.name()))
        );
        event.setCancelled(true);
    }

    public static void BlockBreakEvent(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        LocationKey locationKey = new LocationKey(
                location.getBlockX(), location.getBlockY(), location.getBlockZ()
        );

        if (hoppers.get(locationKey) == null) return;

        hoppers.get(locationKey).removeHopper();
        if (!event.isDropItems()) return;
        Inventory hopperInventory = ((Hopper)(event.getBlock().getState())).getInventory();
        ItemStack[] hopperItem = hopperInventory.getContents();
        hopperInventory.clear();

        event.setDropItems(false);
        location.getWorld().dropItem(location, chunkyHopperItem());

        for (ItemStack i : hopperItem){
            if (InventoryUtil.isEmpty(i)) continue;
            location.getWorld().dropItem(location, i);
        }
    }

    public static boolean recipeMatch(ItemStack craftingRecipe[]){
        boolean canCraft = InventoryUtil.isType(craftingRecipe[0], Material.IRON_BLOCK);
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
        if (!recipeMatch(inventory.getMatrix())) return;
        event.getInventory().setResult(chunkyHopperItem());
    }

    public static void InventoryClickEvent(InventoryClickEvent event){
        if (!event.getInventory().getType().equals(InventoryType.WORKBENCH)) return;
        if (!event.getSlotType().equals(InventoryType.SlotType.RESULT)) return;

        CraftingInventory inventory = (CraftingInventory) event.getInventory();
        ItemStack[] craftingMatrix = inventory.getMatrix();

        if (!recipeMatch(inventory.getMatrix())) return;

        event.setCancelled(true);
        event.setResult(Event.Result.DENY);

        if (event.getClick().isRightClick()) return;

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
        if (!cursor.isSimilar(chunkyHopperItem())) return;
        if (cursor.getAmount() == 64) return;
        if (!ItemUtils.isAdvancedHopper(cursor)) return;

        String hopperType = ItemUtils.getHopperType(cursor);

        if (!hopperType.equals("chunky")) return;

        inventory.setMatrix(afterCraft);
        cursor.setAmount(cursor.getAmount() + 1);
        event.getWhoClicked().setItemOnCursor(cursor);
    }

    public static void PluginDisableEvent(PluginDisableEvent event){
        List<ChunkyHopper> hoppers = new ArrayList<>();
        hoppers.addAll(ChunkyHopper.hopperChunkMap.values());
        for (ChunkyHopper i : hoppers){
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
