package io.github.lazoyoung.radio4u.spigot;

import com.xxmicloxx.NoteBlockAPI.model.Song;
import io.github.lazoyoung.radio4u.spigot.exception.UnsupportedSenderException;
import io.github.lazoyoung.radio4u.spigot.radio.Radio;
import io.github.lazoyoung.radio4u.spigot.radio.RadioListener;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlaylistCommand implements CommandExecutor {
    
    private Radio4Spigot plugin;
    
    
    public PlaylistCommand(Radio4Spigot plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(new String[] {
                    "Playlist : manage noteblock song playlist!\n",
                    " \n",
                    "/playlist select <name>\n",
                    "└ Select a playlist to play or modify.\n",
                    "/playlist play\n",
                    "└ Play the selected playlist.\n",
                    "/playlist list\n",
                    "└ Print the list of playlists.\n",
                    "/playlist show [page]\n",
                    "└ Print the list of songs in this playlist.\n",
                    "/playlist <create/closeChannel> <name>\n",
                    "└ Make or delete a playlist.\n",
                    "/playlist song <add/closeChannel/clearall> <id>[,id, ...]\n",
                    "└ Add or closeChannel songs from the playlist.\n"
            });
            return true;
        }
        
        String sub = args[0].toLowerCase();
        
        try {
            switch (sub) {
                case "select":
                    return select(sender, args);
        
                case "play":
                    return play(sender);
        
                case "list":
                    return list(sender);
        
                case "info":
                case "show":
                case "track":
                case "tracks":
                case "tracklist":
                    return tracklist(sender, args);
        
                case "create":
                case "add":
                case "make":
                    return create(sender, args);
        
                case "closeChannel":
                case "delete":
                    return remove(sender, args);
        
                case "song":
                    return song(sender, args);
        
                default:
                    return false;
            }
        } catch(UnsupportedSenderException e) {
            e.informSender();
            return true;
        }
    }
    
    private Playlist getSelection(CommandSender sender) {
        Playlist playlist;
        
        if(sender instanceof ConsoleCommandSender) {
            playlist = Playlist.getSelection(Playlist.consoleId);
        }
        else if(sender instanceof Player) {
            playlist = Playlist.getSelection(((Player) sender).getUniqueId());
        }
        else {
            return null;
        }
        
        if(playlist == null) {
            sender.sendMessage("Select a playlist first: /playlist select <name>");
        }
        
        return playlist;
    }
    
    private boolean select(CommandSender sender, String[] args) {
        String name = args[1];
        
        if(name == null) {
            sender.sendMessage("Please input the name of playlist.");
            return false;
        }
        
        try {
            if (Playlist.selectPlaylist(sender, Playlist.get(name))) {
                sender.sendMessage("Selected playlist: " + name);
            } else {
                sender.sendMessage("That playlist does not exist.");
            }
        } catch(UnsupportedSenderException e) {
            e.informSender();
        }
        
        return true;
    }
    
    private boolean play(CommandSender sender) throws UnsupportedSenderException {
        Playlist pl = getSelection(sender);
        
        if(pl != null) {
            if(sender instanceof ConsoleCommandSender)
                throw new UnsupportedSenderException(sender);
            
            Player player = (Player) sender;
            String name = "#" + player.getName().toLowerCase();
            
            if(Radio.createChannel(plugin, name, true, false)) {
                Radio channel = Radio.getChannel(name);
                RadioListener listener = RadioListener.get((Player) sender);
                channel.setPlaylist(pl);
                listener.joinChannel(channel);
    
                try {
                    channel.playNext(false);
                    sender.sendMessage("You can pause the music with /radio pause");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    sender.sendMessage("Failed to play song: file is missing");
                }
            }
        }
        return true;
    }
    
    private boolean list(CommandSender sender) {
        for(Playlist playlist : Playlist.getRegistry().values()) {
            String name = playlist.getName();
            int count = playlist.getSongs(false).size();
            
            if(name.equals("global")) {
                continue;
            }
            
            if(count > 0) {
                sender.sendMessage(name + " - " + count + " songs");
            }
            else {
                sender.sendMessage(name + " - empty playlist");
            }
        }
        
        Playlist global = Playlist.getGlobalPlaylist();
        if(global == null) {
            sender.sendMessage("No song is available in this server yet.");
        }
        else {
            int count = global.getSongs(false).size();
            sender.sendMessage("global - " + count + " songs");
        }
        return true;
    }
    
    private boolean tracklist(final CommandSender sender, String[] args) {
        Playlist pl = getSelection(sender);
        
        if(pl != null) {
            final List<Integer> songList = pl.getSongs(true);
            
            if(songList.isEmpty()) {
                sender.sendMessage("This playlist is empty.");
                return true;
            }
            
            int page;
            int lastPage = (int) Math.ceil(songList.size() / 10);
            
            if(args.length > 1) {
                page = Integer.parseInt(args[1]);
            } else {
                page = 1;
                Bukkit.getScheduler().runTaskLater(plugin, () -> sender.sendMessage("Type \'/playlist tracklist <page>\' to jump to that page."), 60L);
            }
            
            try {
                List<Integer> results = songList.subList((page - 1) * 10, page * 10);
                final SongRegistry registry = plugin.songRegistry;
    
                new ListCommand("/playlist show").displayListHeader("Tracklist of " + pl.getName(), page, lastPage, sender);
                results.forEach(id -> {
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        showList(sender, registry.getSong(id), id);
                    });
                });
            } catch(NumberFormatException ignored) {
                sender.sendMessage("Please input valid number in [page]");
                return false;
            } catch(IndexOutOfBoundsException ignored) {
                sender.sendMessage("Page " + page + " exceeds the maximum of " + lastPage + ".");
            }
        }
        return true;
    }
    
    private void showList(CommandSender sender, Song song, int id) {
        if(song != null) {
            if(Util.isSpigot()) {
                String author = song.getAuthor();
                String title = song.getTitle();
                int length = song.getLength();
                float tempo = song.getSpeed();
                int min = (int) Math.floor(length / (60 * tempo));
                String sec = String.valueOf((int) Math.floor((length % (60 * tempo)) / tempo));
                TextComponent body = new TextComponent();
                ComponentBuilder hover = new ComponentBuilder(title).color(ChatColor.AQUA).append("\n");
                
                if(Integer.parseInt(sec) < 10) {
                    sec = "0" + sec;
                }
                
                if(title.length() > 15) {
                    hover = new ComponentBuilder(title.substring(0, 16)).color(ChatColor.AQUA).append("...\n");
                }
                
                if(author == null || author.isEmpty()) {
                    author = "-";
                }
                
                BaseComponent[] hoverText
                        = hover.append("Song ID: " + id + "\n")
                            .append("Author: " + author + "\n")
                            .append("Length: " + min + ":" + sec)
                            .create();
                        
                body.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
                body.setText("- " + title);
                sender.spigot().sendMessage(body);
                return;
            }
            
            sender.sendMessage(song.getTitle() + " (" + id + ")");
        } else {
            sender.sendMessage("An unknown song (" + id + ")");
        }
    }
    
    private boolean create(CommandSender sender, String[] args) {
        String name = args[1];
        boolean success;
        
        if(name == null) {
            sender.sendMessage("Please input the name of playlist.");
            return false;
        }
        
        try {
            success = (Playlist.create(plugin, name) != null);
        } catch(IllegalArgumentException ignored) {
            sender.sendMessage("Alphanumeric names are only accepted. (A-Z, 0-9, dashes)");
            return true;
        }
        
        if(success) {
            sender.sendMessage("Playlist " + name.toLowerCase() + " has been created.");
            try {
                Playlist.selectPlaylist(sender, Playlist.get(name));
            }
            catch (UnsupportedSenderException ignored) {}
        }
        else {
            sender.sendMessage("That playlist already exists.");
        }
        
        return true;
    }
    
    private boolean remove(CommandSender sender, String[] args) {
        String name = args[1];
        
        if(name == null) {
            sender.sendMessage("Please input the name of playlist.");
            return false;
        }
        
        boolean success = Playlist.remove(name);
        
        if(success) {
            sender.sendMessage("Playlist " + name.toLowerCase() + " has been removed.");
        }
        else {
            sender.sendMessage("That playlist can't be removed or is absent.");
        }
    
        return true;
    }
    
    private boolean song(final CommandSender sender, String[] args) {
        String operate = args[1];
        List<Integer> list = new ArrayList<>();
        final Playlist pl = getSelection(sender);
        
        if(pl == null) {
            return true;
        }
        
        if(operate == null) {
            sender.sendMessage("Operation is missing: add / closeChannel / clearall");
            return false;
        }
        
        operate = operate.toLowerCase();
        
        if(args.length < 3) {
            if(operate.equals("clear") || operate.equals("clearall")) {
                int cnt = pl.getSongs(false).size();
                try {
                    pl.clearSongs();
                } catch (IOException e) {
                    e.printStackTrace();
                    sender.sendMessage("Failed to write changes to playlist file.");
                }
                sender.sendMessage("Cleared " + cnt + " songs.");
                return true;
            }
            
            sender.sendMessage("Please provide the song id. (Multiple inputs are allowed)");
            return false;
        }
        
        if(args.length < 4) {
            try {
                String input = args[2];
                
                if(input.contains(",")) {
                    for(String id : input.split(",")) {
                        list.add(Integer.parseInt(id));
                    }
                }
                else {
                    list.add(Integer.parseInt(input));
                }
            } catch(NumberFormatException e) {
                sender.sendMessage("Please input the valid number for <id>.");
                return false;
            }
        }
    
        switch (operate) {
            case "add":
                int count = 0;
                int fails = 0;
                for (int id : list) {
                    try {
                        if (plugin.songRegistry.getSong(id) != null) {
                            if(pl.addSongIntoDisk(id)) {
                                pl.add(Objects.requireNonNull(plugin.songRegistry.getSong(id)));
                                count++;
                            } else {
                                fails++;
                            }
                        } else {
                            fails++;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        sender.sendMessage("IOException of the operation with: " + id);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        sender.sendMessage(id + " was not loaded in plugin's memory.");
                    }
                }
                if(fails > 0) {
                    sender.sendMessage(fails + " song(s) could not be added.");
                }
                sender.sendMessage("Added " + count + " song(s) into \'" + pl.getName() + "\'.");
                break;
            case "closeChannel":
                for(int id : list) {
                    try {
                        if(!pl.removeSongFromDisk(id)) {
                            sender.sendMessage(id + " is not in the playlist!");
                        }
                        pl.remove(plugin.songRegistry.getSong(id));
                    } catch (IOException e) {
                        e.printStackTrace();
                        sender.sendMessage("Error occurred while the operation with: " + id);
                    }
                }
                sender.sendMessage("Removed " + list.size() + " song(s) from \'" + pl.getName() + "\'.");
                break;
            default:
                sender.sendMessage("Unknown operation: " + operate);
                return false;
        }
        
        return true;
    }
}
