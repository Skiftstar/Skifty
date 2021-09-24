package Kyu.Skifty.Language;

import Kyu.Skifty.Util.S;
import Kyu.Skifty.Util.Util;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/*
This class is initialized for every Language in the language folder
 */

public class Language {

    private File file;
    private YamlConfiguration config;
    private String name;
    private Map<String, String> messages = new HashMap<>();

    public Language(File file) {
        this.file = file;
        name = file.getName().split(".yml")[0];
        config = YamlConfiguration.loadConfiguration(file);
        loadMessages();
    }

    //Since the message yml files are structured with sections for better editable, two for loops are required to load messages
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

    //Returns an already formatted message
    public String getFMessage(String messageID, S... filler) {
        String message = messages.get(messageID);
        for (S ls : filler) {
            message = message.replace(ls.getS1(), ls.getS2());
        }
        return Util.color(message);
    }
}
