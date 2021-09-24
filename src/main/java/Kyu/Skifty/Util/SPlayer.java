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

    /*
    General Player Class for this Plugin
    Holds all the Information needed for this plugin to interact with a player
     */

    private YamlConfiguration conf;
    private File file;
    private Player p;
    private Language lang; //Language the player chose for his chat messages

    //Only used if the permission system is enabled in the config
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

    //Check if the config file for the player exists
    //TODO: Support other saveTypes such as MySQL and SQLite
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

    //loads in the player language from his config
    //if no language is set, it is set to the default lang
    private void setup() {
        if (conf.get("language") != null) {
            lang = LangManager.getLanguage(conf.getString("language"));
        } else if (lang == null || conf.get("language") == null){
            lang = LangManager.getDefaultLang();
            conf.set("language", lang.getName());
            try {
                conf.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Sends a message from the language file to the player
    //Placeholders can be used in the messages
    public void sendPMessage(String s, S... placeholder_fillers) {
        String message = lang.getMessage(s);
        //Replace placeholders with the provided value
        for (S ss : placeholder_fillers) {
            message = message.replace(ss.getS1(), ss.getS2());
        }
        p.sendMessage(Util.color(message));
    }

    //Save important values to config
    //TODO: Support other saveTypes such as MySQL and SQLite
    public void save() {
        conf.set("language", lang.getName());
        try {
            conf.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    ==========================================
       Permission System specific Methods
    =========================================
     */

    //Only called when the permission system is enabled in the config
    //Loads in the group and permissions from the config and sets them in the PermissionsAttachment
    public void permSetup() {
        permAttachment = p.addAttachment(Main.getInstance());
        if (conf.get("group") == null) {
            setGroup(Group.GroupManager.getGroup("default"));
        } else {
            setGroup(Group.GroupManager.getGroup(conf.getString("group")));
        }
        //Player perms are loaded in after group perms
        //Because player perms are supposed to override any group perms
        loadGroupPerms();
        conf.getConfigurationSection("permissions").getKeys(false).forEach(s -> {
            perms.put(s.replace("|", "."), conf.getBoolean("permissions." + s));
            permAttachment.setPermission(s.replace("|", "."), perms.get(s.replace("|", ".")));
        });
    }

    public void setPermissions(String perm, boolean val) {
        perms.put(perm, val);
        permAttachment.setPermission(perm, val);
    }

    public void unsetPermission(String perm) {
        perms.remove(perm);
        //Using updatePermissions instead of just removing the perm from the Attachment
        //Because the group may have this permission set as well
        //So removing from player should not instantly remove it from the attachment
        updatePermissions();
    }

    //Loads in the permissions from the group the player is in
    private void loadGroupPerms() {
        Map<String, Boolean> perms = group.getPerms();
        for (String s : perms.keySet()) {
            permAttachment.setPermission(s, perms.get(s));
        }
    }

    public void updatePermissions() {
        p.removeAttachment(permAttachment);
        permAttachment = p.addAttachment(Main.getInstance());
        loadGroupPerms();
        for (String s : perms.keySet()) {
            permAttachment.setPermission(s, perms.get(s));
        }
    }

    /*
    =========================================
            Setters and Getters
    =========================================
     */

    public void setLang(Language lang) {
        this.lang = lang;
    }

    public Language getLang() {
        return lang;
    }

    public void setGroup(Group group) {
        if (group != null) {
            group.removeMember(this);
        }
        this.group = group;
        group.addMember(this);
        updatePermissions();
    }

    public Group getGroup() {
        return group;
    }

    /*
    =========================================
    =========================================
     */

    public static class SPManager {

        private static Map<Player, SPlayer> players = new HashMap<>();

        //Used after server reload to load in players not caught by the JoinEvent
        public static void reloadPlayers() {
            players.clear();
            for (Player p : Bukkit.getOnlinePlayers()) {
                new SPlayer(p);
            }
        }

        public static void removePlayer(Player p) {
            SPlayer sp = players.get(p);
            if (sp.getGroup() != null) {
                //Player is removed from group because offline players are not needed in the group memberlist
                sp.getGroup().removeMember(sp);
            }
            players.remove(p);
        }

        public static void savePlayerData() {
            for (SPlayer p : players.values()) {
                p.save();
            }
        }

        public static SPlayer getPlayer(Player p) {
            return players.get(p);
        }
    }
}
