package Kyu.Skifty.Language;

import Kyu.Skifty.Util.Util;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Language {

    private String name;
    private File file;
    private YamlConfiguration config;
    private Map<String, String> messages = new HashMap<>();

    public Language(File file) {
        this.file = file;
        name = file.getName().split(".yml")[0];
        config = YamlConfiguration.loadConfiguration(file);
        loadMessages();
    }

    public void loadMessages() {
        messages.clear();
        for (String section : config.getKeys(false)) {
            for (String messageID : config.getConfigurationSection(section).getKeys(false)) {
                messages.put(messageID, config.getString(section + "." + messageID));
            }
        }
    }

    public String getMessage(String messageID) {
        return messages.get(messageID);
    }

    public String getName() {
        return name;
    }

    public String getFMessage(String messageID, String[]... filler) {
        String message = messages.get(messageID);
        for (String[] s2 : filler) {
            message = message.replaceFirst(s2[0], s2[1]);
        }
        return Util.color(message);
    }
}
