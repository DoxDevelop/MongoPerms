package mongoperms.bungee;

import com.google.common.collect.Maps;
import lombok.Getter;
import mongoperms.MongoConnection;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.command.ConsoleCommandSender;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class MongoPermsBungee extends Plugin implements Listener {

    @Getter
    private static MongoPermsBungee instance;

    private final Map<ProxiedPlayer, String> PLAYERS_IN_GROUPS = Maps.newLinkedHashMap();
    private final Map<String, List<String>> PERMISSIONS_OF_GROUPS = Maps.newLinkedHashMap();

    @Getter
    private static Configuration settings;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfigIfNotExists();

        settings = Configuration.load(this);

        getProxy().getScheduler().runAsync(this, () -> MongoConnection.load(settings.getMongoHost(), settings.getMongoPort(), settings.getDefaultGroup())); //Need to run async because of BungeeCord Security Manager

        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getPluginManager().registerCommand(this, new PermissionsCommand());

    }

    public void reloadPlayer(final ProxiedPlayer p) {
        getProxy().getScheduler().runAsync(this, () -> {
            synchronized (PLAYERS_IN_GROUPS) {
                PLAYERS_IN_GROUPS.remove(p);
                PLAYERS_IN_GROUPS.put(p, MongoConnection.getGroup(p.getUniqueId()));
            }
        });
    }

    public String getGroup(ProxiedPlayer p) {
        return PLAYERS_IN_GROUPS.get(p);
    }

    public void reloadGroups() {
        getProxy().getScheduler().runAsync(this, () -> {
            synchronized (PERMISSIONS_OF_GROUPS) {
                PERMISSIONS_OF_GROUPS.clear();
                for (String group : MongoConnection.getGroups()) {
                    PERMISSIONS_OF_GROUPS.put(group, MongoConnection.getPermissions(group));
                }
            }
        });
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent e) {
        ProxiedPlayer p = e.getPlayer();
        MongoConnection.registerPlayer(p.getUniqueId());

        String group = MongoConnection.getGroup(p.getUniqueId());
        PLAYERS_IN_GROUPS.put(p, group);
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

            List<String> permissions = PERMISSIONS_OF_GROUPS.get(PLAYERS_IN_GROUPS.get(p)); //players don't have custom permissions?!

            if (permissions.contains(settings.getPermissionNode()) || permissions.contains(e.getPermission())) {
                e.setHasPermission(true);
            }
        }
    }

    private void saveDefaultConfigIfNotExists() {
        File config = new File(getDataFolder(), "config.yml");

        if (!Files.exists(config.toPath())) {
            try {
                if (!getDataFolder().exists()) {
                    getDataFolder().mkdirs();
                }
                Files.copy(getResourceAsStream("config.yml"), config.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
