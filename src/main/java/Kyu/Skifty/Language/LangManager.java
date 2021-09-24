package Kyu.Skifty.Language;

import Kyu.Skifty.Main;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/*
This class handles loading the messages on startup
and gives info about languages to the other classes
 */

public class LangManager {

    private static Map<String, Language> langs = new HashMap<>();
    private static Language defaultLang;

    public static void setup(String defaultLang, Main plugin) {


        File langFolder = new File(plugin.getDataFolder() + "/lang");
        if (!langFolder.exists()) langFolder.mkdir();

        //Create the language file from the resource
        File file = new File (langFolder, "en.yml");
        if (!file.exists()) {
            try {
                Files.copy(plugin.getResource("en.yml"), new File(langFolder, "en.yml").toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } //If file does exist already, update with new messages
        else {
            YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
            YamlConfiguration neww = YamlConfiguration.loadConfiguration(plugin.getTextRessource("en.yml"));
            for (String section : neww.getKeys(false)) {
                for (String id : neww.getConfigurationSection(section).getKeys(false)) {
                    if (conf.get(section + "." + id) == null) {
                        conf.set(section + "." + id, neww.getString(section + "." + id));
                    }
                }
            }
            try {
                conf.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File defFile = new File(langFolder, defaultLang + ".yml");
        if (!defFile.exists()) {
            defFile =  new File(langFolder, "en.yml");
        }

        for (File f : langFolder.listFiles()) {
            Language lang = new Language(f);
            langs.put(lang.getName(), lang);
            if (f.equals(defFile)) LangManager.defaultLang = lang;
        }
    }

    public static Language getDefaultLang() {
        return defaultLang;
    }

    public static Language getLanguage(String name) {
        return langs.get(name);
    }

}
