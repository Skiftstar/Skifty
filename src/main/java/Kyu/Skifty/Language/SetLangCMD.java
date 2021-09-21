package Kyu.Skifty.Language;

import Kyu.Skifty.Main;
import Kyu.Skifty.Util.SPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetLangCMD implements CommandExecutor {

    public SetLangCMD(Main plugin) {
        plugin.getCommand("language").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Player only!");
            return false;
        }
        Player p = (Player) sender;
        SPlayer sp = SPlayer.SPManager.getPlayer(p);
        if (args.length < 1) {
            sp.sendPMessage("NotEnoughArgs");
            return false;
        }
        Language lang = LangManager.getLanguage(args[0]);
        if (lang == null) {
            sp.sendPMessage("NoSuchLanguage");
            return false;
        }
        sp.setLang(lang);
        sp.sendPMessage("LanguageChanged", lang.getName());
        return true;
    }
}
