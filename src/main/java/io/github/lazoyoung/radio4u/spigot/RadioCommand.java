package io.github.lazoyoung.radio4u.spigot;

import com.xxmicloxx.NoteBlockAPI.model.Song;
import io.github.lazoyoung.radio4u.spigot.radio.Radio;
import io.github.lazoyoung.radio4u.spigot.radio.RadioListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
                    "/radio <open/close> <channel-name>\n",
                    "└ Create or delete a channel.\n",
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
                    return joinChannel(sender, Radio.getChannel(args[1].toLowerCase()), false);
        
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
    
    private boolean joinChannel(CommandSender sender, Radio channel, boolean force) {
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
        
        if(channel != null) {
            listener.joinChannel(channel);
            sender.sendMessage("You joined radio channel: " + channel.getName());
            return true;
        }
        
        sender.sendMessage("That channel does not exists.");
        return true;
    }
    
    private boolean create(CommandSender sender, String name) {
        if(name == null) {
            sender.sendMessage("Please input channel name to create.");
            return false;
        }
        
        Radio channel;
        Playlist global = Playlist.getGlobalPlaylist();
        
        if(global == null) {
            sender.sendMessage("No song is available yet.");
            return true;
        }
        
        try {
            channel = Radio.openRadioChannel(plugin, name, true, global);
        } catch(IllegalArgumentException ignored) {
            sender.sendMessage("Alphanumeric names are only accepted. (A-Z, 0-9, dashes)");
            return true;
        }
        
        if(channel != null) {
            sender.sendMessage("Created channel: " + name);
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
        return true;
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
            if(song != null && radio.isPlaying()) {
                sender.sendMessage(radio.getName() + " - Playing: " + song.getTitle());
            }
            else {
                sender.sendMessage(radio.getName() + " - Idle");
            }
        });
        
        if(channels.isEmpty()) {
            sender.sendMessage("No channel is available.");
        }
        
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
        
        if(channel.isPlaying()) {
            sender.sendMessage("Radio is already playing music.");
            return true;
        }
    
        try {
            if(args[0].equalsIgnoreCase("play")) {
                if (channel.playNext(false)) {
                    sender.sendMessage("Playing radio.");
                    return true;
                }
                sender.sendMessage("Failed to play radio.");
            }
            channel.setPlaying(true);
            sender.sendMessage("Resumed radio.");
            return true;
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            sender.sendMessage("Reached the last song. Repeating playlist...");
            
            if(!channel.play(0)) {
                sender.sendMessage("The playlist is empty.");
            }
        } catch(NumberFormatException ignored) {
            sender.sendMessage("Please input valid number for song id.");
        }
    
        return true;
    }
    
    private boolean pauseRadio(CommandSender sender) {
        Radio channel = RadioListener.get((Player) sender).getChannel();
        
        if(channel == null) {
            sender.sendMessage("Please join a radio channel: /radio join <channel>");
            return true;
        }
        
        if(!channel.isPlaying()) {
            sender.sendMessage("This channel is not playing a music.");
        }
        else {
            channel.setPlaying(false);
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
            if(channel.playNext(true)) {
                sender.sendMessage("Skipped.");
                return true;
            }
            sender.sendMessage("Failed to play radio.");
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            sender.sendMessage("Reached the last song. Repeating playlist...");
    
            if(!channel.play(0)) {
                sender.sendMessage("The playlist is empty.");
            }
        }
        return true;
    }
}
