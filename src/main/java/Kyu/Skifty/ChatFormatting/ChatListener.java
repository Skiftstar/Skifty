package Kyu.Skifty.ChatFormatting;

import Kyu.Skifty.Main;
import Kyu.Skifty.Permissions.Group;
import Kyu.Skifty.Util.SPlayer;
import Kyu.Skifty.Util.Util;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    String format;

    public ChatListener(Main plugin) {
        format = Main.getInstance().getConfig().getString("ChatFormatting.format");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onChat(AsyncChatEvent e) {
        e.setCancelled(true);
        Player p = e.getPlayer();
        SPlayer sp = SPlayer.SPManager.getPlayer(p);
        String mess = e.message().toString();
        String newMess = format;
        if (Main.getInstance().permsEnabled) {
            Group group = sp.getGroup();
            newMess = newMess.replace("%prefix", group.getPrefix());
            newMess = newMess.replace("%suffix", group.getSuffix());
        }
        newMess = newMess.replace("%disp", p.displayName().toString());
        newMess = newMess.replace("%name", p.getName());
        newMess = newMess.replace("%mess", mess);
        Bukkit.broadcast(new TextComponent(Util.color(newMess)));
    }

}
