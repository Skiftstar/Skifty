package Kyu.Skifty.Permissions;

import Kyu.Skifty.Language.LangManager;
import Kyu.Skifty.Language.Language;
import Kyu.Skifty.Main;
import Kyu.Skifty.Util.S;
import Kyu.Skifty.Util.SPlayer;
import Kyu.Skifty.Util.Util;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;

/*
This class handles all the commands that have to do with the permissions system
 */

public class PermCommands implements CommandExecutor {

    public PermCommands(Main plugin) {
        plugin.getCommand("perm").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        //admin bypasses permission checks
        //TODO: permission checks
        boolean admin = false;
        Language lang;
        if (!(sender instanceof Player)) {
            //Console is admin by default, has to have default language for chat messages
            admin = true;
            lang = LangManager.getDefaultLang();
        } else {
            lang = SPlayer.SPManager.getPlayer((Player) sender).getLang();
        }

        if (args.length < 1) {
            sender.sendMessage(lang.getFMessage("NotEnoughArgs"));
            return false;
        }
        //command looks like this: "/perm group"
        if (args[0].equalsIgnoreCase("group")) {
            //there are at least two more args required for the subcommand
            if (args.length < 3) {
                sender.sendMessage(lang.getFMessage("NotEnoughArgs"));
                return false;
            }
            handleGroupCommands(Arrays.copyOfRange(args, 1, args.length), lang, sender);
            return true;
        }

        //command looks like this: "/perm user"
        if (args[0].equalsIgnoreCase("user")) {
            //There are at least two more args required for the subcommand
            if (args.length < 3) {
                sender.sendMessage(lang.getFMessage("NotEnoughArgs"));
                return false;
            }
            handleUserCommands(Arrays.copyOfRange(args, 1, args.length), lang, sender);
            return true;
        }

        //command looks like this: "/perm creategroup"
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
            sender.sendMessage(lang.getFMessage("GroupCreated", new S("%group", groupName)));
            return true;
        }

        return false;
    }

    //Handles every subcommand that has to od with the user
    private void handleUserCommands(String[] args, Language lang, CommandSender sender) {
        String name = args[0];
        Player p = Bukkit.getPlayer(name);
        if (p == null) {
            sender.sendMessage(lang.getFMessage("PlayerNotOnline"));
            return;
        }
        SPlayer sp = SPlayer.SPManager.getPlayer(p);

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
            return;
        }

        sender.sendMessage(lang.getFMessage("NotAValidValue", new S("%val", args[0])));
    }

    //handles all commands that have to do with groups
    private void handleGroupCommands(String[] args, Language lang, CommandSender sender) {
        String groupName = args[0];
        if (!Group.GroupManager.exists(groupName)) {
            sender.sendMessage(lang.getFMessage("NoSuchGroup"));
            return;
        }
        Group group = Group.GroupManager.getGroup(groupName);

        //command looks like this: "/perm group NAME perm"
        if (args[1].equalsIgnoreCase("perm")) {
            if (args.length < 3) {
                sender.sendMessage(lang.getFMessage("NotEnoughArgs"));
                return;
            }
            handleGroupPerms(Arrays.copyOfRange(args, 2, args.length), lang, sender, group);
            return;
        }

        //command looks like this: "/perm group NAME prefix/suffix"
        if (args[1].equalsIgnoreCase("prefix") || args[2].equalsIgnoreCase("suffix")) {
            if (args.length < 3) {
                sender.sendMessage(lang.getFMessage("NotEnoughArgs"));
                return;
            }
            handleGroupPrefixSuffixCommand(Arrays.copyOfRange(args, 2, args.length), lang, sender, group, args[1]);
            return;
        }

        sender.sendMessage(lang.getFMessage("NotAValidValue", new S("%val", args[0])));
    }

    //handles the modification of a groups suffix or prefix
    private void handleGroupPrefixSuffixCommand(String[] args, Language lang, CommandSender sender, Group group, String type) {
        //command looks like this: "/perm group NAME prefix/suffix unset"
        if (args[0].equalsIgnoreCase("unset")) {
            if (type.equalsIgnoreCase("prefix")) group.setPrefix(null);
            else group.setSuffix(null);
            return;
        }

        //command looks like this: "/perm group NAME prefix/suffix set PREFIX/SUFFIX"
        if (args[0].equalsIgnoreCase("set")) {
            StringBuilder string = new StringBuilder(args[1]);
            for (int i = 2; i < args.length; i++) {
                string.append(args[i]);
            }
            if (type.equalsIgnoreCase("prefix")) group.setPrefix(Util.color(string.toString()));
            else group.setSuffix(Util.color(string.toString()));
            return;
        }

        sender.sendMessage(lang.getFMessage("NotAValidValue", new S("%val", args[0])));
    }

    //Handles the modification of a groups permissions
    private void handleGroupPerms(String[] args, Language lang, CommandSender sender, Group group) {
        //shows a list of the groups permissions and their values
        //command looks like this: "/perm group NAME perm info"
        if (args[0].equalsIgnoreCase("info")) {
            sender.sendMessage(formatMessage(group, lang));
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
            if (group.isPermSet(permission)) {
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

            group.setPermission(permission, value);
            sender.sendMessage(lang.getFMessage("PermSetGroup", new S("%perm", permission), new S("%val", Boolean.toString(value)), new S("%group", group.getName())));
            return;
        }

        //command looks like this: "/perm group NAME perm unset PERMISSION"
        if (args[0].equalsIgnoreCase("unset")) {
            if (!group.isPermSet(permission)) {
                sender.sendMessage(lang.getFMessage("PermNotSet"));
                return;
            }
            group.unsetPerm(permission);
            sender.sendMessage(lang.getFMessage("PermUnsetGroup", new S("%perm", permission), new S("%group", group.getName())));
            return;
        }

        sender.sendMessage(lang.getFMessage("NotAValidValue", new S("%val", args[0])));
    }

    //Formats the permission Info message for groups
    private String formatMessage(Group g, Language lang) {
        StringBuilder header = new StringBuilder(lang.getFMessage("permGroupInfo", new S("%name", g.getName())));
        Map<String, Boolean> perms = g.getPerms();
        for (String s : perms.keySet()) {
            header.append("\n");
            header.append(lang.getFMessage("permListTemplate", new S("%perm", s), new S("%val", ""+perms.get(s))));
        }
        return header.toString();
    }
}
