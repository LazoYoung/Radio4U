package io.github.lazoyoung.radio4u.spigot;

import org.bukkit.entity.Player;

import java.io.File;

class Song {
    
    public int id;
    public File file;
    public String name;
    public String desc;
    
    Song(int id, File file, String name, String desc) {
        this.id = id;
        this.file = file;
        this.name = name.toLowerCase();
        this.desc = desc;
    }
    
    public void play(Player player) {
    
    }
    
    public void pause(Player player) {
    
    }
}
