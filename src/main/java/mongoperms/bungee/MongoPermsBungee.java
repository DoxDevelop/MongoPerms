package mongoperms.bungee;

import lombok.Getter;
import mongoperms.Configuration;
import mongoperms.Group;
import mongoperms.MongoConnection;
import mongoperms.MongoPermsAPI;
import mongoperms.bungee.commands.PermissionsCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

public class MongoPermsBungee extends Plugin implements Listener {

    @Getter
    private static MongoPermsBungee instance;

    @Getter
    private static Configuration settings;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfigIfNotExists();

        try {
            settings = Configuration.load();
        } catch (IOException e) {
            throw new RuntimeException();
        }

        getProxy().getScheduler().runAsync(this, () -> MongoConnection.load(settings.getMongoHost(), settings.getMongoPort(), settings.getDefaultGroup(), settings.getMongoUsername(), settings.getMongoPassword(), true, settings.isUseAuthentication())); //Need to run async because of BungeeCord Security Manager

        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getPluginManager().registerCommand(this, new PermissionsCommand());

    }

    @Override
    public void onDisable() {
        Group.saveGroups();
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent e) {
        ProxiedPlayer p = e.getPlayer();
        MongoConnection.registerPlayer(p.getUniqueId());
    }

    @EventHandler
    public void onPermissionCheck(PermissionCheckEvent e) {
        CommandSender sender = e.getSender();
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer p = (ProxiedPlayer) e.getSender();

            Collection<String> permissions = MongoPermsAPI.getGroupOfPlayer(p.getUniqueId()).get().getPermissionsWithInheritances();

            if (permissions.contains(settings.getPermissionNode()) || permissions.contains(e.getPermission())) {
                e.setHasPermission(true);
            }
        } else {
            e.setHasPermission(true);
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
