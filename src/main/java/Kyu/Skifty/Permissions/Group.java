package Kyu.Skifty.Permissions;

import Kyu.Skifty.Main;
import Kyu.Skifty.Util.SPlayer;
import Kyu.Skifty.Util.SaveType;
import Kyu.Skifty.Util.Util;
import it.unimi.dsi.fastutil.Hash;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.awt.peer.ScrollPanePeer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group {

    private String name, prefix = null, suffix = null;
    private int weight = 0;
    private List<Group> childs = new ArrayList<>();
    private Map<String, Boolean> perms = new HashMap<>();
    private List<SPlayer> members = new ArrayList<>();

    public Group(String name) {
        this.name = name;
    }

    public void setPermission(String permission, boolean value) {
        perms.put(permission.toLowerCase(), value);
        for (SPlayer p : members) {
            p.updatePermissions();
        }
    }

    public boolean isPermSet(String permission) {
        return perms.containsKey(permission.toLowerCase());
    }

    public void unsetPerm(String permission) {
        perms.remove(permission.toLowerCase());
        for (SPlayer p : members) {
            p.updatePermissions();
        }
    }

    public void save() {
        switch (GroupManager.saveType) {
            case YML:
                saveToYML();
                break;
        }
    }

    private void loadFromYML() {
        File file = new File(Main.getInstance().getDataFolder() + "/groups", name + ".yml");
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
        if (conf.get("prefix") != null) {
            prefix = Util.color(conf.getString("prefix"));
        }
        if (conf.get("suffix") != null) {
            suffix = Util.color(conf.getString("suffix"));
        }
        if (conf.get("permissions") == null) {
            return;
        }
        conf.getConfigurationSection("permissions").getKeys(false).forEach(s -> {
            perms.put(s.replace("|", "."), conf.getBoolean("permissions." + s));
        });
    }


    private void saveToYML() {
        File file = new File(Main.getInstance().getDataFolder() + "/groups", name + ".yml");
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
        if (prefix != null) {
            conf.set("prefix", prefix);
        }
        if (suffix != null) {
            conf.set("suffix", suffix);
        }
        conf.set("weight", weight);
        conf.set("permissions", null);
        for (String perm : perms.keySet()) {
            conf.set("permissions." + perm.replace(".", "|"), perms.get(perm));
        }
        try {
            conf.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFromMySQL() {

    }

    private void loadFromSQLite() {

    }

    public String getName() {
        return name;
    }

    public Map<String, Boolean> getPerms() {
        return perms;
    }

    public void addMember(SPlayer p) {
        members.add(p);
    }

    public void removeMember(SPlayer p) {
        members.remove(p);
    }

    public static class GroupManager {

        private static Map<String, Group> groups = new HashMap<>();
        private static SaveType saveType;

        public static void saveGroups() {
            for (Group g : groups.values()) {
                g.save();
            }
        }

        public static void loadGroups() {
            switch (saveType) {
                case YML:
                    File groupsFolder = new File(Main.getInstance().getDataFolder() + "/groups");
                    File defGroup = new File(groupsFolder, "default.yml");
                    if (!defGroup.exists()) {
                        try {
                            defGroup.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    for (File file : groupsFolder.listFiles()) {
                        Group group = new Group(file.getName().split(".yml")[0]);
                        group.loadFromYML();
                        groups.put(group.getName(), group);
                    }
                    break;
            }
        }

        public static Group getGroup(String name) {
            return groups.get(name.toLowerCase());
        }

        public static Group createGroup(String groupName) {
            Group group = new Group(groupName);
            switch (saveType) {
                case YML:
                    createYMLFile(groupName);
                    break;
            }
            groups.put(group.getName(), group);
            return group;
        }


        public static void createYMLFile(String groupName) {
            File file = new File(Main.getInstance().getDataFolder() + "/groups", groupName.toLowerCase() + ".yml");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void createMySQLEntry(String groupName){

        }

        public void createSQLiteEntry(String groupName) {

        }

        public static void setSaveType(SaveType saveType) {
            GroupManager.saveType = saveType;
        }

        public static boolean exists(String name) {
            for (Group group : groups.values()) {
                if (group.getName().equalsIgnoreCase(name)) return true;
            }
            return false;
        }
    }
}
