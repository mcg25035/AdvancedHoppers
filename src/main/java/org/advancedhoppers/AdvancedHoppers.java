package org.advancedhoppers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class AdvancedHoppers extends JavaPlugin {
    public File pluginDir = getDataFolder();
    public HashMap<String, List<Consumer<Object[]>>> eventsFunctionMapping = new HashMap<>();
    public HashMap<String, String> languageMapping = new HashMap<>();
    public HashMap<String, Object> config = new HashMap<>();
    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new Events(), this);
    }

    public void checkPluginFile() throws IOException {

        if (!pluginDir.exists()){
            pluginDir.mkdirs();
        }
        if (!pluginDir.isDirectory()){
            pluginDir.delete();
            pluginDir.mkdirs();
        }
        File configFileReal = pluginDir.toPath().resolve("config.yml").toFile();
        if (!configFileReal.exists()){
            URL configFileTemplate = getClass().getResource("/config.yml");
            assert configFileTemplate != null;
            FileUtils.copyURLToFile(configFileTemplate,configFileReal);
        }
        if (!configFileReal.isFile()){
            boolean ignored = configFileReal.delete();
            URL configFileTemplate = getClass().getResource("/config.yml");
            assert configFileTemplate != null;
            FileUtils.copyURLToFile(configFileTemplate,configFileReal);
        }
    }

    public static AdvancedHoppers getInstance() {
        return getPlugin(AdvancedHoppers.class);
    }

    public static File getHoppersDir(String hopperType) {
        File hoppersDir = new File(getInstance().getDataFolder(), hopperType);
        if (!hoppersDir.exists()) {
            hoppersDir.mkdirs();
        }
        return hoppersDir;
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

        try{
            checkPluginFile();
        }
        catch (Exception ignored){}

        File configFile = pluginDir.toPath().resolve("config.yml").toFile();
        InputStream inputStream = null;

        try {
            inputStream = new FileInputStream(configFile);
        } catch (Exception e) {}
        this.config = (new Yaml()).load(inputStream);

        try {
            InputStream textSource = this.getClass().getClassLoader().getResourceAsStream("langs/"+config.get("lang")+".json");
            String fileData = new String(textSource.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject langJson = new Gson().fromJson(fileData , JsonObject.class);
            Map<String, JsonElement> langJsonElement = langJson.asMap();
            for (String key : langJsonElement.keySet()){
                String value = langJsonElement.get(key).getAsString();
                if (value == null) continue;
                languageMapping.put(key, value);
            }
        } catch (Exception ignored) {}

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
