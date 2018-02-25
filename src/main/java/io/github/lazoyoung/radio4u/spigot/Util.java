package io.github.lazoyoung.radio4u.spigot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    
    public static boolean isAlphaNumeric(String str) {
        Pattern p = Pattern.compile("\\W");
        Matcher m = p.matcher(str);
        return !m.find();
    }
}
