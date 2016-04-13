package mongoperms.bungee;

import mongoperms.MongoConnection;
import mongoperms.bukkit.MongoPerms;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.List;
import java.util.UUID;

public class PermissionsCommand extends Command {

    public PermissionsCommand() {
        super("perms", "mongoperms.perms");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        BungeeCord.getInstance().getScheduler().runAsync(MongoPermsBungee.getInstance(), () -> {
            if (args.length == 0) {
                sender.sendMessage(new ComponentBuilder("Available options: addgroup, removegroup, setgroup, group, user, add, remove, groups").color(ChatColor.YELLOW).create());
                return;
            }

            String subCommand = args[0];

            if (subCommand.equalsIgnoreCase("addgroup")) {

                String group = args[1];
                MongoConnection.addGroup(group);
                sender.sendMessage(new TextComponent("§aGroup " + group + " has been created."));

            } else if (subCommand.equalsIgnoreCase("removegroup")) {

                String group = args[1];
                MongoConnection.removeGroup(group);
                sender.sendMessage(new TextComponent("§aGroup " + group + " has been removed."));

            } else if (subCommand.equalsIgnoreCase("setgroup")) {

                if (args.length != 3) {
                    sender.sendMessage(new TextComponent("§e/perms setgroup <Player> <Group>"));
                } else {
                    ProxiedPlayer p = BungeeCord.getInstance().getPlayer(args[1]);
                    UUID uuid = p == null ? MongoPerms.getUUID(args[0]) : p.getUniqueId();
                    String group = args[2];
                    MongoConnection.setGroup(uuid, group);
                    sender.sendMessage(new TextComponent("§aUser " + (p == null ? args[0] : p.getName()) + " is now a \"" + group + "\""));
                }

            } else if (subCommand.equalsIgnoreCase("group")) {

                String group = args[1];
                List<String> permissions = MongoConnection.getPermissions(group);
                sender.sendMessage(new TextComponent("§eGroup \"" + group + "\" has the following permissions:"));
                permissions.forEach(s -> sender.sendMessage(new TextComponent(" §e- " + s)));

            } else if (subCommand.equalsIgnoreCase("user")) {

                ProxiedPlayer p = BungeeCord.getInstance().getPlayer(args[1]);
                String group = MongoConnection.getGroup(p.getUniqueId());
                List<String> permissions = MongoConnection.getPermissions(group);
                sender.sendMessage(new TextComponent("§ePlayer \"" + p.getName() + "\" has the following permissions :"));
                permissions.forEach(s -> sender.sendMessage(new TextComponent(" §e- " + s)));
                sender.sendMessage(new TextComponent("§eGroup: " + group));

            } else if (subCommand.equalsIgnoreCase("add")) {

                String group = args[1];
                String permission = args[2];
                MongoConnection.addPermission(group, permission);
                sender.sendMessage(new TextComponent("§aPermission \"" + permission + "\" has been added to " + group + "."));

            } else if (subCommand.equalsIgnoreCase("remove")) {

                String group = args[1];
                String permission = args[2];
                MongoConnection.removePermission(group, permission);
                sender.sendMessage(new TextComponent("§aPermission \"" + permission + "\" has been removed from " + group + "."));

            } else if (subCommand.equalsIgnoreCase("groups")) {

                final String[] groups = {""};
                MongoConnection.getGroups().forEach(s -> groups[0] += s + ", ");
                sender.sendMessage(new TextComponent("§eFollowing groups are available:"));
                sender.sendMessage(new TextComponent("§e" + groups[0].substring(0, groups[0].length() - 2)));

            } else {

                sender.sendMessage(new TextComponent("§cThe subcommand \"" + subCommand + "\" doesn't exist."));

            }
        });
    }

}
