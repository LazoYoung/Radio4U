package io.github.lazoyoung.radio4u.spigot.command;

import com.xxmicloxx.NoteBlockAPI.model.Song;
import io.github.lazoyoung.radio4u.spigot.Playlist;
import io.github.lazoyoung.radio4u.spigot.Radio4Spigot;
import io.github.lazoyoung.radio4u.spigot.Text;
import io.github.lazoyoung.radio4u.spigot.Util;
import io.github.lazoyoung.radio4u.spigot.radio.Radio;
import io.github.lazoyoung.radio4u.spigot.radio.RadioListener;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
    private Text text;
    
    public RadioCommand(Radio4Spigot plugin) {
        this.plugin = plugin;
        this.text = plugin.text;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
            int page = 1;
            String name = sender.getName();
            ListCommand listCommand = new ListCommand("/radio help");
            TextComponent example = new TextComponent("\n" + text.get("command.help.example"));
            TextComponent run = new TextComponent("\n" + text.get("command.help.run"));
            TextComponent open = new TextComponent("/radio open <channel name>");
            TextComponent close = new TextComponent("/radio close [channel name]");
            TextComponent join = new TextComponent("/radio join <channel name>");
            TextComponent leave = new TextComponent("/radio leave");
            TextComponent list = new TextComponent("/radio list [page]");
            TextComponent playlist = new TextComponent("/radio playlist <name>");
            TextComponent play = new TextComponent("/radio play [index]");
            TextComponent live = new TextComponent("/radio live");
            TextComponent pause = new TextComponent("/radio pause");
            TextComponent resume = new TextComponent("/radio resume");
            TextComponent volume = new TextComponent("/radio volume [0~100]");
            TextComponent skip = new TextComponent("/radio skip");
            TextComponent shuffle = new TextComponent("/radio shuffle");
            TextComponent[] cmds = new TextComponent[] {open, close, join, leave, list, playlist, play, live, pause, resume, volume, skip, shuffle};
            example.setColor(ChatColor.GRAY);
            example.setItalic(true);
            run.setColor(ChatColor.GRAY);
            run.setItalic(true);
            open.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text.get("radio.open.info")).append(example).create()));
            open.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/radio open " + name));
            close.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text.get("radio.close.info")).append(example).create()));
            close.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/radio close "));
            join.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text.get("radio.join.info")).append(example).create()));
            join.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/radio join global"));
            leave.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text.get("radio.leave.info")).append(run).create()));
            leave.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/radio leave"));
            list.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text.get("radio.list.info")).append(example).create()));
            list.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/radio list"));
            playlist.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text.get("radio.playlist.info")).append(example).create()));
            playlist.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/radio playlist global"));
            play.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text.get("radio.play.info")).append(example).create()));
            play.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/radio play "));
            live.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text.get("radio.live.info")).append(example).create()));
            live.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/radio live"));
            pause.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text.get("radio.pause.info")).append(run).create()));
            pause.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/radio pause"));
            resume.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text.get("radio.resume.info")).append(run).create()));
            resume.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/radio resume"));
            volume.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text.get("radio.volume.info")).append(example).create()));
            volume.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/radio volume"));
            skip.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text.get("radio.skip.info")).append(run).create()));
            skip.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/radio skip"));
            shuffle.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text.get("radio.shuffle.info")).append(run).create()));
            shuffle.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/radio shuffle"));
            if (args.length > 1) {
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException ignored) {
                    sender.sendMessage(text.get("command.format.number"));
                    return true;
                }
            }
            listCommand.displayListHeader(text.get("radio.help.header"), page, new Double(Math.ceil(cmds.length / 5D)).intValue(), sender);
            for (int i=5*(page-1); i<5*page; i++) {
                if (i<cmds.length) {
                    sender.spigot().sendMessage(cmds[i]);
                }
            }
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
            sender.sendMessage(text.get("radio.not-in"));
            return true;
        }

        if(arg != null) {
            try {
                volume = Byte.valueOf(arg);
            } catch (NumberFormatException ignored) {
                sender.sendMessage(text.get("command.format.number"));
                return true;
            }
        }
        else {
            Util.actionMessage(player, text.get("radio.volume.current", channel.getVolume()));
            return true;
        }

        channel.setVolume(volume);
        sender.sendMessage(text.get("radio.volume.set", volume, channel.getName()));
        return true;
    }

    private boolean switchLive(CommandSender sender) {
        Player player = (Player) sender;
        Radio channel = RadioListener.get(player).getChannel();
        boolean toLive = true;

        if(channel == null) {
            sender.sendMessage(text.get("radio.not-in"));
        }
        else if(channel.isLocal()) {
            sender.sendMessage(text.get("radio.live.local-not-supported"));
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
                    sender.sendMessage(text.get("radio.live.local-unsupported"));
                    if (block != null && block.getType() != Material.AIR) {
                        Util.actionMessage(player, text.get("radio.live.wrong-target"));
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
                        sender.sendMessage(text.get("radio.live.on"));
                    } else {
                        sender.sendMessage(text.get("radio.live.off"));
                    }
                    return true;
                }
                Util.debug("Failed to rename a channel.");
            } else {
                Util.debug("Failed to create a reformed channel.");
            }
            sender.sendMessage(text.get("radio.live.failed"));
        }
        return true;
    }
    
    /**
     * @deprecated Distance has nothing to do with audio radius
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
            sender.sendMessage(text.get("radio.define.name"));
            return false;
        }
        Radio channel = Radio.getChannel(name);
        if(channel == null) {
            sender.sendMessage(text.get("radio.query.no-result", name));
        }
        else if(canApprove((Player) sender, "access", channel)) {
            joinChannel(sender, channel);
            return true;
        }
        sender.sendMessage(this.plugin.text.get("command.forbidden"));
        return true;
    }
    
    private boolean create(CommandSender sender, String name) {
        if(!sender.hasPermission("radio4u.radio.open")) {
            sender.sendMessage(this.plugin.text.get("command.forbidden"));
        }
        else if(name == null) {
            sender.sendMessage(text.get("radio.define.name"));
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
                    sender.sendMessage(text.get("command.format.alphanumeric"));
                    return true;
                }
                if (channel != null) {
                    joinChannel(sender, channel);
                    sender.sendMessage(text.get("radio.open.succeed"));
                } else {
                    sender.sendMessage(text.get("radio.open.duplicated"));
                }
                return true;
            }
            sender.sendMessage(text.get("radio.global.absent"));
        }
        return true;
    }
    
    private boolean remove(CommandSender sender, String arg) {
        Radio channel = null;
        
        if (arg != null) {
            channel = Radio.getChannel(arg);
            if (channel == null) {
                sender.sendMessage(text.get("radio.query.no-result", arg.toLowerCase()));
                return true;
            }
        }
        else if(sender instanceof Player) {
            channel = RadioListener.get((Player) sender).getChannel();
        }
        
        if (channel == null) {
            sender.sendMessage(text.get("radio.define.name"));
        }
        else if(sender instanceof Player && !canApprove((Player) sender, "playlist", channel)) {
            sender.sendMessage(this.plugin.text.get("command.forbidden"));
        }
        else {
            channel.closeChannel();
            sender.sendMessage(text.get("channel.close.succeed", channel.getName()));
        }
        return true;
    }
    
    private void joinChannel(CommandSender sender, Radio channel) {
        RadioListener listener = RadioListener.get((Player) sender);
        Radio preCh = listener.getChannel();
        
        if(channel != null) {
            if(preCh != null) {
                listener.leaveChannel();
                sender.sendMessage(text.get("radio.join.moving"));
            }
            listener.joinChannel(channel);
            sender.sendMessage(text.get("radio.join.succeed", channel.getName()));
        }
    }
    
    private boolean quitChannel(CommandSender sender) {
        RadioListener listener = RadioListener.get((Player) sender);
        Radio channel = listener.getChannel();
        
        if(channel != null) {
            listener.leaveChannel();
            sender.sendMessage(text.get("radio.leave.succeed", channel.getName()));
        }
        else {
            sender.sendMessage(text.get("radio.not-in"));
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
                sender.sendMessage(text.get("radio.status.now-playing", radio.getName(), song.getTitle()));
            }
            else {
                sender.sendMessage(text.get("radio.status.idle", radio.getName()));
            }
        });
        
        if(channels.isEmpty()) {
            sender.sendMessage(text.get("radio.list.empty"));
        }
        
        return true;
    }
    
    private boolean setPlaylist(CommandSender sender, String name) {
        Player player = (Player) sender;
        Radio channel = RadioListener.get(player).getChannel();
        
        if(channel == null) {
            sender.sendMessage(text.get("radio.not-in"));
        }
        else if(name == null) {
            sender.sendMessage(text.get("playlist.define.name"));
            return false;
        }
        else {
            if (!canApprove(player, "playlist", channel)) {
                sender.sendMessage(this.plugin.text.get("command.forbidden"));
            } else {
                Playlist pl = Playlist.get(name);
                if (pl == null) {
                    sender.sendMessage(text.get("playlist.select.absent"));
                    return true;
                }
                channel.setPlaylist(pl);
                sender.sendMessage(text.get("radio.playlist.set", name, channel.getName()));
            }
        }
        return true;
    }

    private boolean shufflePlaylist(CommandSender sender) {
        Player player = (Player) sender;
        Radio channel = RadioListener.get(player).getChannel();
        
        if(channel == null) {
            sender.sendMessage(text.get("radio.not-in"));
        }
        else if(!canApprove(player, "shuffle", channel)) {
            sender.sendMessage(this.plugin.text.get("command.forbidden"));
        }
        else {
            for(UUID playerId : channel.getListenerUUIDs()) {
                Player player1 = Bukkit.getPlayer(playerId);
                if(player1 != null) {
                    player1.sendMessage(text.get("radio.playlist.shuffled", channel.getName()));
                }
            }
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
            sender.sendMessage(text.get("radio.not-in"));
        }
        else if (channel.isPlaying()) {
            sender.sendMessage(text.get("radio.play.is-playing"));
        }
        else {
            try {
                if (args[0].equals("play")) {
                    if (!channel.playNext(false)) {
                        sender.sendMessage(text.get("radio.play.failed"));
                    }
                    return true;
                }
                channel.setPlaying(true);
                return true;
            } catch (IndexOutOfBoundsException ignored) {
                if (!channel.play(0)) {
                    sender.sendMessage(text.get("playlist.empty"));
                }
            } catch (NumberFormatException ignored) {
                sender.sendMessage(text.get("command.format.number"));
            }
        }
        return true;
    }
    
    private boolean pauseRadio(CommandSender sender) {
        Player player = (Player) sender;
        Radio channel = RadioListener.get(player).getChannel();
        
        if (channel == null) {
            sender.sendMessage(text.get("radio.not-in"));
        }
        else if (!canApprove(player, "pause", channel)) {
            sender.sendMessage(this.plugin.text.get("command.forbidden"));
        }
        else if (!channel.isPlaying()) {
                sender.sendMessage(text.get("radio.pause.is-paused"));
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
            sender.sendMessage(text.get("radio.not-in"));
        }
        else if(!channel.isPlaying()) {
            sender.sendMessage(text.get("radio.skip.is-paused"));
        }
        else if(!canApprove(player, "skip", channel)) {
            sender.sendMessage(this.plugin.text.get("command.forbidden"));
        }
        else {
            try {
                if (!channel.playNext(true)) {
                    sender.sendMessage(text.get("radio.play.failed"));
                }
            } catch (IndexOutOfBoundsException e) {
                if (!channel.play(0)) {
                    sender.sendMessage(text.get("playlist.empty"));
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
