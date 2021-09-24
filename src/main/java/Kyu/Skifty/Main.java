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

public final class Main extends JavaPlugin {

    public static File playerConfFolder;
    private static Main instance;
    public boolean permsEnabled = false;

    @Override
    public void onEnable() {
        instance = this;
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
        //Incase plugin gets reloaded
        setupLang();
        setupEssentials();
        SPlayer.SPManager.reloadPlayers();
    }

    private void setupPermissions() {
        if (!getConfig().getBoolean("Permissions.enabled")) {
            return;
        }
        permsEnabled = true;
        SaveType saveType = SaveType.valueOf(getConfig().getString("Permissions.saveType"));
        if (saveType == null) {
            saveType = SaveType.YML;
            getConfig().set("Permissions.saveType", "YML");
        }
        if (saveType.equals(SaveType.YML)) {
            File groupsFolder = new File(getDataFolder() + "/groups");
            if (!groupsFolder.exists()) {
                groupsFolder.mkdir();
            }
        }
        Group.GroupManager.setSaveType(saveType);
        Group.GroupManager.loadGroups();
        new PermCommands(this);
    }

    private void setupPlayerConfFolder() {
        playerConfFolder = new File(getDataFolder() + "/players");
        if (!playerConfFolder.exists()) playerConfFolder.mkdir();
    }

    private void setupLang() {
        String defaultLang = getConfig().getString("Default Language");
        LangManager.setup(defaultLang, this);
        new SetLangCMD(this);
    }

    public Reader getTextRessource(String s) {
        return getTextResource(s);
    }

    private void setupEssentials() {
        new JoinLeaveListener(this);
    }

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        SPlayer.SPManager.savePlayerData();
        Group.GroupManager.saveGroups();
    }


}
