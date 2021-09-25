package Kyu.Skifty.ChatFormatting;

import Kyu.Skifty.Main;
import Kyu.Skifty.Permissions.Group;
import Kyu.Skifty.Util.SPlayer;
import Kyu.Skifty.Util.Util;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.TextComponent;
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
        TextComponent comp = (TextComponent) e.message();
        String mess = comp.content();
        String newMess = format;
        if (Main.getInstance().permsEnabled) {
            Group group = sp.getGroup();
            newMess = newMess.replace("%prefix", group.getPrefix());
            newMess = newMess.replace("%suffix", group.getSuffix());
        }
        newMess = newMess.replace("%disp", ((TextComponent) p.displayName()).content());
        newMess = newMess.replace("%name", p.getName());
        newMess = newMess.replace("%mess", mess);
        Bukkit.broadcastMessage(Util.color(newMess));
    }

}
