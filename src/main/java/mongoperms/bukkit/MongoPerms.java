package mongoperms.bukkit;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.SneakyThrows;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static mongoperms.MongoPermsAPI.getUUID;

public class MongoPerms extends JavaPlugin {

    @Getter
    private static MongoPerms instance;

    @Getter
    private static Configuration settings;

    public static final Map<UUID, PermissionAttachment> attachments = Maps.newLinkedHashMap();
    public static final Map<String, List<String>> groups = Maps.newLinkedHashMap();

    private static Field field;

    @Override
    public void onEnable() {
        try {
            field = getCraftHumanEntityClass().getDeclaredField("perm");
            field.setAccessible(true);
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
        MongoConnection.load(settings.getMongoHost(), settings.getMongoPort(), settings.getDefaultGroup());

        if (settings.isUseVault()) {
            Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
            if (vault != null) {
                new VaultMongoBridge(vault);
            }
        }

        getServer().getPluginManager().registerEvents(new MongoListener(), this);
        registerCommand(new ReloadCommand());

        MongoConnection.getGroups().forEach(group -> {
            List<String> permissions = MongoConnection.getPermissions(group);
            if (permissions != null) {
                groups.put(group, permissions);
            } else {
                groups.put(group, Lists.newArrayList());
            }
        });

        System.out.println("[MongoPerms] Enabled version: " + getDescription().getVersion());

    }

    public static void generateAttachment(Player p) {

        if (p == null || !p.isOnline()) {
            return; //avoid bugs
        }

        try {
            field.set(p, new CustomPermissibleBase(p));
        } catch (ReflectiveOperationException e1) {
            e1.printStackTrace();
        }

        PermissionAttachment attachment = p.addAttachment(instance);

        String group = MongoConnection.getGroup(getUUID(p.getName()));

        if (group == null) {
            group = getSettings().getDefaultGroup();
        }

        if (groups.containsKey(group)) {
            groups.get(group).forEach(s -> {
                if (s.startsWith("-")) {
                    attachment.setPermission(s.substring(1, s.length()), false);
                } else {
                    attachment.setPermission(s, true);
                }
            });
        }

        attachments.put(getUUID(p.getName()), attachment);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private static Class<? extends HumanEntity> getCraftHumanEntityClass() {
        return (Class<? extends HumanEntity>) Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".entity.CraftHumanEntity");
    }

    public static void unlogAttachment(Player p) {
        PermissionAttachment attachment = attachments.remove(getUUID(p.getName()));

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
        Command command = executor.getClass().getAnnotation(Command.class);

        if (command == null) {
            return;
        }

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
        System.out.println(String.format("[MongoPerms] Registered command %s", command.name()));
    }

    @SneakyThrows
    private PluginCommand newCommand(String name, Plugin owner) {
        Constructor<? extends org.bukkit.command.Command> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
        constructor.setAccessible(true);
        return (PluginCommand) constructor.newInstance(name, owner);
    }

    @SneakyThrows
    public <T> T newInstance(Class<? extends T> clazz) {
        return clazz.newInstance(); //throws Exception if there's a constructor with multiple args!
    }

    @SneakyThrows
    private CommandMap getCommandMap() {
        Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        f.setAccessible(true);
        return (CommandMap) f.get(Bukkit.getServer());
    }


}
