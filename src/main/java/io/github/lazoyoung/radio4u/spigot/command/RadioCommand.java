package io.github.lazoyoung.radio4u.spigot.command;

import com.xxmicloxx.NoteBlockAPI.model.Song;
import io.github.lazoyoung.radio4u.spigot.Playlist;
import io.github.lazoyoung.radio4u.spigot.Radio4Spigot;
import io.github.lazoyoung.radio4u.spigot.Util;
import io.github.lazoyoung.radio4u.spigot.radio.Radio;
import io.github.lazoyoung.radio4u.spigot.radio.RadioListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.annotation.command.Command;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Command(name = "radio", desc = "Play songs in a radio channel.", aliases = {"musicplayer", "channel"}, permission = "radio4u.radio")
public class RadioCommand implements CommandExecutor {
    
    private Radio4Spigot plugin;
    
    public RadioCommand(Radio4Spigot plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
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
                    "/radio play [index]\n",
                    "└ Play a song in playlist\n",
                    "/radio live\n",
                    "└ Switch live-mode (Block based radio)\n",
                    "/radio distance [radius]\n",
                    "└ Set distance where players can listen (Live-mode only)\n",
                    "/radio <pause/resume>\n",
                    "/radio volume <0-100>\n",
                    "/radio skip\n",
                    "/radio shuffle\n"
            });
            return true;
        }
        
        String action = args[0].toLowerCase();
        args[0] = action;
        
        switch(action) {
            case "list": // TODO list players in a channel. i.e. /radio list player
                return listChannels(sender);
    
            case "delete":
            case "close":
            case "remove":
                return remove(sender, getSubArg(args));
        }
        
        if(sender instanceof Player) {
            switch (action) {
                case "create":
                case "add":
                case "open":
                    return create(sender, getSubArg(args));
        
                case "join":
                case "enter":
                    return joinChannel(sender, getSubArg(args));
        
                case "quit":
                case "leave":
                case "exit":
                    return quitChannel(sender);
        
                case "playlist":
                    return setPlaylist(sender, getSubArg(args));

                case "live":
                    return switchLive(sender);
                    
                case "distance":
                    return setDistance(sender, getSubArg(args));

                case "volume":
                    return setVolume(sender, getSubArg(args));

                case "play":
                case "resume":
                    return playRadio(sender, args);
        
                case "stop":
                case "pause":
                    return pauseRadio(sender);
        
                case "skip":
                case "next":
                    return skipRadio(sender);

                case "shuffle":
                    return shufflePlaylist(sender);
        
                default:
                    return false;
            }
        } else {
            sender.sendMessage("You cannot use this command!");
            return true;
        }
    }

    private boolean setVolume(CommandSender sender, String arg) {
        Player player = (Player) sender;
        Radio channel = RadioListener.get(player).getChannel();
        byte volume;

        if(channel == null) {
            sender.sendMessage("You need to be in a channel.");
            return true;
        }

        if(arg != null) {
            try {
                volume = Byte.valueOf(arg);
            } catch (NumberFormatException ignored) {
                sender.sendMessage("Only numeric values are accepted.");
                return true;
            }
        }
        else {
            sender.sendMessage("Current volume: " + channel.getVolume());
            return true;
        }

        channel.setVolume(volume);
        sender.sendMessage("Volume set to " + volume + " for channel: " + channel.getName());
        return true;
    }

    private boolean switchLive(CommandSender sender) {
        Player player = (Player) sender;
        Radio channel = RadioListener.get(player).getChannel();
        boolean toLive = true;

        if(channel == null) {
            sender.sendMessage("You need to be in a channel. Open a channel: /radio create");
        }
        else if(channel.isLocal()) {
            sender.sendMessage("Local channel is not supported. Open a channel: /radio create");
        }
        else if(!canApprove(player, "live", channel)) {
            sender.sendMessage(this.plugin.text.get("command.forbidden"));
        }
        else {
            if (channel.isLive()) {
                toLive = false;
            }
    
            Block block = player.getTargetBlock(null, 5);
            String tempName = "#" + player.getName().toLowerCase();
            Radio newChannel;
    
            if (toLive) {
                if (block == null || !block.getType().equals(Material.NOTE_BLOCK)) {
                    sender.sendMessage("Target a Note Block to play live music.");
                    if (block != null && block.getType() != Material.AIR) {
                        Util.actionMessage(player, "This is not a Note Block!");
                    }
                    return true;
                }
                newChannel = Radio.openLiveChannel(plugin, block, tempName, false, channel.getOwnerUUID(), channel.getPlaylist());
            } else {
                newChannel = Radio.openRadioChannel(plugin, tempName, false, channel.getOwnerUUID(), channel.getPlaylist());
            }
    
            if (newChannel != null) {
                for (UUID playerId : channel.getListenerUUIDs()) {
                    Player listener = Bukkit.getPlayer(playerId);
                    if (listener != null) {
                        RadioListener.get(listener).joinChannel(newChannel);
                    }
                }
                channel.closeChannel();
                newChannel.setPlaying(true);
                if (newChannel.rename(channel.getName())) {
                    if (toLive) {
                        sender.sendMessage("The channel is live now!");
                    } else {
                        sender.sendMessage("The channel is no longer live.");
                    }
                    return true;
                }
                Util.debug("Failed to rename a channel.");
            } else {
                Util.debug("Failed to create a reformed channel.");
            }
            sender.sendMessage("Failed to switch channel.");
        }
        return true;
    }
    
    /**
     * @deprecated TODO Distance has nothing to do with audio radius
     */
    @Deprecated
    private boolean setDistance(CommandSender sender, String input) {
        Radio channel = RadioListener.get((Player) sender).getChannel();
        int radius;
        if(channel == null) {
            sender.sendMessage("You need to be in a channel.");
            return true;
        }
        if(channel.isLive()) {
            if (input != null) {
                try {
                    radius = Integer.parseInt(input);
                } catch (NumberFormatException ignored) {
                    sender.sendMessage("Only numeric values are accepted.");
                    return true;
                }
                channel.setDistance(radius);
                sender.sendMessage("Live distance is set to " + radius + " for channel: " + channel.getName());
            }
            else {
                sender.sendMessage("Current live distance: " + channel.getDistance());
            }
            return true;
        }
        sender.sendMessage("This channel is not live.");
        return true;
    }

    private boolean joinChannel(CommandSender sender, String name) {
        if(name == null) {
            sender.sendMessage("Please specify the name of channel.");
            return false;
        }
        Radio channel = Radio.getChannel(name);
        if(channel == null) {
            sender.sendMessage("That channel does not exist.");
        }
        else if(canApprove((Player) sender, "access", channel)) {
            if (!joinChannel(sender, channel, false)) {
                sender.sendMessage("Failed to join channel: " + channel.getName());
            }
            return true;
        }
        sender.sendMessage(this.plugin.text.get("command.forbidden"));
        return true;
    }

    private boolean joinChannel(CommandSender sender, Radio channel, boolean force) {
        RadioListener listener = RadioListener.get((Player) sender);
        Radio preCh = listener.getChannel();
        
        if(channel != null) {
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
            listener.joinChannel(channel);
            sender.sendMessage("You joined a channel: " + channel.getName());
        }
        return true;
    }
    
    private boolean create(CommandSender sender, String name) {
        if(!sender.hasPermission("radio4u.radio.open")) {
            sender.sendMessage(this.plugin.text.get("command.forbidden"));
        }
        else if(name == null) {
            sender.sendMessage("Please input channel name to create.");
        }
        else {
            Radio channel;
            UUID id = null;
            Playlist global = Playlist.getGlobalPlaylist();
            if (global != null) {
                if (sender instanceof Player) {
                    id = ((Player) sender).getUniqueId();
                }
                try {
                    channel = Radio.openRadioChannel(plugin, name, true, id, global);
                } catch (IllegalArgumentException ignored) {
                    sender.sendMessage("Alphanumeric names are only accepted. (A-Z, 0-9, dashes)");
                    return true;
                }
                if (channel != null) {
                    sender.sendMessage("Creating channel...");
                    joinChannel(sender, channel, true);
                    sender.sendMessage("To play music, type /radio play");
                } else {
                    sender.sendMessage("That channel already exists.");
                }
                return true;
            }
            sender.sendMessage("No song is available yet.");
        }
        return true;
    }
    
    private boolean remove(CommandSender sender, String arg) {
        Radio channel = null;
        
        if (arg != null) {
            channel = Radio.getChannel(arg);
            if (channel == null) {
                sender.sendMessage("That channel does not exist.");
                return true;
            }
        }
        else if(sender instanceof Player) {
            channel = RadioListener.get((Player) sender).getChannel();
        }
        
        if (channel == null) {
            sender.sendMessage("You are not in a channel. Specify a channel name.");
        }
        else if(sender instanceof Player && !canApprove((Player) sender, "playlist", channel)) {
            sender.sendMessage(this.plugin.text.get("command.forbidden"));
        }
        else {
            channel.closeChannel();
            sender.sendMessage("Closed the channel: " + channel.getName());
        }
        return true;
    }
    
    private boolean quitChannel(CommandSender sender) {
        RadioListener listener = RadioListener.get((Player) sender);
        Radio channel = listener.getChannel();
        
        if(channel != null) {
            listener.leaveChannel();
            sender.sendMessage("You left a channel: " + channel.getName());
        }
        else {
            sender.sendMessage("You are not in a channel.");
        }
        return true;
    }
    
    private boolean listChannels(CommandSender sender) {
        if(!sender.hasPermission("radio4u.radio.access.others")) {
            sender.sendMessage(this.plugin.text.get("command.forbidden"));
            return true;
        }
        
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
        Player player = (Player) sender;
        Radio channel = RadioListener.get(player).getChannel();
        
        if(channel == null) {
            sender.sendMessage("You need to be in a channel.");
        }
        else if(name == null) {
            sender.sendMessage("Please specify the name of playlist.");
            return false;
        }
        else {
            if (!canApprove(player, "playlist", channel)) {
                sender.sendMessage(this.plugin.text.get("command.forbidden"));
            } else {
                Playlist pl = Playlist.get(name);
                if (pl == null) {
                    sender.sendMessage("That playlist does not exist!");
                    return true;
                }
                channel.setPlaylist(pl);
                sender.sendMessage("Set playlist \'" + name + "\' to channel \'" + channel.getName() + '\'');
            }
        }
        return true;
    }

    private boolean shufflePlaylist(CommandSender sender) {
        Player player = (Player) sender;
        Radio channel = RadioListener.get(player).getChannel();
        
        if(channel == null) {
            sender.sendMessage("You need to be in a channel.");
        }
        else if(!canApprove(player, "shuffle", channel)) {
            sender.sendMessage(this.plugin.text.get("command.forbidden"));
        }
        else {
            sender.sendMessage("Shuffle playlist in channel: " + channel.getName());
            List<Song> list = channel.getPlaylist().getSongList();
            Collections.shuffle(list);
            com.xxmicloxx.NoteBlockAPI.model.Playlist playlist
                    = new com.xxmicloxx.NoteBlockAPI.model.Playlist(list.toArray(new Song[0]));
            channel.setPlaylist(playlist);
            channel.play(0);
        }
        return true;
    }
    
    private boolean playRadio(CommandSender sender, String[] args) {
        Radio channel = RadioListener.get((Player) sender).getChannel();
        
        if (channel == null) {
            sender.sendMessage("Please join a radio channel: /radio join <channel>");
        }
        else if (channel.isPlaying()) {
            sender.sendMessage("Radio is already playing music.");
        }
        else {
            try {
                if (args[0].equals("play")) {
                    if (channel.playNext(false)) {
                        sender.sendMessage("Playing radio.");
                    }
                    sender.sendMessage("Failed to play radio.");
                    return true;
                }
                channel.setPlaying(true);
                return true;
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                sender.sendMessage("Reached the last song. Repeating playlist...");
        
                if (!channel.play(0)) {
                    sender.sendMessage("The playlist is empty.");
                }
            } catch (NumberFormatException ignored) {
                sender.sendMessage("Please input valid number for song id.");
            }
        }
        return true;
    }
    
    private boolean pauseRadio(CommandSender sender) {
        Player player = (Player) sender;
        Radio channel = RadioListener.get(player).getChannel();
        
        if (channel == null) {
            sender.sendMessage("Please join a radio channel: /radio join <channel>");
        }
        else if (!canApprove(player, "pause", channel)) {
            sender.sendMessage(this.plugin.text.get("command.forbidden"));
        }
        else if (!channel.isPlaying()) {
                sender.sendMessage("This channel is not playing a music.");
        }
        else {
            channel.setPlaying(false);
        }
        return true;
    }
    
    private boolean skipRadio(CommandSender sender) {
        Player player = (Player) sender;
        Radio channel = RadioListener.get(player).getChannel();
        
        if(channel == null) {
            sender.sendMessage("Please join a radio channel: /radio join <channel>");
        }
        else if(!channel.isPlaying()) {
            sender.sendMessage("Please play the music first: /radio play");
        }
        else if(!canApprove(player, "skip", channel)) {
            sender.sendMessage(this.plugin.text.get("command.forbidden"));
        }
        else {
            try {
                if (channel.playNext(true)) {
                    sender.sendMessage("Skipped.");
                    return true;
                }
                sender.sendMessage("Failed to play radio.");
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                sender.sendMessage("Reached the last song. Repeating playlist...");
        
                if (!channel.play(0)) {
                    sender.sendMessage("The playlist is empty.");
                }
            }
        }
        return true;
    }

    private String getSubArg(String[] args) {
        if(args.length > 1) {
            return args[1].toLowerCase();
        }
        return null;
    }
    
    private boolean canApprove(Player player, String action, Radio channel) {
        String node = "radio4u.radio." + action;
        if(channel.getOwnerUUID().equals(player.getUniqueId()) && player.hasPermission(node)) {
            return true;
        }
        return player.hasPermission(node + ".others");
    }
}
