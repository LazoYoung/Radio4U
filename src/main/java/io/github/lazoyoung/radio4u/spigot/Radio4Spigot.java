package io.github.lazoyoung.radio4u.spigot;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Radio4Spigot extends JavaPlugin {
    
    public SongRegistry songRegistry;
    
    
    @Override
    public void onEnable() {
        boolean regLoaded = loadSongRegistry();
        
        if(!regLoaded) {
            getLogger().severe("Failed to load or create SongRegistry.yml");
            getPluginLoader().disablePlugin(this);
        }
        
        getCommand("song").setExecutor(new SongCommand(songRegistry));
    }
    
    private boolean loadSongRegistry() {
        File file = new File(getDataFolder(), "SongRegistry.yml");
        FileConfiguration config = null;
        
        try {
    
            if (!file.exists()) {
                getDataFolder().mkdirs();
                
                if(!file.createNewFile()) {
                    return false;
                }
            }
    
            config = new YamlConfiguration();
            config.load(file);
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
        }
        
        songRegistry = new SongRegistry(this, file, config);
        return true;
    }
    
}
