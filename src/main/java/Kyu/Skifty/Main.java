package Kyu.Skifty;

import Kyu.Skifty.Essentials.JoinLeaveListener;
import Kyu.Skifty.Language.LangManager;
import Kyu.Skifty.Language.SetLangCMD;
import Kyu.Skifty.Util.SPlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.Reader;

public final class Main extends JavaPlugin {

    public static File playerConfFolder;
    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;
        getConfig().options().copyDefaults(true);
        saveConfig();


        setupPlayerConfFolder();
        //Incase plugin gets reloaded
        SPlayer.SPManager.reloadPlayers();

        setupLang();
        setupEssentials();
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
        // Plugin shutdown logic
    }


}
