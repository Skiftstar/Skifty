package Kyu.Skifty.Permissions.PermCommands;

import Kyu.Skifty.Language.Language;
import Kyu.Skifty.Permissions.Group;
import Kyu.Skifty.Util.S;
import Kyu.Skifty.Util.SPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Map;

public class UserCommands {

    //Handles every subcommand that has to od with the user
    public static void handleUserCommands(String[] args, Language lang, CommandSender sender) {
        String name = args[0];
        Player p = Bukkit.getPlayer(name);
        if (p == null) {
            sender.sendMessage(lang.getFMessage("PlayerNotOnline"));
            return;
        }
        SPlayer sp = SPlayer.SPManager.getPlayer(p);

        //used to modify the users permissions
        //command looks like this: "/perm user NAME perm"
        if (args[1].equalsIgnoreCase("perm")) {
            if (args.length < 4) {
                sender.sendMessage(lang.getFMessage("NotEnoughArgs"));
                return;
            }
            handleUserPerms(Arrays.copyOfRange(args, 2, args.length), lang, sender, sp);
        }

        //used to modify the users group
        //command looks like this: "/perm user NAME group"
        if (args[1].equalsIgnoreCase("group")) {
            if (args.length < 3) {
                sender.sendMessage(lang.getFMessage("NotEnoughArgs"));
                return;
            }
            String groupS = args[2];
            Group group = Group.GroupManager.getGroup(groupS);
            if (group == null) {
                sender.sendMessage(lang.getFMessage("NoSuchGroup"));
                return;
            }
            sp.setGroup(group);
            sender.sendMessage(lang.getFMessage("UserGroupSet", new S("%p", p.getName()), new S("%group", group.getName())));
            return;
        }

        sender.sendMessage(lang.getFMessage("NotAValidValue", new S("%val", args[0])));
    }

    //Handles the modification of a groups permissions
    public static void handleUserPerms(String[] args, Language lang, CommandSender sender, SPlayer player) {
        //shows a list of the groups permissions and their values
        //command looks like this: "/perm group NAME perm info"
        if (args[0].equalsIgnoreCase("info")) {
            sender.sendMessage(formatMessage(player, lang));
            return;
        }

        //Additional args required for the other subcommands
        if (args.length < 2) {
            sender.sendMessage(lang.getFMessage("NotEnoughArgs"));
            return;
        }

        //by default permissions will be set to true when assigned to a group
        boolean value = true;
        String permission = args[1];

        //command looks like this: "/perm group NAME perm set PERMISSION (value)"
        if (args[0].equalsIgnoreCase("set")) {
            if (player.isPermSet(permission)) {
                sender.sendMessage(lang.getFMessage("PermAlreadySet"));
                return;
            }

            //false can be added as an additional argument to deny a permission to a group
            if (args.length >= 3) {
                if (!args[2].equalsIgnoreCase("false")) {
                    sender.sendMessage(lang.getFMessage("NotAValidValue", new S("%val", args[2])));
                    return;
                }
                value = false;
            }

            player.setPermission(permission, value);
            sender.sendMessage(lang.getFMessage("PermSetUser", new S("%perm", permission), new S("%val", Boolean.toString(value)), new S("%name", player.getP().getName())));
            return;
        }

        //command looks like this: "/perm group NAME perm unset PERMISSION"
        if (args[0].equalsIgnoreCase("unset")) {
            if (!player.isPermSet(permission)) {
                sender.sendMessage(lang.getFMessage("PermNotSet"));
                return;
            }
            player.unsetPerm(permission);
            sender.sendMessage(lang.getFMessage("PermUnsetUser", new S("%perm", permission), new S("%name", player.getP().getName())));
            return;
        }

        sender.sendMessage(lang.getFMessage("NotAValidValue", new S("%val", args[0])));
    }

    public static String formatMessage(SPlayer p, Language lang) {
        StringBuilder header = new StringBuilder(lang.getFMessage("UserPermInfo", new S("%name", p.getP().getName())));
        Map<String, Boolean> perms = p.getPerms();
        for (String s : perms.keySet()) {
            header.append("\n");
            header.append(lang.getFMessage("permListTemplate", new S("%perm", s), new S("%val", ""+perms.get(s))));
        }
        return header.toString();
    }


}
