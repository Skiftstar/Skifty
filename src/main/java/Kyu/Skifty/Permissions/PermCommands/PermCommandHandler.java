package Kyu.Skifty.Permissions.PermCommands;

import Kyu.Skifty.Language.LangManager;
import Kyu.Skifty.Language.Language;
import Kyu.Skifty.Main;
import Kyu.Skifty.Permissions.Group;
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

public class PermCommandHandler implements CommandExecutor, TabCompleter {

    private Map<String, List<String>> contextMenu = new HashMap<>();

    public PermCommandHandler(Main plugin) {
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
            GroupCommands.handleGroupCommands(Arrays.copyOfRange(args, 1, args.length), lang, sender);
            return true;
        }

        //command looks like this: "/perm user"
        if (args[0].equalsIgnoreCase("user")) {
            //There are at least two more args required for the subcommand
            if (args.length < 3) {
                sender.sendMessage(lang.getFMessage("NotEnoughArgs"));
                return false;
            }
            UserCommands.handleUserCommands(Arrays.copyOfRange(args, 1, args.length), lang, sender);
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
        if (args.length == 4 && args[0].equalsIgnoreCase("user") && args[2].equalsIgnoreCase("perm")) {
            return startsWith(args[3], contextMenu.get("perm"));
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
            if (s.toLowerCase().equalsIgnoreCase(argument.toLowerCase())) {
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
