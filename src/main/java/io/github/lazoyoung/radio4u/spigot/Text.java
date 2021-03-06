package io.github.lazoyoung.radio4u.spigot;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Text {
    
    private Radio4Spigot plugin;
    private Logger log;
    private File file;
    private FileConfiguration config;
    
    public Text(Radio4Spigot plugin) {
        this.plugin = plugin;
        this.log = plugin.getLogger();
        initFile();
    }
    
    public static Text initText(Radio4Spigot plugin) {
        return new Text(plugin);
    }
    
    public String get(String path, @Nullable Object... args) {
        String def = this.config.getDefaults().getString(path);
        String val = this.config.getString(path, def);
        if(val == null) {
            return this.config.getString("text.unknown", "Unknown text. Review text.yml");
        }
        val = ChatColor.translateAlternateColorCodes('&', val);
        return String.format(val, args);
    }
    
    private void initFile() {
        file = new File(this.plugin.getDataFolder(), "text.yml");
    
        try {
            if (this.file.createNewFile()) {
                log.info("Created file: text.yml");
            }
            this.config = YamlConfiguration.loadConfiguration(this.file);
            this.config.options().copyDefaults(true);
            setDefaults();
        } catch (IOException e) {
            e.printStackTrace();
            log.severe("Failed to create file: text.yml");
        }
    }
    
    private void setDefaults() throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("text.unknown", "Unknown text. Please review text.yml");
        map.put("command.usage", "Invalid command. Type /<command> for help.");
        map.put("command.forbidden", "You need a permission to do that.");
        map.put("command.format.alphanumeric", "Alphanumeric values are only accepted (A-Z, 0-9, dashes)");
        map.put("command.format.number", "Please input a valid number.");
        map.put("command.help.example", "Click to see an example.");
        map.put("command.help.run", "Click to execute.");
        map.put("crit.songs", "songs");
        map.put("radio4u.reload.info", "Reload song files from disk.");
        map.put("playlist.info", "Manage your playlist.");
        map.put("playlist.help", "Playlist : manage noteblock song playlist!");
        map.put("playlist.select.info", "Select a playlist to play or modify.");
        map.put("playlist.select.succeed", "Selected playlist: ");
        map.put("playlist.select.absent", "That playlist does not exist.");
        map.put("playlist.empty", "This playlist is empty.");
        map.put("playlist.absent", "Select a playlist in advance.");
        map.put("playlist.absent.global", "No song is available yet.");
        map.put("playlist.define.name", "Please input the name of playlist.");
        map.put("playlist.define.operation", "Operation is missing: add / remove / clear");
        map.put("playlist.define.song", "Please input song id (Multiple IDs allowed)");
        map.put("playlist.play.info", "Play the selected playlist.");
        map.put("playlist.play.how2pause", "You can pause the music with /radio pause");
        map.put("playlist.list.info", "Print the list of playlists.");
        map.put("playlist.list.header", "Tracklist of %s");
        map.put("playlist.list.exceed", "You exceed the last page of %d.");
        map.put("playlist.list.jump", "Type \"/playlist tracklist <page>\" to jump to the page.");
        map.put("playlist.show.info", "Print the list of songs in this playlist.");
        map.put("playlist.create.info", "Create a playlist.");
        map.put("playlist.create.succeed", "Playlist %s has been created.");
        map.put("playlist.create.duplicated", "That playlist already exists.");
        map.put("playlist.remove.info", "Remove a playlist");
        map.put("playlist.remove.succeed", "Playlist %s has been removed.");
        map.put("playlist.remove.failed", "Playlist can not be removed or is absent.");
        map.put("playlist.song.operation.info", "Add or remove songs from the playlist.");
        map.put("playlist.song.operation.failed", "Failed to modify playlist.");
        map.put("playlist.song.operation.unknown", "Unknown operation: %s");
        map.put("playlist.song.add.succeed", "Added %d song(s) into playlist: %s");
        map.put("playlist.song.add.fail_count", "%d song(s) could not be added.");
        map.put("playlist.song.remove.succeed", "Removed %d songs from playlist: %s");
        map.put("playlist.song.clear.succeed", "Cleared %d songs.");
        map.put("playlist.song.id", "Song ID: ");
        map.put("playlist.song.author", "Author: ");
        map.put("playlist.song.length", "Length: %1$dmin %2$dsec");
        map.put("playlist.song.unknown", "unknown song");
        map.put("playlist.song.absent", "The song (%d) does not exist in current playlist.");
        map.put("radio.info", "Broadcast music through a radio channel.");
        map.put("radio.help.header", "Radio Commands Help");
        map.put("radio.open.info", "Create a channel.");
        map.put("radio.open.succeed", "You opened a channel. Type \"/radio play\" to play music.");
        map.put("radio.open.duplicated", "That channel already exists.");
        map.put("radio.close.info", "Delete a channel.");
        map.put("radio.close.succeed", "Channel \"%s\" has been closed.");
        map.put("radio.join.info", "Join a channel.");
        map.put("radio.join.moving", "Leaving previous channel...");
        map.put("radio.join.succeed", "You joined a channel: %s");
        map.put("radio.leave.info", "Leave current channel.");
        map.put("radio.leave.succeed", "You left a channel: %s");
        map.put("radio.playlist.info", "Set playlist of current channel.");
        map.put("radio.playlist.set", "Set playlist \"%1$s\" to channel \"%2$s\".");
        map.put("radio.playlist.shuffled", "Playlist shuffled in channel \"%s\".");
        map.put("radio.play.info", "Start playing music.");
        map.put("radio.play.is-playing", "Radio is already playing music.");
        map.put("radio.play.failed", "Failed to play radio.");
        map.put("radio.pause.info", "Pause music in this channel.");
        map.put("radio.pause.is-paused", "Radio is already paused.");
        map.put("radio.skip.info", "Skip to next music.");
        map.put("radio.skip.is-paused", "Radio is not playing yet.");
        map.put("radio.volume.info", "Adjust volume in this channel.");
        map.put("radio.volume.current", "Volume: %d");
        map.put("radio.volume.set", "Volume set to %d for channel \"%s\".");
        map.put("radio.live.info", "Switch live-mode of this channel.");
        map.put("radio.live.local-unsupported", "Local channel is not supported. Open a channel: /radio create");
        map.put("radio.live.target", "Target a Note Block while running this command.");
        map.put("radio.live.wrong-target", "This is not a Note Block!");
        map.put("radio.live.on", "The channel is live now!");
        map.put("radio.live.off", "The channel is no longer live.");
        map.put("radio.live.failed", "Failed to switch live-mode.");
        map.put("radio.status.now-playing", "%1$s - Playing: %2$s");
        map.put("radio.status.idle", "%s - Idle");
        map.put("radio.list.info", "List all channels available.");
        map.put("radio.list.empty", "No channel is available.");
        map.put("radio.not-in", "You need to be in a channel.");
        map.put("radio.query.no-result", "Unable to find channel: %s");
        map.put("radio.define.name", "Please input the name of channel.");
        map.put("radio.global.absent", "No song is available yet.");
        
        this.config.addDefaults(map);
        this.config.save(this.file);
    }
    
}
