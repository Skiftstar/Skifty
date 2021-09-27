package Kyu.Skifty.Permissions.PermCommands;

import Kyu.Skifty.Language.Language;
import Kyu.Skifty.Permissions.Group;
import Kyu.Skifty.Util.S;
import Kyu.Skifty.Util.Util;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Map;

public class GroupCommands {

    //handles all commands that have to do with groups
    public static void handleGroupCommands(String[] args, Language lang, CommandSender sender) {
        String groupName = args[0];
        if (!Group.GroupManager.exists(groupName)) {
            sender.sendMessage(lang.getFMessage("NoSuchGroup"));
            return;
        }
        Group group = Group.GroupManager.getGroup(groupName);

        //commands looks like this: "/perm group NAME setweight"
        if (args[1].equalsIgnoreCase("setWeight")) {
            if (args.length < 3) {
                int weight;
                try {
                    weight = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(lang.getFMessage("NotANumber", new S("%val", args[2])));
                    return;
                }
                group.setWeight(weight);
            }
        }

        //command looks like this: "/perm group NAME perm"
        if (args[1].equalsIgnoreCase("perm")) {
            if (args.length < 4) {
                sender.sendMessage(lang.getFMessage("NotEnoughArgs"));
                return;
            }
            handleGroupPerms(Arrays.copyOfRange(args, 2, args.length), lang, sender, group);
            return;
        }

        //command looks like this: "/perm group NAME parent"
        if (args[1].equalsIgnoreCase("parent")) {
            if (args.length < 3) {
                sender.sendMessage(lang.getFMessage("NotEnoughArgs"));
                return;
            }
            handleGroupParentCommand(Arrays.copyOfRange(args, 2, args.length), lang, sender, group);
            return;
        }

        //command looks like this: "/perm group NAME prefix/suffix"
        if (args[1].equalsIgnoreCase("prefix") || args[2].equalsIgnoreCase("suffix")) {
            if (args.length < 4) {
                sender.sendMessage(lang.getFMessage("NotEnoughArgs"));
                return;
            }
            handleGroupPrefixSuffixCommand(Arrays.copyOfRange(args, 2, args.length), lang, sender, group, args[1]);
            return;
        }

        sender.sendMessage(lang.getFMessage("NotAValidValue", new S("%val", args[0])));
    }

    public static void handleGroupParentCommand(String[] args, Language lang, CommandSender sender, Group group) {
        if (args[0].equalsIgnoreCase("clear")) {
            group.clearParents();
            sender.sendMessage(lang.getFMessage("GroupParentsCleared", new S("%group", group.getName())));
            return;
        }
        if (args[0].equalsIgnoreCase("info")) {
            StringBuilder mess = new StringBuilder(lang.getFMessage("GroupParentInfo", new S("%group", group.getName())));
            for (Group g : group.getParens()) {
                mess.append("\n");
                mess.append(lang.getFMessage("GroupParentInfoLine", new S("%group", g.getName())));
            }
            sender.sendMessage(mess.toString());
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(lang.getFMessage("NotEnoughArgs"));
            return;
        }
        Group g = Group.GroupManager.getGroup(args[1]);
        if (g == null) {
            sender.sendMessage(lang.getFMessage("NoSuchGroup"));
            return;
        }
        if (args[0].equalsIgnoreCase("add")) {
            if (group.getParens().contains(g)) {
                sender.sendMessage(lang.getFMessage("AlreadySetAsParent", new S("%group1", g.getName()), new S("%group2", group.getName())));
                return;
            }
            group.addParent(g);
            sender.sendMessage(lang.getFMessage("GroupParentAdded", new S("%group1", g.getName()), new S("%group2", group.getName())));
            return;
        }
        if (args[0].equalsIgnoreCase("remove")) {
            if (!group.getParens().contains(g)) {
                sender.sendMessage(lang.getFMessage("NotSetAsParent", new S("%group1", g.getName()), new S("%group2", group.getName())));
                return;
            }
            group.removeParent(g);
            sender.sendMessage(lang.getFMessage("GroupParentRemoved", new S("%group1", g.getName()), new S("%group2", group.getName())));
            return;
        }

    }

    //handles the modification of a groups suffix or prefix
    public static void handleGroupPrefixSuffixCommand(String[] args, Language lang, CommandSender sender, Group group, String type) {
        //command looks like this: "/perm group NAME prefix/suffix unset"
        if (args[0].equalsIgnoreCase("remove")) {
            if (type.equalsIgnoreCase("prefix")) {
                group.setPrefix(null);
                sender.sendMessage(lang.getFMessage("GroupPrefixRemoved", new S("%group", group.getName())));
            }
            else {
                group.setSuffix(null);
                sender.sendMessage(lang.getFMessage("GroupSuffixRemoved", new S("%group", group.getName())));
            }
            return;
        }

        //command looks like this: "/perm group NAME prefix/suffix set PREFIX/SUFFIX"
        if (args[0].equalsIgnoreCase("set")) {
            StringBuilder string = new StringBuilder(args[1]);
            for (int i = 2; i < args.length; i++) {
                string.append(" ");
                string.append(args[i]);
            }
            if (type.equalsIgnoreCase("prefix")) {
                group.setPrefix(Util.color(string.toString()));
                sender.sendMessage(lang.getFMessage("GroupPrefixSet", new S("%group", group.getName()), new S("%pref", Util.color(string.toString()))));
            }
            else {
                group.setSuffix(Util.color(string.toString()));
                sender.sendMessage(lang.getFMessage("GroupSuffixSet", new S("%group", group.getName()), new S("%suff", Util.color(string.toString()))));
            }
            return;
        }

        sender.sendMessage(lang.getFMessage("NotAValidValue", new S("%val", args[0])));
    }

    //Handles the modification of a groups permissions
    public static void handleGroupPerms(String[] args, Language lang, CommandSender sender, Group group) {
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
    public static String formatMessage(Group g, Language lang) {
        StringBuilder header = new StringBuilder(lang.getFMessage("permGroupInfo", new S("%name", g.getName())));
        Map<String, Boolean> perms = g.getPerms();
        for (String s : perms.keySet()) {
            header.append("\n");
            header.append(lang.getFMessage("permListTemplate", new S("%perm", s), new S("%val", ""+perms.get(s))));
        }
        return header.toString();
    }

}
