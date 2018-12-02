package io.github.lazoyoung.radio4u.spigot.command;

import com.xxmicloxx.NoteBlockAPI.model.Song;
import io.github.lazoyoung.radio4u.spigot.Playlist;
import io.github.lazoyoung.radio4u.spigot.Radio4Spigot;
import io.github.lazoyoung.radio4u.spigot.Text;
import io.github.lazoyoung.radio4u.spigot.Util;
import io.github.lazoyoung.radio4u.spigot.exception.UnsupportedSenderException;
import io.github.lazoyoung.radio4u.spigot.radio.Radio;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.annotation.command.Command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Command(name = "playlist", desc = "Manage nbs music playlists.", permission = "radio4u.playlist")
public class PlaylistCommand implements CommandExecutor {
    
    private Radio4Spigot plugin;
    private Text text;
    
    public PlaylistCommand(Radio4Spigot plugin) {
        this.plugin = plugin;
        this.text = plugin.text;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(new String[] {
                    text.get("playlist.help") + "\n",
                    " \n",
                    "/playlist select <name>\n",
                    "└ " + text.get("playlist.select.info") + "\n",
                    "/playlist play\n",
                    "└ " + text.get("playlist.play.info") + "\n",
                    "/playlist list\n",
                    "└ " + text.get("playlist.list.info") + "\n",
                    "/playlist show [page]\n",
                    "└ " + text.get("playlist.show.info") + "\n",
                    "/playlist <create/remove> <name>\n",
                    "└ " + text.get("playlist.create.info") + "\n",
                    "/playlist song <add/remove/clearall> <id>[,id, ...]\n",
                    "└ " + text.get("playlist.song.add.info") + "\n",
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
        
                case "remove":
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
            sender.sendMessage(text.get("playlist.absent"));
        }
        
        return playlist;
    }
    
    private boolean select(CommandSender sender, String[] args) {
        String name = args[1];
        
        if(!sender.hasPermission("radio4u.playlist.use")) {
            sender.sendMessage(this.plugin.text.get("command.forbidden"));
            return true;
        }
        
        if(name == null) {
            sender.sendMessage(text.get("playlist.define.name"));
            return false;
        }
        
        try {
            if (Playlist.selectPlaylist(sender, Playlist.get(name))) {
                sender.sendMessage(text.get("playlist.select.succeed") + name);
            } else {
                sender.sendMessage(text.get("playlist.select.absent"));
            }
        } catch(UnsupportedSenderException e) {
            e.informSender();
        }
        
        return true;
    }
    
    private boolean play(CommandSender sender) throws UnsupportedSenderException {
        if(!sender.hasPermission("radio4u.playlist.use")) {
            sender.sendMessage(this.plugin.text.get("command.forbidden"));
            return true;
        }
        
        Playlist pl = getSelection(sender);
        if(pl != null) {
            if(sender instanceof ConsoleCommandSender)
                throw new UnsupportedSenderException(sender);
            
            Player player = (Player) sender;
            Radio channel = Radio.openLocalChannel(plugin, player, pl);
            
            if(channel != null) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (channel.play(0)) {
                        sender.sendMessage(text.get("playlist.play.how2pause"));
                    }
                    else {
                        sender.sendMessage(text.get("playlist.absent"));
                    }
                }, 10L);
            }
        }
        return true;
    }
    
    private boolean list(CommandSender sender) {
        if(!sender.hasPermission("radio4u.playlist.use")) {
            sender.sendMessage(this.plugin.text.get("command.forbidden"));
            return true;
        }
        
        for(Playlist playlist : Playlist.getRegistry().values()) {
            String name = playlist.getName();
            int count = playlist.getCount();
            
            if(name.equals("global")) {
                continue;
            }
            
            if(count > 0) {
                sender.sendMessage(name + " - " + count + " " + text.get("crit.songs"));
            }
            else {
                sender.sendMessage(name + " - " + text.get("playlist.empty"));
            }
        }
        
        Playlist global = Playlist.getGlobalPlaylist();
        if(global == null) {
            sender.sendMessage(text.get("playlist.absent.global"));
        }
        else {
            sender.sendMessage("global - " + global.getCount() + " " + text.get("crit.songs"));
        }
        return true;
    }
    
    private boolean tracklist(final CommandSender sender, String[] args) {
        if(!sender.hasPermission("radio4u.playlist.use")) {
            sender.sendMessage(this.plugin.text.get("command.forbidden"));
            return true;
        }
        
        Playlist pl = getSelection(sender);
        if(pl != null) {
            final List<Song> songList = pl.getSongList();
            
            if(songList.isEmpty()) {
                sender.sendMessage(text.get("playlist.empty"));
                return true;
            }
            
            int page;
            int lastPage = (int) Math.ceil(songList.size() / 10D);
            
            if(args.length > 1) {
                page = Integer.parseInt(args[1]);
            } else {
                page = 1;
                Bukkit.getScheduler().runTaskLater(plugin, () -> sender.sendMessage(text.get("playlist.list.jump")), 60L);
            }
            
            try {
                int lastIndex = page * 10;
                List<Song> results = null;

                if(lastIndex > songList.size()) {
                    lastIndex = songList.size();
                }

                try {
                    results = songList.subList((page - 1) * 10, lastIndex);
                } catch (IllegalArgumentException | IndexOutOfBoundsException ignored) {
                    sender.sendMessage(text.get("playlist.list.exceed", lastPage));
                    return true;
                }

                new ListCommand("/playlist show").displayListHeader(text.get("playlist.list.header", pl.getName()), page, lastPage, sender);
                results.forEach(song -> Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    showList(sender, song, plugin.songRegistry.getSongID(song));
                }));
            } catch (NumberFormatException ignored) {
                sender.sendMessage(text.get("command.format.alphanumeric"));
                return false;
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
                        = hover.append(text.get("playlist.song.id") + id + "\n")
                            .append(text.get("playlist.song.author") + author + "\n")
                            .append(text.get("playlist.song.id", min, sec))
                            .create();
                        
                body.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
                body.setText("- " + title);
                sender.spigot().sendMessage(body);
                return;
            }
            
            sender.sendMessage("[" + text.get("playlist.song.id") + id + "] " + song.getTitle());
        } else {
            sender.sendMessage("[" + text.get("playlist.song.id") + id + "] " + text.get("playlist.song.unknown"));
        }
    }
    
    private boolean create(CommandSender sender, String[] args) {
        if(!sender.hasPermission("radio4u.playlist.modify")) {
            sender.sendMessage(this.plugin.text.get("command.forbidden"));
            return true;
        }
        
        if(args.length < 2) {
            sender.sendMessage(text.get("playlist.define.name"));
            return false;
        }
    
        String name = args[1].toLowerCase();
        boolean success;
        
        try {
            success = (Playlist.create(plugin, name) != null);
        } catch(IllegalArgumentException ignored) {
            sender.sendMessage("Alphanumeric names are only accepted. (A-Z, 0-9, dashes)");
            return true;
        }
        
        if(success) {
            sender.sendMessage(text.get("playlist.create.succeed", name));
            try {
                Playlist.selectPlaylist(sender, Playlist.get(name));
            }
            catch (UnsupportedSenderException ignored) {}
        }
        else {
            sender.sendMessage(text.get("playlist.create.duplicated"));
        }
        
        return true;
    }
    
    private boolean remove(CommandSender sender, String[] args) {
        if(!sender.hasPermission("radio4u.playlist.modify")) {
            sender.sendMessage(this.plugin.text.get("command.forbidden"));
            return true;
        }
        
        String name = args[1];
        
        if(name == null) {
            sender.sendMessage(text.get("playlist.define.name"));
            return false;
        }
        
        name = name.toLowerCase();
        boolean success = Playlist.remove(name);
        
        if(success) {
            sender.sendMessage(text.get("playlist.remove.succeed", name));
        }
        else {
            sender.sendMessage(text.get("playlist.remove.failed"));
        }
    
        return true;
    }
    
    private boolean song(final CommandSender sender, String[] args) {
        if(!sender.hasPermission("radio4u.playlist.modify")) {
            sender.sendMessage(this.plugin.text.get("command.forbidden"));
            return true;
        }
        
        String operate = args[1];
        List<Integer> list = new ArrayList<>();
        final Playlist pl = getSelection(sender);
        
        if(pl == null) {
            return true;
        }
        
        if(operate == null) {
            sender.sendMessage(text.get("playlist.define.operation"));
            return false;
        }
        
        operate = operate.toLowerCase();
        
        if(args.length < 3) {
            if(operate.equals("clear") || operate.equals("clearall")) {
                int cnt = pl.getCount();
                try {
                    pl.clearSongs();
                } catch (IOException e) {
                    e.printStackTrace();
                    sender.sendMessage(text.get("playlist.song.operation.failed"));
                    Util.debug("Failed to write changes to playlist file.");
                }
                sender.sendMessage(text.get("playlist.song.clear.succeed", cnt));
                return true;
            }
            
            sender.sendMessage(text.get("playlist.define.song"));
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
                sender.sendMessage(text.get("command.format.number"));
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
                        Util.debug("IOException of the operation with: " + id);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        Util.debug(id + " was not loaded in plugin's memory.");
                    } finally {
                        sender.sendMessage(text.get("playlist.song.operation.failed"));
                    }
                }
                if(fails > 0) {
                    sender.sendMessage(text.get("playlist.song.add.fail_count", fails));
                }
                sender.sendMessage(text.get("playlist.song.add.succeed", count, pl.getName()));
                break;
            case "remove":
                for(int id : list) {
                    try {
                        if(!pl.removeSongFromDisk(id)) {
                            sender.sendMessage(text.get("playlist.song.absent", id));
                        }
                        pl.remove(plugin.songRegistry.getSong(id));
                    } catch (IOException e) {
                        e.printStackTrace();
                        Util.debug("Error occurred while the operation with: " + id);
                        sender.sendMessage(text.get("playlist.song.operation.failed"));
                    }
                }
                sender.sendMessage(text.get("playlist.song.remove.succeed", list.size(), pl.getName()));
                break;
            default:
                sender.sendMessage(text.get("playlist.song.operation.unknown"));
                return false;
        }
        
        return true;
    }
}
