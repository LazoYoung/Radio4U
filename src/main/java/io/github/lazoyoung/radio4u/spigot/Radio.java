package io.github.lazoyoung.radio4u.spigot;

import com.xxmicloxx.NoteBlockAPI.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Radio implements Listener {
    
    private static HashMap<String, Radio> registry = new HashMap<>();
    private static HashMap<UUID, String> listener = new HashMap<>();
    private Radio4Spigot plugin;
    private Playlist playlist;
    private SongPlayer songPlayer;
    private List<Song> songs = new ArrayList<>();
    private List<UUID> players = new ArrayList<>();
    private String name;
    private int index = 0;
    private boolean repeat = false;
    
    
    private Radio(Radio4Spigot plugin, String name) {
        this.plugin = plugin;
        this.name = name;
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    
    public static boolean createChannel(Radio4Spigot plugin, String name) {
        name = name.toLowerCase();
    
        if(registry.containsKey(name))
            return false;
        
        registry.put(name, new Radio(plugin, name));
        return true;
    }
    
    public static Radio getChannelByPlayer(Player player) {
        return getChannel(listener.get(player.getUniqueId()));
    }
    
    public static Radio getChannel(String name) {
        return registry.get(name);
    }
    
    public static List<Radio> getChannels() {
        List<Radio> list = new ArrayList<>();
        registry.forEach((name, radio) -> list.add(radio));
        
        return list;
    }
    
    public void join(Player player) {
        if(songPlayer != null) {
            songPlayer.addPlayer(player);
        }
        players.add(player.getUniqueId());
        listener.put(player.getUniqueId(), name);
    }
    
    public void quit(Player player) {
        if(songPlayer != null) {
            songPlayer.removePlayer(player);
        }
        players.remove(player.getUniqueId());
        listener.remove(player.getUniqueId());
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isPlaying() {
        if(songPlayer != null && songPlayer.isPlaying()) {
            return true;
        }
        return false;
    }
    
    public com.xxmicloxx.NoteBlockAPI.Song getSongPlaying() {
        if(songPlayer != null) {
            return songPlayer.getSong();
        }
        return null;
    }
    
    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        updateSongs(true);
    }
    
    public boolean pause() {
        if(isPlaying()) {
            songPlayer.setPlaying(false);
            return true;
        }
        
        return false;
    }
    
    public void resume() {
        if(songPlayer != null) {
            songPlayer.setPlaying(true);
            return;
        }
    
        try {
            playNext(false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
            
            songPlayer.destroy();
        }
        else if(playlist == null) {
            return false;
        }
        
        playSongfile(songs.get(index++).file);
        return true;
    }
    
    /**
     * Play the specific song in the playlist for this radio.
     * @param id the song id
     * @return False if playlist is not defined.
     * @throws NullPointerException thrown if the song is not registered
     * @throws FileNotFoundException thrown if song file is missing
     */
    public boolean play(int id) throws NullPointerException, FileNotFoundException {
        if(playlist == null) {
            return false;
        }
        
        Song song = plugin.songRegistry.getSongInfo(id);
        
        if(song == null) {
            throw new NullPointerException();
        }
        
        playSongfile(song.file);
        return true;
    }
    
    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }
    
    public void updateSongs(boolean shuffle) {
        playlist.getSongs().forEach(id -> songs.add(plugin.songRegistry.getSongInfo(id)));
        index = 0;
        
        if(shuffle) {
            Collections.shuffle(songs);
        }
    }
    
    @EventHandler
    public void onSongEnd(SongEndEvent event) {
        File endSong = event.getSongPlayer().getSong().getPath();
        
        if(!endSong.equals(songPlayer.getSong().getPath())) {
            return;
        }
        
        try {
            if(!songPlayer.isPlaying()) {
                playNext(false);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException ignored) {
            repeatRadio();
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
    
    private void playSongfile(File file) throws FileNotFoundException {
        if(file.exists()) {
            songPlayer = new RadioSongPlayer(NBSDecoder.parse(file), SoundCategory.RECORDS);
            songPlayer.setAutoDestroy(true);
            songPlayer.setPlaying(true);
        }
        else {
            throw new FileNotFoundException("Song file is missing: " + file.getName());
        }
    
        for(UUID id : players) {
            Player player = Bukkit.getPlayer(id);
            if(player != null) {
                player.sendMessage("Now playing: " + getSongPlaying().getTitle());
            }
            songPlayer.addPlayer(player);
        }
    }
    
}
