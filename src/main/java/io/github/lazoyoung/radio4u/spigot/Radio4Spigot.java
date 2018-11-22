package io.github.lazoyoung.radio4u.spigot;

import io.github.lazoyoung.radio4u.spigot.command.PlaylistCommand;
import io.github.lazoyoung.radio4u.spigot.command.Radio4UCommand;
import io.github.lazoyoung.radio4u.spigot.command.RadioCommand;
import io.github.lazoyoung.radio4u.spigot.event.listener.PlayerEvent;
import io.github.lazoyoung.radio4u.spigot.event.listener.RadioEvent;
import io.github.lazoyoung.radio4u.spigot.radio.Radio;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Plugin(name = "Radio4U", version = "1.1")
@Description("Radio4U is a spigot plugin, offering music player functionality with .nbs files.")
@Author("LazoYoung")
@Dependency("NoteBlockAPI")
@ApiVersion(ApiVersion.Target.v1_13)
public class Radio4Spigot extends JavaPlugin {
    
    public SongRegistry songRegistry;
    
    
    @Override
    public void onEnable() {
        boolean regLoaded = loadSongRegistry();
        
        if(regLoaded) {
            try {
                loadSongs(getServer().getConsoleSender());
                loadPlaylist();
            } catch (NoClassDefFoundError e) {
                e.printStackTrace();
                if(e.toString().contains("NoteBlockAPI")) {
                    getServer().getConsoleSender().sendMessage("\u00A7cThis version of NoteBlockAPI" +
                            " is incompatible with Radio4U. Updating is recommended.");
                }
            }
        }
        else {
            getLogger().severe("Failed to load song registry.");
            getPluginLoader().disablePlugin(this);
        }
        
        registerCommands();
        registerEventListeners();
    }

    public void loadSongs(CommandSender sender) {
        sender.sendMessage("Reading nbs files from plugins/Radio4U/songs ...");
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            int cnt = songRegistry.loadSongs();
            sender.sendMessage("Radio4U found " + cnt + " songs from disk.");
            openMainChannel();
        });
    }
    
    private boolean loadSongRegistry() {
        File file = new File(getDataFolder(), "SongRegistry.yml");
        File songDir = new File(getDataFolder(), "songs");
        FileConfiguration config = null;
        
        try {
            if(!getDataFolder().isDirectory() && !getDataFolder().mkdirs()) {
                return false;
            }
            
            if (!file.exists() && !file.createNewFile()) {
                return false;
            }
            
            if(!songDir.isDirectory() && !songDir.mkdirs()) {
                return false;
            }
    
            config = new YamlConfiguration();
            config.load(file);
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
        }
        
        songRegistry = new SongRegistry(this, file, config);
        return true;
    }
    
    private void loadPlaylist() {
        File dir = new File(getDataFolder(), "playlists");
        File[] files = dir.listFiles((dir1, name) -> name.endsWith(".yml"));
        
        if(files != null) {
            for (File file : files) {
                String name = file.getName().split("\\.")[0];
                Playlist.create(this, name);
            }
        }
    }

    private void openMainChannel() {
        Radio channel = Radio.openRadioChannel(this, "main", false, Objects.requireNonNull(Playlist.getGlobalPlaylist()));
        if(channel != null) {
            channel.setPlaying(true);
            getLogger().info("Opened main radio channel.");
        }
    }

    private void registerCommands() {
        getCommand("radio4u").setExecutor(new Radio4UCommand(this));
        getCommand("playlist").setExecutor(new PlaylistCommand(this));
        getCommand("radio").setExecutor(new RadioCommand(this));
    }

    private void registerEventListeners() {
        PluginManager man = getServer().getPluginManager();
        man.registerEvents(new PlayerEvent(this), this);
        man.registerEvents(new RadioEvent(), this);
    }
    
}
