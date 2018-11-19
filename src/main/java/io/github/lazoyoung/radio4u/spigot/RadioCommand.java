package io.github.lazoyoung.radio4u.spigot;

import com.xxmicloxx.NoteBlockAPI.model.Song;
import io.github.lazoyoung.radio4u.spigot.exception.UnsupportedSenderException;
import io.github.lazoyoung.radio4u.spigot.radio.Radio;
import io.github.lazoyoung.radio4u.spigot.radio.RadioListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.FileNotFoundException;
import java.util.List;

public class RadioCommand implements CommandExecutor {
    
    private Radio4Spigot plugin;
    
    
    public RadioCommand(Radio4Spigot plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(new String[] {
                    "Radio : play songs in a radio channel!\n",
                    " \n",
                    "/radio <create/closeChannel> <channel-name>\n",
                    "└ Create or closeChannel channel.\n",
                    "/radio <join/quit> <channel-name>\n",
                    "└ Join or leave the channel.\n",
                    "/radio list\n",
                    "└ List all available radio channels\n",
                    "/radio playlist <name>\n",
                    "└ Set playlist for your channel.\n",
                    "/radio play [songID]\n",
                    "└ Play song in the playlist.\n",
                    "/radio <pause/resume>\n",
                    "└ Pause or resume the radio.\n",
                    "/radio skip\n",
                    "└ Skip to the next song.\n"
            });
            return true;
        }
        
        String action = args[0].toLowerCase();
        
        if(action.equals("list") || action.equals("channels")) {
            return listChannels(sender);
        }
        
        if(sender instanceof Player) {
            switch (action) {
                case "create":
                case "add":
                case "open":
                    return create(sender, args[1]);
        
                case "closeChannel":
                case "delete":
                case "close":
                    return remove(sender);
        
                case "join":
                case "enter":
                    return joinChannel(sender, args[1], false);
        
                case "quit":
                case "leave":
                case "mute":
                    return quitChannel(sender);
        
                case "playlist":
                    return setPlaylist(sender, args[1]);
        
                case "play":
                case "resume":
                    return playRadio(sender, args);
        
                case "stop":
                case "pause":
                    return pauseRadio(sender);
        
                case "skip":
                case "next":
                    return skipRadio(sender);
        
                default:
                    return false;
            }
        } else {
            sender.sendMessage("You cannot use this command!");
            return true;
        }
    }
    
    private boolean joinChannel(CommandSender sender, String name, boolean force) {
        RadioListener listener = RadioListener.get((Player) sender);
        Radio preCh = listener.getChannel();
        
        if(preCh != null) {
            if(force) {
                listener.leaveChannel();
                sender.sendMessage("Leaving previous channel..");
            }
            else {
                sender.sendMessage("Please leave the current channel: /radio quit");
                return true;
            }
        }
        
        Radio channel = Radio.getChannel(name);
        
        if(channel != null) {
            listener.joinChannel(channel);
            sender.sendMessage("You joined radio channel: " + name);
            return true;
        }
        
        sender.sendMessage("That channel does not exists.");
        return true;
    }
    
    private boolean create(CommandSender sender, String channel) {
        if(channel == null) {
            sender.sendMessage("Please input channel name to create.");
            return false;
        }
        
        boolean success;
        
        try {
            success = Radio.openChannel(plugin, channel, false, true);
        } catch(IllegalArgumentException ignored) {
            sender.sendMessage("Alphanumeric names are only accepted. (A-Z, 0-9, dashes)");
            return true;
        }
        
        if(success) {
            sender.sendMessage("Created channel: " + channel);
            sender.sendMessage("To play music, type /radio play");
            joinChannel(sender, channel, true);
        }
        else {
            sender.sendMessage("That channel already exists.");
        }
        return true;
    }
    
    private boolean remove(CommandSender sender) {
        Radio channel = RadioListener.get((Player) sender).getChannel();
        
        if(channel != null) {
            channel.closeChannel();
            sender.sendMessage("Closed the radio channel: " + channel.getName());
            return true;
        }
        
        sender.sendMessage("Please join a channel to be removed.");
        return false;
    }
    
    private boolean quitChannel(CommandSender sender) {
        RadioListener listener = RadioListener.get((Player) sender);
        Radio channel = listener.getChannel();
        
        if(channel != null) {
            listener.leaveChannel();
            sender.sendMessage("You left radio channel: " + channel.getName());
        }
        else {
            sender.sendMessage("You are not in a channel.");
        }
        return true;
    }
    
    private boolean listChannels(CommandSender sender) {
        List<Radio> channels = Radio.getChannels();
        channels.forEach(radio -> {
            Song song = radio.getSongPlaying();
            if(song != null) {
                sender.sendMessage(radio.getName() + " - Playing: " +song.getTitle());
            }
            else {
                sender.sendMessage(radio.getName() + " - Idle");
            }
        });
        
        if(channels.isEmpty()) {
            sender.sendMessage("No channel is available.");
        }
        
        sender.sendMessage("To create a channel: /radio create <channel>");
        return true;
    }
    
    private boolean setPlaylist(CommandSender sender, String name) {
        Radio channel = RadioListener.get((Player) sender).getChannel();
        
        if(channel == null) {
            sender.sendMessage("Please join a radio channel: /radio join <channel>");
            return true;
        }
        
        if(name == null) {
            sender.sendMessage("Please input the name of playlist.");
            return false;
        }
        
        Playlist pl = Playlist.get(name);
        
        if(pl == null) {
            sender.sendMessage("That playlist does not exist!");
            return true;
        }
        
        channel.setPlaylist(pl);
        sender.sendMessage("Set playlist \'" + name + "\' to channel \'" + channel.getName() + '\'');
        return true;
    }
    
    private boolean playRadio(CommandSender sender, String[] args) {
        Radio channel = RadioListener.get((Player) sender).getChannel();
        
        if(channel == null) {
            sender.sendMessage("Please join a radio channel: /radio join <channel>");
            return true;
        }
        
        if(channel.isPlaying() && args.length < 2) {
            sender.sendMessage("The music is already playing.");
            return true;
        }
    
        try {
            boolean success;
            
            if(args.length > 1 && !args[0].equalsIgnoreCase("resume")) {
                int id = Integer.parseInt(args[1]);
                
                if(plugin.songRegistry.getSong(id) == null) {
                    sender.sendMessage(new String[] {
                            "That song is not in this playlist.\n",
                            "To show all songs available: /playlist show"
                    });
                    Playlist.selectPlaylist(sender, channel.getPlaylist());
                    return true;
                }
                
                success = channel.play(id);
            }
            else if(args[0].equalsIgnoreCase("resume")){
                success = channel.resume();
            }
            else {
                success = channel.playNext(false);
            }
            
            if(success) {
                sender.sendMessage("Playing music.");
            }
            else {
                sender.sendMessage("Please define the playlist: /radio playlist <name>");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            sender.sendMessage("Failed to play song: file is missing");
        } catch(NumberFormatException ignored) {
            sender.sendMessage("Please input valid number for song id.");
        } catch (UnsupportedSenderException e) {
            e.informSender();
        }
    
        return true;
    }
    
    private boolean pauseRadio(CommandSender sender) {
        Radio channel = RadioListener.get((Player) sender).getChannel();
        
        if(channel == null) {
            sender.sendMessage("Please join a radio channel: /radio join <channel>");
            return true;
        }
        
        if(!channel.pause()) {
            sender.sendMessage("This channel is not playing a music.");
        }
        else {
            sender.sendMessage("Paused music in channel: " + channel.getName());
        }
        /*
        else if(!channel.isPlaying()){
            boolean success;
            
            try {
                success = channel.resume();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                sender.sendMessage("Failed to play song: file is missing");
                return true;
            }
            
            if(success) {
                sender.sendMessage("Resumed the music in channel: " + channel.getName());
            }
            else {
                sender.sendMessage("Please define a playlist: /radio playlist <name>");
            }
        }
        else {
            sender.sendMessage("The music is already playing.");
        }
        */
        return true;
    }
    
    private boolean skipRadio(CommandSender sender) {
        Radio channel = RadioListener.get((Player) sender).getChannel();
        
        if(channel == null) {
            sender.sendMessage("Please join a radio channel: /radio join <channel>");
            return true;
        }
        
        if(!channel.isPlaying()) {
            sender.sendMessage("Please play the music first: /radio play");
            return true;
        }
    
        try {
            if(!channel.playNext(true)) {
                sender.sendMessage("Please define a playlist: /radio playlist <name>");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            sender.sendMessage("Failed to play music.");
        }
        return true;
    }
}
