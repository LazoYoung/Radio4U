package io.github.lazoyoung.radio4u.spigot.command;

import io.github.lazoyoung.radio4u.spigot.Radio4Spigot;
import io.github.lazoyoung.radio4u.spigot.Text;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.annotation.command.Command;

@Command(name = "radio4u", desc = "Main command for Radio4U.", aliases = {"radioplugin", "musicplugin"}, permission = "radio4u.plugin")
public class Radio4UCommand implements CommandExecutor {

    private Radio4Spigot plugin;
    private Text text;

    public Radio4UCommand(Radio4Spigot plugin) {
        this.plugin = plugin;
        this.text = plugin.text;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if(!cmd.getName().equals("radio4u")) {
            return true;
        }

        if(args.length > 0) {
            String arg = args[0].toLowerCase();

            switch(arg) {
                case "reload":
                    return reload(sender);

                default:
                    return false;
            }
        }

        TextComponent suffix = new TextComponent("\n" + text.get("command.help.example"));
        TextComponent reload = new TextComponent("/radio4u reload");
        TextComponent radio = new TextComponent("/radio");
        TextComponent playlist = new TextComponent("/playlist");
        reload.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(text.get("radio4u.reload.info")), suffix}));
        reload.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/radio4u reload"));
        radio.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(text.get("radio.info")), suffix}));
        radio.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/radio"));
        playlist.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(text.get("playlist.info")), suffix}));
        playlist.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/playlist"));
        sender.spigot().sendMessage(reload);
        sender.spigot().sendMessage(radio);
        sender.spigot().sendMessage(playlist);
        return true;
    }

    private boolean reload(CommandSender sender) {
        if(sender.hasPermission("radio4u.plugin.reload")) {
            plugin.loadSongs(sender);
        }
        else {
            sender.sendMessage(this.plugin.text.get("command.forbidden"));
        }
        return true;
    }
}
