package Kyu.Skifty.Essentials;

import Kyu.Skifty.Main;
import Kyu.Skifty.Util.SPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinLeaveListener implements Listener {

    public JoinLeaveListener(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        SPlayer sp = new SPlayer(p);
    }

    @EventHandler
    private void onLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        SPlayer.SPManager.getPlayer(p).save();
        SPlayer.SPManager.removePlayer(p);
    }
}
