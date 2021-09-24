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

import java.util.Arrays;
import java.util.Map;

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
            if (args.length < 3) {
                sender.sendMessage(lang.getFMessage("NotEnoughArgs"));
                return false;
            }
            handleGroupCommands(Arrays.copyOfRange(args, 1, args.length), lang, sender);
            return true;
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
            sender.sendMessage(lang.getFMessage("GroupCreated", new String[]{"%p", groupName}));
            return true;
        }

        return false;
    }

    private void handleGroupCommands(String[] args, Language lang, CommandSender sender) {
        System.out.println(Arrays.toString(args));
        String groupName = args[0];
        if (!Group.GroupManager.exists(groupName)) {
            sender.sendMessage(lang.getFMessage("NoSuchGroup"));
            return;
        }
        Group group = Group.GroupManager.getGroup(groupName);
        if (args[1].equalsIgnoreCase("perm")) {
            if (args.length < 3) {
                sender.sendMessage(lang.getFMessage("NotEnoughArgs"));
                return;
            }
            handleGroupPerms(Arrays.copyOfRange(args, 2, args.length), lang, sender, group);
        }
    }

    private String formatMessage(Group g, Language lang) {
        StringBuilder header = new StringBuilder(lang.getFMessage("permGroupInfo", new String[]{"%name", g.getName()}));
        Map<String, Boolean> perms = g.getPerms();
        for (String s : perms.keySet()) {
            header.append("\n");
            header.append(lang.getFMessage("permListTemplate", new String[]{"%perm", s}, new String[]{"%val", ""+perms.get(s)}));
        }
        return header.toString();
    }

    private void handleGroupPerms(String[] args, Language lang, CommandSender sender, Group group) {
        if (args[0].equalsIgnoreCase("info")) {
            sender.sendMessage(formatMessage(group, lang));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(lang.getFMessage("NotEnoughArgs"));
            return;
        }

        boolean value = true;
        String permission = args[1];
        if (args[0].equalsIgnoreCase("set")) {
            if (group.isPermSet(permission)) {
                sender.sendMessage(lang.getFMessage("PermAlreadySet"));
                return;
            }
            if (args.length >= 3) {
                if (!args[2].equalsIgnoreCase("false")) {
                    sender.sendMessage(lang.getFMessage("NotAValidValue", new String[]{"%p", args[2]}));
                    return;
                }
                value = false;
            }
            group.setPermission(permission, value);
            sender.sendMessage(lang.getFMessage("PermSetGroup", new String[]{"%perm", permission}, new String[]{"%val", Boolean.toString(value)}, new String[]{"%group", group.getName()}));
            return;
        }
        if (args[0].equalsIgnoreCase("unset")) {
            if (!group.isPermSet(permission)) {
                sender.sendMessage(lang.getFMessage("PermNotSet"));
                return;
            }
            group.unsetPerm(permission);
            sender.sendMessage(lang.getFMessage("PermUnsetGroup", new String[]{"%perm", permission}, new String[]{"%group", group.getName()}));
            return;
        }

    }
}
