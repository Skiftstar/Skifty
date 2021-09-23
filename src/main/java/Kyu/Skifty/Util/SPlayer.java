package Kyu.Skifty.Util;

import Kyu.Skifty.Language.LangManager;
import Kyu.Skifty.Language.Language;
import Kyu.Skifty.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SPlayer {

    private Language lang;
    private Player p;
    private YamlConfiguration conf;

    public SPlayer(Player p) {
        this.p = p;
        checkForConf();
        setup();
        SPManager.players.put(p, this);
    }

    private void checkForConf() {
        File file = new File(Main.playerConfFolder, p.getUniqueId() + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        conf = YamlConfiguration.loadConfiguration(file);
    }

    private void setup() {
        if (conf.get("language") != null) {
            lang = LangManager.getLanguage(conf.getString("language"));
        } else if (lang == null || conf.get("language") == null){
            lang = LangManager.getDefaultLang();
            conf.set("language", lang.getName());
        }
    }

    public void sendPMessage(String s, String... placeholder_fillers) {
        String message = lang.getMessage(s);
        for (String s2 : placeholder_fillers) {
            message = message.replaceFirst("%p", s2);
        }
        p.sendMessage(Util.color(message));
    }

    public void setLang(Language lang) {
        this.lang = lang;
    }

    public Language getLang() {
        return lang;
    }

    public static class SPManager {

        private static Map<Player, SPlayer> players = new HashMap<>();

        public static void reloadPlayers() {
            players.clear();
            for (Player p : Bukkit.getOnlinePlayers()) {
                new SPlayer(p);
            }
        }

        public static void removePlayer(Player p) {
            players.remove(p);
        }

        public static SPlayer getPlayer(Player p) {
            return players.get(p);
        }
    }
}
