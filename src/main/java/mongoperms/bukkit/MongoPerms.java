package mongoperms.bukkit;

import com.google.common.collect.Maps;
import lombok.Getter;
import mongoperms.Configuration;
import mongoperms.Group;
import mongoperms.MongoConnection;
import mongoperms.bukkit.command.CommandRegistrar;
import mongoperms.bukkit.command.ReloadCommand;
import mongoperms.bukkit.vault.VaultMongoBridge;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import static mongoperms.MongoPermsAPI.getUUID;

public class MongoPerms extends JavaPlugin {

    @Getter
    private static MongoPerms instance;

    @Getter
    private static Configuration settings;

    public static final Map<UUID, PermissionAttachment> ATTACHMENTS = Maps.newLinkedHashMap();

    private static Field field;

    @Override
    public void onEnable() {
        try {
            field = getCraftHumanEntityClass().getDeclaredField("perm");
            field.setAccessible(true);
        } catch (ReflectiveOperationException e1) {
            getLogger().severe("[MongoPerms] Couldn't find CraftHumanEntityClass! Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        instance = this;

        if (!Files.exists(new File(getDataFolder(), "config.yml").toPath())) {
            saveDefaultConfig();
        }

        try {
            settings = Configuration.load();
        } catch (IOException e) {
            getLogger().severe("[MongoPerms] Couldn't load configuration file.");
            throw new RuntimeException(e);
        }

        MongoConnection.load(
                settings.getMongoHost(),
                settings.getMongoPort(),
                settings.getDefaultGroup(),
                settings.getMongoUsername(),
                settings.getMongoPassword(),
                false,
                settings.isUseAuthentication()
        );

        if (settings.isUseVault()) {
            Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
            if (vault != null) {
                new VaultMongoBridge(this);
                getLogger().info("[MongoPerms] Using VaultMongoBridge.");
            } else {
                getLogger().warning("[MongoPerms] Couldn't find Vault plugin. Consider disabling \"use-vault\" setting in configuration.");
            }
        }

        getServer().getPluginManager().registerEvents(new MongoListener(), this);

        CommandRegistrar registrar = new CommandRegistrar(this);
        registrar.registerCommand(new ReloadCommand());

        getLogger().log(Level.INFO, "[MongoPerms] Enabled version: {0}", getDescription().getVersion());

    }

    public static void generateAttachment(Player p) {

        if (p == null || !p.isOnline()) {
            return; //avoid bugs
        }

        try {
            field.set(p, new CustomPermissibleBase(p));
        } catch (ReflectiveOperationException | NullPointerException e) {
            getInstance().getLogger().log(Level.SEVERE, "[MongoPerms] Couldn't set permissible base. All-permission node will not work!", e);
        }

        PermissionAttachment attachment = p.addAttachment(instance);

        String name = MongoConnection.getGroup(getUUID(p.getName()));

        if (name == null) {
            name = getSettings().getDefaultGroup();
        }

        Group.getGroup(name).ifPresent(group -> {
            group.getPermissions().forEach(permission -> {
                if (permission.startsWith("-")) {
                    attachment.setPermission(permission.substring(1), false);
                } else {
                    attachment.setPermission(permission, true);
                }
            });
        });

        ATTACHMENTS.put(getUUID(p.getName()), attachment);
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends HumanEntity> getCraftHumanEntityClass() throws ReflectiveOperationException {
        return (Class<? extends HumanEntity>) Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".entity.CraftHumanEntity");
    }

    public static void unLogAttachment(Player p) {
        PermissionAttachment attachment = ATTACHMENTS.remove(getUUID(p.getName()));

        if (attachment == null) {
            getInstance().getLogger().log(Level.WARNING, "[MongoPerms] Couldn't find {0}'s PermissionAttachment.", p.getName());
            return;
        }

        p.removeAttachment(attachment);
    }


}
