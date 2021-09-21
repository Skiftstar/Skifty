package Kyu.Skifty.Util;

import Kyu.Skifty.Main;
import org.bukkit.ChatColor;

public class Util {

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static void log(String s) {
        Main.getInstance().getServer().getConsoleSender().sendMessage(s);
    }
}
