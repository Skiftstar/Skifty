package Kyu.Skifty.Permissions;

import Kyu.Skifty.Language.LangManager;
import Kyu.Skifty.Language.Language;
import Kyu.Skifty.Main;
import Kyu.Skifty.Util.SPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PermCommands implements CommandExecutor {

    public PermCommands(Main plugin) {
        plugin.getCommand("perm").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        boolean admin = false;
        Language lang;
        if (!(sender instanceof Player)) {
            admin = true;
            lang = LangManager.getDefaultLang();
        } else {
            lang = SPlayer.SPManager.getPlayer((Player) sender).getLang();
        }

        if (args.length < 1) {
            sender.sendMessage(lang.getFMessage("NotEnoughArgs"));
            return false;
        }
        if (args[0].equalsIgnoreCase("group")) {
            handleGroupCommands(args, lang, sender);
        }
        else if (args[0].equalsIgnoreCase("creategroup")) {
            if (args.length < 2) {
                sender.sendMessage(lang.getFMessage("NotEnoughArgs"));
                return false;
            }
            String groupName = args[1];
            if (Group.GroupManager.exists(groupName)) {
                sender.sendMessage(lang.getFMessage("GroupAlreadyExists"));
                return false;
            }
            Group.GroupManager.createGroup(groupName);
            sender.sendMessage(lang.getFMessage("GroupCreated", groupName));
            return true;
        }

        return false;
    }

    private void handleGroupCommands(String[] args, Language lang, CommandSender sender) {
        if (args.length < 3) {
            sender.sendMessage(lang.getFMessage("NotEnoughArgs"));
            return;
        }
        //if (args[])
    }
}
