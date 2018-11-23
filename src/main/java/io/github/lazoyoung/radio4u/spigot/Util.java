package io.github.lazoyoung.radio4u.spigot;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    
    public static final String INVALID_COMMAND = "Invalid command. Type /<command> for help.";
    public static final String PERMISSION_DENIED = "You need a permission to do that.";
    
    public static boolean isAlphaNumeric(String str) {
        Pattern p = Pattern.compile("\\W");
        Matcher m = p.matcher(str);
        return !m.find();
    }
    
    public static boolean isSpigot() {
        String ver = Bukkit.getServer().getVersion();
        return ver.contains("Spigot") || ver.contains("Paper");
    }
    
    public static void actionMessage(Player player, String str) {
        if(isSpigot()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(str));
            return;
        }
        player.sendMessage(str);
    }

    public static void debug(String str) {
        Bukkit.getServer().getConsoleSender().sendMessage("[Radio4U Debug] " + ChatColor.GRAY + str);
    }

}
