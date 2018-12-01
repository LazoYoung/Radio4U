package io.github.lazoyoung.radio4u.spigot;

import io.github.lazoyoung.radio4u.spigot.command.PlaylistCommand;
import io.github.lazoyoung.radio4u.spigot.command.Radio4UCommand;
import io.github.lazoyoung.radio4u.spigot.command.RadioCommand;
import io.github.lazoyoung.radio4u.spigot.event.listener.PlayerEvent;
import io.github.lazoyoung.radio4u.spigot.event.listener.RadioEvent;
import io.github.lazoyoung.radio4u.spigot.radio.Radio;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
import org.bukkit.plugin.java.annotation.permission.ChildPermission;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Plugin(name = "Radio4U", version = "1.1.1")
@Description("Radio4U is a spigot plugin, offering music player functionality with .nbs files.")
@Author("LazoYoung")
@Dependency("NoteBlockAPI")
@ApiVersion(ApiVersion.Target.v1_13)
@Permission(name = "radio4u.plugin", desc = "See command help for /radio4u", defaultValue = PermissionDefault.TRUE)
@Permission(name = "radio4u.plugin.reload", desc = "Reload this plugin", children = {@ChildPermission(name = "radio4u.plugin")})
@Permission(name = "radio4u.playlist", desc = "See command help for /playlist", defaultValue = PermissionDefault.TRUE)
@Permission(name = "radio4u.playlist.use", desc = "View or play a playlist", defaultValue = PermissionDefault.TRUE)
@Permission(name = "radio4u.playlist.modify", desc = "Modify a playlist")
@Permission(name = "radio4u.radio", desc = "See command help for /radio", defaultValue = PermissionDefault.TRUE)
@Permission(name = "radio4u.radio.open", desc = "Open a public channel (except local one)")
@Permission(name = "radio4u.radio.close", desc = "Close my channel")
@Permission(name = "radio4u.radio.close.others", desc = "Close other channels")
@Permission(name = "radio4u.radio.pause", desc = "Pause or resume in my channel")
@Permission(name = "radio4u.radio.pause.others", desc = "Pause or resume in another channel")
@Permission(name = "radio4u.radio.skip", desc = "Skip to next song in my playlist")
@Permission(name = "radio4u.radio.skip.others", desc = "Skip to next song in another channel")
@Permission(name = "radio4u.radio.playlist", desc = "Set playlist in my channel")
@Permission(name = "radio4u.radio.playlist.others", desc = "Set playlist in another channel")
@Permission(name = "radio4u.radio.shuffle", desc = "Shuffle playlist in my channel")
@Permission(name = "radio4u.radio.shuffle.others", desc = "Shuffle playlist in another channel")
@Permission(name = "radio4u.radio.live", desc = "Switch live-mode in my channel")
@Permission(name = "radio4u.radio.live.others", desc = "Switch live-mode in another channel")
@Permission(name = "radio4u.radio.access", desc = "Join my channel")
@Permission(name = "radio4u.radio.access.others", desc = "View or join other channels", defaultValue = PermissionDefault.TRUE)
@Permission(name = "radio4u.radio.control", desc = "Control everything in my channel", defaultValue = PermissionDefault.TRUE,
        children = {
                @ChildPermission(name = "radio4u.radio"),
                @ChildPermission(name = "radio4u.radio.open"),
                @ChildPermission(name = "radio4u.radio.close"),
                @ChildPermission(name = "radio4u.radio.pause"),
                @ChildPermission(name = "radio4u.radio.skip"),
                @ChildPermission(name = "radio4u.radio.playlist"),
                @ChildPermission(name = "radio4u.radio.shuffle"),
                @ChildPermission(name = "radio4u.radio.access")
        })
@Permission(name = "radio4u.radio.control.others", desc = "Control everything in another channel",
        children = {
                @ChildPermission(name = "radio4u.radio.control"),
                @ChildPermission(name = "radio4u.radio.close.others"),
                @ChildPermission(name = "radio4u.radio.pause.others"),
                @ChildPermission(name = "radio4u.radio.skip.others"),
                @ChildPermission(name = "radio4u.radio.playlist.others"),
                @ChildPermission(name = "radio4u.radio.shuffle.others"),
                @ChildPermission(name = "radio4u.radio.access.others")
        })
public class Radio4Spigot extends JavaPlugin {
    
    public SongRegistry songRegistry;
    public Text text;
    
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
            return;
        }
        
        loadConfigurations();
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
        Radio channel = Radio.openRadioChannel(this, "main", false, null, Objects.requireNonNull(Playlist.getGlobalPlaylist()));
        if(channel != null) {
            channel.setPlaying(true);
            getLogger().info("Opened main radio channel.");
        }
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void loadConfigurations() {
        getDataFolder().mkdir();
        this.text = new Text(this);
    }
    
    private void registerCommands() {
        PluginCommand radio4u = getCommand("radio4u");
        PluginCommand playlist = getCommand("playlist");
        PluginCommand radio = getCommand("radio");
        
        radio4u.setExecutor(new Radio4UCommand(this));
        radio4u.setUsage(this.text.get("command.usage"));
        radio4u.setPermissionMessage(this.text.get("command.forbidden"));
        playlist.setExecutor(new PlaylistCommand(this));
        playlist.setUsage(this.text.get("command.usage"));
        playlist.setPermissionMessage(this.text.get("command.forbidden"));
        radio.setExecutor(new RadioCommand(this));
        radio.setUsage(this.text.get("command.usage"));
        radio.setPermissionMessage(this.text.get("command.forbidden"));
    }

    private void registerEventListeners() {
        PluginManager man = getServer().getPluginManager();
        man.registerEvents(new PlayerEvent(this), this);
        man.registerEvents(new RadioEvent(), this);
    }
    
}
