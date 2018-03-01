package io.github.lazoyoung.radio4u.spigot;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    
    public static boolean isAlphaNumeric(String str) {
        Pattern p = Pattern.compile("\\W");
        Matcher m = p.matcher(str);
        return !m.find();
    }
    
    public static boolean isSpigot() {
        return Bukkit.getServer().getVersion().contains("Spigot");
    }
    
}
