package org.advancedhoppers;

import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class AdvancedHoppers extends JavaPlugin {
    public HashMap<String, List<Consumer<Object[]>>> eventsFunctionMapping = new HashMap<>();

    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new Events(), this);
    }

    public static AdvancedHoppers getInstance() {
        return getPlugin(AdvancedHoppers.class);
    }

    public static File getHoppersDir(String hopperType) {
        File chunkyHoppersDir = new File(getInstance().getDataFolder(), hopperType);
        if (!chunkyHoppersDir.exists()) {
            chunkyHoppersDir.mkdirs();
        }
        return chunkyHoppersDir;
    }

    public String pathToName(String path){
        String[] eventFullName = path.split("\\.");
        return eventFullName[eventFullName.length-1];
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        registerEvents();

        Method[] events = Events.class.getMethods();
        for (Method i : events){
            String eventName = pathToName(i.getName());
            if (!eventName.contains("Event")){
                continue;
            }
            eventsFunctionMapping.put(eventName, new ArrayList<>());
        }

        Reflections reflections = new Reflections("org.advancedhoppers.hoppers" ,new SubTypesScanner(false));
        Set<Class<?>> hoppers = reflections.getSubTypesOf(Object.class);
        hoppers.addAll(reflections.getSubTypesOf(Object.class));
        for (Class<?> hopper : hoppers){
            Method[] methods = hopper.getMethods();
            for (Method method : methods) {
                if (!method.getName().contains("Event")) {
                    continue;
                }
                String eventKey = pathToName(method.getName());
                if (!eventsFunctionMapping.containsKey(eventKey)) {
                    continue;
                }
                eventsFunctionMapping.get(eventKey).add((Object[] args) -> {
                    try {
                        method.invoke(hopper, args);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });


            }


        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
