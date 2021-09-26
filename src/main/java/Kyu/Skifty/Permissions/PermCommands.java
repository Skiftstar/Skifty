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
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/*
This class handles all the commands that have to do with the permissions system
 */

public class PermCommands implements CommandExecutor, TabCompleter {

    private Map<String, List<String>> contextMenu = new HashMap<>();

    public PermCommands(Main plugin) {
        plugin.getCommand("perm").setExecutor(this);
        fillContextMenu();
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
            sender.sendMessage(lang.getFMessage("UserGroupSet", new S("%p", p.getName()), new S("%group", group.getName())));
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

    private void handleGroupParentCommand(String[] args, Language lang, CommandSender sender, Group group) {
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
    private void handleGroupPrefixSuffixCommand(String[] args, Language lang, CommandSender sender, Group group, String type) {
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


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            return startsWith(args[0], contextMenu.get(""));
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("group")) {
            return startsWith(args[1], Group.GroupManager.getGroupNames());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("group")) {
            return startsWith(args[2], contextMenu.get("group"));
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("group") && (args[2].equalsIgnoreCase("prefix") || args[2].equalsIgnoreCase("suffix"))) {
            return startsWith(args[3], contextMenu.get("prefix"));
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("group") && args[2].equalsIgnoreCase("perm")) {
            return startsWith(args[3], contextMenu.get("perm"));
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("group") && args[2].equalsIgnoreCase("parent")) {
            return startsWith(args[3], contextMenu.get("parent"));
        }
        if (args.length == 5 && args[0].equalsIgnoreCase("group") && args[2].equalsIgnoreCase("parent") && (args[3].equalsIgnoreCase("set") || args[3].equalsIgnoreCase("remove"))) {
            return startsWith(args[4], remove(Group.GroupManager.getGroupNames(), args[1]));
        }
        if (args.length == 2) {
            return null;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("user")) {
            return startsWith(args[2], contextMenu.get("user"));
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("user") && args[2].equalsIgnoreCase("group")) {
            return startsWith(args[3], Group.GroupManager.getGroupNames());
        }
        return new ArrayList<>();
    }

    private List<String> remove(List<String> list, String toRemove) {
        list.remove(toRemove);
        return list;
    }

    private List<String> startsWith(String argument, List<String> options) {
        List<String> newList = new ArrayList<>();
        for (String s : options) {
            if (s.toLowerCase().startsWith(argument.toLowerCase())) {
                newList.add(s);
            }
        }
        return newList;
    }

    private void fillContextMenu() {
        List<String> defComms = new ArrayList<>(Arrays.asList("createGroup", "user", "group", "help"));
        contextMenu.put("", defComms);
        List<String> userComms = new ArrayList<>(Arrays.asList("group", "perm"));
        contextMenu.put("user", userComms);
        List<String> groupComms = new ArrayList<>(Arrays.asList("prefix", "perm", "setWeight", "parent"));
        contextMenu.put("group", groupComms);
        List<String> parentComms = new ArrayList<>(Arrays.asList("clear", "info", "add", "remove"));
        contextMenu.put("parent", parentComms);
        List<String> permComms = new ArrayList<>(Arrays.asList("set", "unset", "info"));
        contextMenu.put("perm", permComms);
        List<String> prefixComms = new ArrayList<>(Arrays.asList("set", "remove"));
        contextMenu.put("prefix", prefixComms);
        List<String> userParentComms = new ArrayList<>(Arrays.asList("set", "add", "remove", "info"));
        contextMenu.put("userGroup", userParentComms);
    }
}
