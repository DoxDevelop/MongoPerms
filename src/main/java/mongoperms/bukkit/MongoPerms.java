package mongoperms.bukkit;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.SneakyThrows;
import mongoperms.Group;
import mongoperms.MongoConnection;
import mongoperms.bukkit.command.Command;
import mongoperms.bukkit.command.ReloadCommand;
import mongoperms.bukkit.vault.VaultMongoBridge;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static mongoperms.MongoPermsAPI.getUUID;

public class MongoPerms extends JavaPlugin {

    @Getter
    private static MongoPerms instance;

    @Getter
    private static Configuration settings;

    public static final Map<UUID, PermissionAttachment> ATTACHMENTS = Maps.newLinkedHashMap();

    private static Field permissibleField;

    @Override
    public void onEnable() {
        try {
            permissibleField = getCraftHumanEntityClass().getDeclaredField("perm");
            permissibleField.setAccessible(true);
        } catch (ReflectiveOperationException e1) {
            System.out.println("[MongoPerms] Couldn't find CraftHumanEntityClass! Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        instance = this;

        if (!Files.exists(new File(getDataFolder(), "config.yml").toPath())) {
            saveDefaultConfig();
        }

        settings = Configuration.load(this);
        MongoConnection.load(settings.getMongoHost(), settings.getMongoPort(), settings.getDefaultGroup(), settings.getMongoUsername(), settings.getMongoPassword(), settings.isUseAuthentication());

        if (settings.isUseVault()) {
            Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
            if (vault != null) {
                new VaultMongoBridge(vault, this);
            }
        }

        getServer().getPluginManager().registerEvents(new MongoListener(), this);
        registerCommand(new ReloadCommand());

        System.out.println("[MongoPerms] Enabled version: " + getDescription().getVersion());

    }

    public static void generateAttachment(Player p) {

        if (p == null || !p.isOnline()) {
            return; //avoid bugs
        }

        try {
            permissibleField.set(p, new CustomPermissibleBase(p));
        } catch (ReflectiveOperationException | NullPointerException e) {
            e.printStackTrace();
        }

        PermissionAttachment attachment = p.addAttachment(instance);

        String name = MongoConnection.getGroup(getUUID(p.getName()));

        if (name == null) {
            name = getSettings().getDefaultGroup();
        }

        Group group = Group.getGroup(name);
        Preconditions.checkNotNull(group);

        group.getPermissions().forEach(permission -> {
            if (permission.startsWith("-")) {
                attachment.setPermission(permission.substring(1), false);
            } else {
                attachment.setPermission(permission, true);
            }
        });

        ATTACHMENTS.put(getUUID(p.getName()), attachment);
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends HumanEntity> getCraftHumanEntityClass() throws ReflectiveOperationException {
        return (Class<? extends HumanEntity>) Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".entity.CraftHumanEntity");
    }

    public static void unlogAttachment(Player p) {
        PermissionAttachment attachment = ATTACHMENTS.remove(getUUID(p.getName()));

        if (attachment == null) {
            System.err.println("[MongoPerms]" + p.getName() + "'s attachment is null?");
            return;
        }

        p.removeAttachment(attachment);
    }

    /*
        doing some experimenting below :D
     */
    private void registerCommand(CommandExecutor executor) {
        Preconditions.checkNotNull(executor);
        Command command = executor.getClass().getAnnotation(Command.class);
        Preconditions.checkNotNull(command, "Couldn't register " + executor.getClass().getSimpleName() + "! @Command not found.");

        CommandMap map = getCommandMap();
        PluginCommand cmd = newCommand(command.name(), this);
        cmd.setDescription(command.description());
        cmd.setExecutor(executor);
        cmd.setTabCompleter(newInstance(command.tabCompleter()));

        if (!command.permission().equals("")) {
            cmd.setPermission(command.permission());
            if (!command.permissionMessage().equals("")) {
                cmd.setPermissionMessage(command.permissionMessage());
            }
        }

        if (!command.usage().equals("")) {
            cmd.setUsage(command.usage());
        }

        if (command.aliases().length != 0) {
            cmd.setAliases(Arrays.asList(command.aliases()));
        }

        map.register(getClass().getSimpleName().toLowerCase(), cmd);
        System.out.printf("[MongoPerms] Registered command %s", command.name());
    }

    @SneakyThrows
    private PluginCommand newCommand(String name, Plugin owner) {
        Constructor<? extends org.bukkit.command.Command> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
        constructor.setAccessible(true);
        return (PluginCommand) constructor.newInstance(name, owner);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T newInstance(Class<? extends T> clazz, Object... args) {
        Constructor<T> constructor = (Constructor<T>) clazz.getConstructors()[0];

        Class<?>[] parameters = constructor.getParameterTypes();

        if (parameters.length == 0) {
            return constructor.newInstance();
        } else {
            if (Plugin.class.isAssignableFrom(parameters[0]) && parameters.length == 1) {
                return constructor.newInstance(this);
            }
        }

        throw new IllegalStateException();
    }

    @SneakyThrows
    private CommandMap getCommandMap() {
        Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        f.setAccessible(true);
        return (CommandMap) f.get(Bukkit.getServer());
    }


}
