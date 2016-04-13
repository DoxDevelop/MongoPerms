package mongoperms.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MongoListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST) //we need to run first
    public void onLogin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        MongoPerms.generateAttachment(p);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        MongoPerms.unlogAttachment(p);
    }

}
