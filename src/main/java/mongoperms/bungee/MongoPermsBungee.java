package mongoperms.bungee;

import com.google.common.collect.Maps;
import mongoperms.MongoConnection;
import lombok.Getter;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.command.ConsoleCommandSender;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;
import java.util.List;

public class MongoPermsBungee extends Plugin implements Listener {

    @Getter
    private static MongoPermsBungee instance;

    private HashMap<ProxiedPlayer, List<String>> PERMISSIONS = Maps.newLinkedHashMap();

    @Override
    public void onEnable() {

        instance = this;

        getProxy().getScheduler().runAsync(this, MongoConnection::load); //Need to run async because of BungeeCord Security Manager

        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getPluginManager().registerCommand(this, new PermissionsCommand());

    }

    @EventHandler
    public void onPostLogin(PostLoginEvent e) {
        ProxiedPlayer p = e.getPlayer();
        MongoConnection.registerPlayer(p.getUniqueId());

        String group = MongoConnection.getGroup(p.getUniqueId());

        PERMISSIONS.put(p, MongoConnection.getPermissions(group));
    }

    @EventHandler
    public void onPermissionCheck(PermissionCheckEvent e) {
        CommandSender sender = e.getSender();
        if (sender instanceof ConsoleCommandSender) {
            e.setHasPermission(true);
        } else {
            ProxiedPlayer p = (ProxiedPlayer) e.getSender();

            if (e.hasPermission()) {
                return; //TODO shall we remove this?
            }

            List<String> permissions = PERMISSIONS.get(p);

            if (permissions.contains("*")) {
                e.setHasPermission(true);
                return;
            }

            if (permissions.contains(e.getPermission())) {
                e.setHasPermission(true);
            }
        }
    }

}
