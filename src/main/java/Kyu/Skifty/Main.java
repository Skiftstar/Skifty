package Kyu.Skifty;

import Kyu.Skifty.Essentials.JoinLeaveListener;
import Kyu.Skifty.Language.LangManager;
import Kyu.Skifty.Language.SetLangCMD;
import Kyu.Skifty.Permissions.Group;
import Kyu.Skifty.Permissions.PermCommands;
import Kyu.Skifty.Util.SPlayer;
import Kyu.Skifty.Util.SaveType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;

/*
Main Class of the Skifty Plugin
This plugin is designed to be an all-in-one Server Plugin
It's highly customizable with the ability to turn off modules you don't need and every important value accessible in the config
Author: Kyu
 */


public final class Main extends JavaPlugin {

    public static File playerConfFolder;
    private static Main instance;
    public boolean permsEnabled = false;

    @Override
    public void onEnable() {
        new TestCommand(this);

        instance = this;
        //Default config is created this way so that comments are saved as well
        File defConf = new File(getDataFolder(), "config.yml");
        if (!defConf.exists()) {
            try {
                Files.copy(getResource("config.yml"), defConf.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        setupPlayerConfFolder();
        setupPermissions();
        setupLang();
        setupEssentials();
        //Incase plugin gets reloaded
        SPlayer.SPManager.reloadPlayers();
    }

    private void setupPermissions() {
        if (!getConfig().getBoolean("Permissions.enabled")) {
            return;
        }
        permsEnabled = true;
        //saveType currently only affects groups, set to YML by default
        SaveType saveType = SaveType.valueOf(getConfig().getString("Permissions.saveType"));
        if (saveType == null) {
            saveType = SaveType.YML;
            getConfig().set("Permissions.saveType", "YML");
        }
        if (saveType.equals(SaveType.YML)) {
            //creates the folder where all the group yml files are located
            File groupsFolder = new File(getDataFolder() + "/groups");
            if (!groupsFolder.exists()) {
                groupsFolder.mkdir();
            }
        }
        Group.GroupManager.setSaveType(saveType);
        Group.GroupManager.loadGroups();
        new PermCommands(this);
    }

    //Creates the folder where all the player yml files are located
    private void setupPlayerConfFolder() {
        playerConfFolder = new File(getDataFolder() + "/players");
        if (!playerConfFolder.exists()) playerConfFolder.mkdir();
    }

    //Loads in default language from config and sets up language system
    private void setupLang() {
        String defaultLang = getConfig().getString("Default Language");
        LangManager.setup(defaultLang, this);
        new SetLangCMD(this);
    }

    //Sets up stuff from the Essentials Module
    private void setupEssentials() {
        new JoinLeaveListener(this);
    }

    public Reader getTextRessource(String s) {
        return getTextResource(s);
    }


    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        SPlayer.SPManager.savePlayerData();
        SPlayer.SPManager.removeAttachments();
        Group.GroupManager.saveGroups();
    }


}
