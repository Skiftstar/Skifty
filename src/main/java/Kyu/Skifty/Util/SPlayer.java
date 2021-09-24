package Kyu.Skifty.Util;

import Kyu.Skifty.Language.LangManager;
import Kyu.Skifty.Language.Language;
import Kyu.Skifty.Main;
import Kyu.Skifty.Permissions.Group;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SPlayer {

    private Language lang;
    private Player p;
    private YamlConfiguration conf; File file;
    private Group group = null;
    private Map<String, Boolean> perms = new HashMap<>();
    private PermissionAttachment permAttachment;

    public SPlayer(Player p) {
        this.p = p;
        checkForConf();
        setup();
        if (Main.getInstance().permsEnabled) {
            permSetup();
        }
        SPManager.players.put(p, this);
    }

    private void checkForConf() {
        file = new File(Main.playerConfFolder, p.getUniqueId() + ".yml");
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
            p.sendMessage("test");
            lang = LangManager.getDefaultLang();
            conf.set("language", lang.getName());
            try {
                conf.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    public void save() {
        conf.set("language", lang.getName());
        try {
            conf.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void permSetup() {
        permAttachment = p.addAttachment(Main.getInstance());
        if (conf.get("group") == null) {
            setGroup(Group.GroupManager.getGroup("default"));
        } else {
            setGroup(Group.GroupManager.getGroup(conf.getString("group")));
        }
        loadGroupPerms();
        conf.getConfigurationSection("permissions").getKeys(false).forEach(s -> {
            perms.put(s.replace("|", "."), conf.getBoolean("permissions." + s));
            permAttachment.setPermission(s.replace("|", "."), perms.get(s.replace("|", ".")));
        });
    }

    private void loadGroupPerms() {
        Map<String, Boolean> perms = group.getPerms();
        for (String s : perms.keySet()) {
            permAttachment.setPermission(s, perms.get(s));
        }
    }

    public void setPermissions(String perm, boolean val) {
        perms.put(perm, val);
        permAttachment.setPermission(perm, val);
    }

    public void unsetPermission(String perm) {
        perms.remove(perm);
        updatePermissions();
    }

    public void updatePermissions() {
        p.removeAttachment(permAttachment);
        permAttachment = p.addAttachment(Main.getInstance());
        loadGroupPerms();
        for (String s : perms.keySet()) {
            permAttachment.setPermission(s, perms.get(s));
        }
    }

    public void setGroup(Group group) {
        if (group != null) {
            group.removeMember(this);
        }
        this.group = group;
        group.addMember(this);
    }

    public Group getGroup() {
        return group;
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
            SPlayer sp = players.get(p);
            if (sp.getGroup() != null) {
                sp.getGroup().removeMember(sp);
            }
            players.remove(p);
        }

        public static SPlayer getPlayer(Player p) {
            return players.get(p);
        }

        public static void savePlayerData() {
            for (SPlayer p : players.values()) {
                p.save();
            }
        }
    }
}
