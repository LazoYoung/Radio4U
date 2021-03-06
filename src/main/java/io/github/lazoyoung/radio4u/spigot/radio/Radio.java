package io.github.lazoyoung.radio4u.spigot.radio;

import com.xxmicloxx.NoteBlockAPI.model.Playlist;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.model.SoundCategory;
import com.xxmicloxx.NoteBlockAPI.songplayer.NoteBlockSongPlayer;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import com.xxmicloxx.NoteBlockAPI.songplayer.SongPlayer;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;
import io.github.lazoyoung.radio4u.spigot.Radio4Spigot;
import io.github.lazoyoung.radio4u.spigot.Util;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Radio {
    
    public boolean shuffle = true;
    private static HashMap<String, Radio> registry = new HashMap<>();
    private Radio4Spigot plugin;
    private SongPlayer player;
    private List<Integer> songs;
    private String name;
    private boolean local;
    private UUID ownerId;
    
    private Radio(Radio4Spigot plugin, String name, UUID ownerId) {
        this.plugin = plugin;
        this.name = name;
        this.songs = new ArrayList<>();
        this.player = null;
        this.local = false;
        this.ownerId = ownerId;
        
        if(this.ownerId == null) {
            this.ownerId = UUID.randomUUID();
        }
    }
    
    private static Radio openChannel(Radio4Spigot plugin, String name,
                                     boolean strictName, UUID ownerId) throws IllegalArgumentException {
        name = name.toLowerCase();
        if(registry.containsKey(name))
            return null;
        if(strictName && !Util.isAlphaNumeric(name))
            throw new IllegalArgumentException();
    
        Radio channel = new Radio(plugin, name, ownerId);
        Radio.registry.put(name, channel);
        return channel;
    }
    
    public static Radio openRadioChannel(Radio4Spigot plugin, @Nonnull String name,
                                         boolean strictName, @Nullable UUID ownerId, @Nonnull Playlist playlist) throws IllegalArgumentException {
        Radio channel = openChannel(plugin, name, strictName, ownerId);
        if(channel != null) {
            channel.player = new RadioSongPlayer(playlist, SoundCategory.RECORDS);
            channel.setVolume((byte) 50);
            return channel;
        }
        return null;
    }

    public static Radio openLiveChannel(Radio4Spigot plugin, @Nonnull Block block,
                                        @Nonnull String name, boolean strictName, @Nullable UUID ownerId, @Nonnull Playlist playlist) {
        Radio channel = openChannel(plugin, name, strictName, ownerId);
        if(channel != null) {
            channel.player = new NoteBlockSongPlayer(playlist, SoundCategory.RECORDS);
            ((NoteBlockSongPlayer) channel.player).setNoteBlock(block);
            return channel;
        }
        return null;
    }
    
    public static Radio openLocalChannel(Radio4Spigot plugin, @Nonnull Player player, @Nonnull Playlist playlist) throws IllegalArgumentException {
        String name = "#" + player.getName().toLowerCase();
        Radio channel = openChannel(plugin, name, false, player.getUniqueId());
        if(channel != null) {
            channel.player = new RadioSongPlayer(playlist, SoundCategory.RECORDS);
            channel.player.setAutoDestroy(true);
            channel.local = true;
            channel.setVolume((byte) 50);
            RadioListener listener = RadioListener.get(player);
            listener.joinChannel(channel);
            return channel;
        }
        return null;
    }
    
    public static Radio getChannel(String name) {
        return Radio.registry.get(name);
    }
    
    public static List<Radio> getChannels() {
        List<Radio> list = new ArrayList<>();
        Radio.registry.forEach((name, radio) -> list.add(radio));
        return list;
    }
    
    public void closeChannel() {
        if(this.player != null) {
            this.player.destroy();
        }
        Radio.registry.remove(this.getName());
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
    
    public UUID getOwnerUUID() {
        return this.ownerId;
    }
    
    public Set<UUID> getListenerUUIDs() {
        return this.player.getPlayerUUIDs();
    }
    
    public com.xxmicloxx.NoteBlockAPI.model.Playlist getPlaylist() {
        return this.player.getPlaylist();
    }

    public boolean rename(String name) {
        if(!Radio.registry.containsKey(name)) {
            Radio.registry.remove(this.getName());
            Radio.registry.put(name, this);
            this.name = name;
            return true;
        }
        return false;
    }

    public boolean isPlaying() {
        return this.player.isPlaying();
    }
    
    public boolean isLocal() {
        return this.local;
    }

    public boolean isLive() {
        return this.player instanceof NoteBlockSongPlayer;
    }

    public byte getVolume() {
        return this.player.getVolume();
    }
    
    public int getDistance() {
        if(this.player instanceof NoteBlockSongPlayer) {
            return ((NoteBlockSongPlayer) this.player).getDistance();
        }
        return -1;
    }
    
    public void setPlaylist(Playlist playlist) {
        this.player.setPlaylist(playlist);
        refreshSongs();
    }
    
    public void setPlaying(boolean playing) {
        this.player.setPlaying(playing);
        
        if(playing) {
            for(UUID playerId : getListenerUUIDs()) {
                Player player = Bukkit.getPlayer(playerId);
                if(player != null) {
                    Util.actionMessage(player, "Radio music is resumed.");
                }
            }
        }
    }

    public void setVolume(byte volume) {
        this.player.setVolume(volume);
    }
    
    public void setDistance(int distance) {
        if(this.player instanceof NoteBlockSongPlayer) {
            ((NoteBlockSongPlayer) player).setDistance(distance);
        }
    }

    /**
     * @apiNote Do not call this and setPlaylist() simultaneously!
     * @param skip Whether to skip the current song.
     * @return False if this radio failed to play.
     * @throws IndexOutOfBoundsException thrown if radio has reached the last song.
     */
    public boolean playNext(boolean skip) throws IndexOutOfBoundsException {
        if(isPlaying() && !skip) {
            return false;
        }
        
        return playSong(this.player.getPlayedSongIndex() + 1);
    }
    
    /**
     * Play the specific song in the playlist for this radio.
     * @apiNote Do not call this and setPlaylist() simultaneously!
     * @param index index of the song in playlist
     * @return False if playlist is empty or does not have matching song.
     */
    public boolean play(int index) {
        Playlist playlist = getPlaylist();
        if(playlist == null || !playlist.exist(index)) {
            return false;
        }
        return playSong(index);
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

    private void refreshSongs() {
        this.songs.clear();
        
        if(getPlaylist() == null) {
            return;
        }
        
        List<Song> songList = getPlaylist().getSongList();
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            for (Song song : songList) {
                this.songs.add(this.plugin.songRegistry.getSongID(song));
            }
            if (shuffle) {
                Collections.shuffle(this.songs);
            }
        });
    }
    
    /**
     * @apiNote If index matches no song and the radio is in loop mode, it will play the first song in playlist.
     * @param index the index of song in playlist
     * @return true if succeed
     * @throws IndexOutOfBoundsException thrown if index matches no song and the radio is not in loop mode
     */
    private boolean playSong(int index) throws IndexOutOfBoundsException {
        Playlist playlist = getPlaylist();
        
        if(playlist == null) {
            return false;
        }
        
        if(playlist.exist(index)) {
            this.player.setPlaying(true);
            this.player.playSong(index);
            for(UUID playerId : this.player.getPlayerUUIDs()) {
                Player player = Bukkit.getPlayer(playerId);
                if(player != null) {
                    Util.actionMessage(player, "Now playing: " + getSongPlaying().getTitle());
                }
            }
            return true;
        }
        
        if(this.player.isLoop()) {
            if(this.player.getPlayedSongIndex() > 0) {
                return playSong(0);
            }
            return false;
        }
        
        throw new IndexOutOfBoundsException();
    }
    
    @Deprecated
    private void playSongFile(File file) throws FileNotFoundException {
        if(file.exists()) {
            this.player = new RadioSongPlayer(NBSDecoder.parse(file), SoundCategory.RECORDS);
            this.player.setPlaying(true);
            
        }
        else {
            throw new FileNotFoundException("Song file is missing: " + file.getName());
        }

        for(UUID playerId : this.player.getPlayerUUIDs()) {
            Player player = Bukkit.getPlayer(playerId);
            if(player != null) {
                Util.actionMessage(player, "Now playing: " + getSongPlaying().getTitle());
                this.player.addPlayer(player);
            }
        }
    }

}
