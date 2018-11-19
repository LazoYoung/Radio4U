package io.github.lazoyoung.radio4u.spigot.radio;

import com.xxmicloxx.NoteBlockAPI.event.SongEndEvent;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.model.SoundCategory;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import com.xxmicloxx.NoteBlockAPI.songplayer.SongPlayer;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;
import io.github.lazoyoung.radio4u.spigot.Playlist;
import io.github.lazoyoung.radio4u.spigot.Radio4Spigot;
import io.github.lazoyoung.radio4u.spigot.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Radio implements Listener {
    
    public boolean repeat = false;
    public boolean autoSleep = true;
    private static HashMap<String, Radio> registry = new HashMap<>();
    private Radio4Spigot plugin;
    private Playlist playlist;
    private SongPlayer player;
    private List<Integer> songs = new ArrayList<>();
    private String name;
    private boolean local; // TODO Implement local channel (destroy when no one hears)
    private int index = 0;
    
    private Radio(Radio4Spigot plugin, String name, boolean local) {
        this.plugin = plugin;
        this.name = name;
        this.local = local;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public static boolean openChannel(Radio4Spigot plugin, String name, boolean local, boolean strictName) throws IllegalArgumentException {
        name = name.toLowerCase();
        if(registry.containsKey(name))
            return false;
        if(strictName && !Util.isAlphaNumeric(name))
            throw new IllegalArgumentException();
        registry.put(name, new Radio(plugin, name, local));
        return true;
    }
    
    public static Radio getChannel(String name) {
        return registry.get(name);
    }
    
    public static List<Radio> getChannels() {
        List<Radio> list = new ArrayList<>();
        registry.forEach((name, radio) -> list.add(radio));
        return list;
    }
    
    public void closeChannel() {
        if(this.player != null) {
            this.player.destroy();
        }
        registry.remove(this.getName());
    }
    
    public String getName() {
        return this.name;
    }
    
    public Song getSongPlaying() {
        if(this.player != null) {
            return this.player.getSong();
        }
        return null;
    }
    
    public Playlist getPlaylist() {
        return playlist;
    }
    
    public boolean isPlaying() {
        return this.player != null && this.player.isPlaying();
    }
    
    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        updateSongs(true);
    }
    
    public boolean pause() {
        if(isPlaying()) {
            this.player.setPlaying(false);
            return true;
        }
        return false;
    }
    
    public boolean resume() throws FileNotFoundException {
        if(this.player != null) {
            this.player.setPlaying(true);
            return true;
        }
        return playNext(false);
    }
    
    /**
     * Play the next song in the playlist for this radio.
     * @param skip Whether to skip the current song.
     * @return False if this radio failed to play.
     * @throws FileNotFoundException thrown if the song file is missing
     * @throws IndexOutOfBoundsException thrown if radio has reached the last song.
     */
    public boolean playNext(boolean skip) throws FileNotFoundException, IndexOutOfBoundsException {
        if(isPlaying()) {
            if(!skip) {
                return false;
            }
    
            this.player.destroy();
        }
        else if(this.playlist == null) {
            return false;
        }

        Song song = plugin.songRegistry.getSong(this.songs.get(index++));
        if(song == null) {
            throw new FileNotFoundException();
        }
        playSongFile(song.getPath());
        return true;
    }
    
    /**
     * Play the specific song in the playlist for this radio.
     * @param id ID of the song to play
     * @return False if playlist is not defined.
     * @throws FileNotFoundException thrown if song file is missing
     */
    public boolean play(int id) throws FileNotFoundException {
        if(this.playlist == null) {
            return false;
        }
        if(isPlaying()) {
            this.player.destroy();
        }
        playSongFile(plugin.songRegistry.getSong(id).getPath());
        return true;
    }
    
    @EventHandler
    public void onSongEnd(SongEndEvent event) {
        File endSong = event.getSongPlayer().getSong().getPath();
        if(this.player == null || !endSong.equals(this.player.getSong().getPath())) {
            return;
        }
        try {
            if(!this.player.isPlaying() && !(autoSleep && this.player.getPlayerUUIDs().size() < 1)) {
                playNext(false);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException ignored) {
            repeatRadio();
        }
    }

    void join(Player player) {
        if(this.player != null) {
            this.player.addPlayer(player);
        }
    }

    void quit(Player player) {
        if(this.player != null) {
            this.player.removePlayer(player);
        }
    }

    private void updateSongs(boolean shuffle) {
        this.songs.clear();
        this.songs.addAll(this.playlist.getSongs(true));
        index = 0;

        if(shuffle) {
            Collections.shuffle(this.songs);
        }
    }
    
    private void repeatRadio() {
        if(repeat) {
            updateSongs(true);
            try {
                playNext(false);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void playSongFile(File file) throws FileNotFoundException {
        if(file.exists()) {
            player = new RadioSongPlayer(NBSDecoder.parse(file), SoundCategory.RECORDS);
            player.setPlaying(true);
        }
        else {
            throw new FileNotFoundException("Song file is missing: " + file.getName());
        }

        for(UUID playerId : this.player.getPlayerUUIDs()) {
            Player player = Bukkit.getPlayer(playerId);
            if(player != null) {
                player.sendMessage("Now playing: " + getSongPlaying().getTitle());
                this.player.addPlayer(player);
            }
        }
    }
    
}
