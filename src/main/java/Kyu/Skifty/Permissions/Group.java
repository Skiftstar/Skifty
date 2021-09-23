package Kyu.Skifty.Permissions;

import Kyu.Skifty.Main;
import Kyu.Skifty.Util.SaveType;
import Kyu.Skifty.Util.Util;
import it.unimi.dsi.fastutil.Hash;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

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

    public Group(String name) {
        this.name = name;
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
            perms.put(s, conf.getBoolean("permissions." + s));
        });
    }

    private void saveToYML() {

    }

    private void loadFromMySQL() {

    }

    private void loadFromSQLite() {

    }

    public String getName() {
        return name;
    }

    public static class GroupManager {

        private static List<Group> groups = new ArrayList<>();
        private static SaveType saveType;

        public static void loadGroups() {
            switch (saveType) {
                case YML:
                    File groupsFolder = new File(Main.getInstance().getDataFolder() + "/groups");
                    for (File file : groupsFolder.listFiles()) {
                        Group group = new Group(file.getName().split(".yml")[0]);
                        group.loadFromYML();
                    }
                    break;
            }
        }

        public static Group createGroup(String groupName) {
            Group group = new Group(groupName);
            switch (saveType) {
                case YML:
                    createYMLFile(groupName);
                    break;
            }
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
            for (Group group : groups) {
                if (group.getName().equalsIgnoreCase(name)) return true;
            }
            return false;
        }
    }
}
