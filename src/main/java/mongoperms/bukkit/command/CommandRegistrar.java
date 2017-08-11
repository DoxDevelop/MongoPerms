package mongoperms.bukkit.command;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

@RequiredArgsConstructor
public class CommandRegistrar {

    private final Plugin plugin;

    public void registerCommand(CommandExecutor executor) {
        Preconditions.checkNotNull(executor);
        Command command = executor.getClass().getAnnotation(Command.class);
        Preconditions.checkNotNull(command, "Couldn't register " + executor.getClass().getSimpleName() + "! @Command not found.");

        CommandMap map = getCommandMap();
        PluginCommand cmd = newCommand(command.name(), plugin);
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
    private <T> T newInstance(Class<? extends T> clazz) {
        Constructor<T> constructor = (Constructor<T>) clazz.getConstructors()[0];

        if (constructor.getParameterTypes().length == 0) {
            return constructor.newInstance();
        } else {
            return constructor.newInstance(this);
        }
    }

    @SneakyThrows
    private CommandMap getCommandMap() {
        Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        f.setAccessible(true);
        return (CommandMap) f.get(Bukkit.getServer());
    }

}
