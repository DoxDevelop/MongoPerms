package mongoperms.bungee;

import com.google.common.base.Joiner;
import mongoperms.MongoConnection;
import mongoperms.MongoConnection.Result;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static mongoperms.MongoPermsAPI.getUUID;

public class PermissionsCommand extends Command {

    public PermissionsCommand() {
        super("perms", "mongoperms.perms");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        BungeeCord.getInstance().getScheduler().runAsync(MongoPermsBungee.getInstance(), () -> {
            if (args.length == 0) {
                sender.sendMessage(new ComponentBuilder("Available options: addgroup, removegroup, setgroup, group, user, add, remove, groups").color(ChatColor.YELLOW).create());
                sender.sendMessage(new TextComponent("§eMore information by using: /perms <subcommand>"));
                return;
            }

            String subCommand = args[0];

            if (subCommand.equalsIgnoreCase("addgroup")) {

                if (args.length != 2) {
                    sender.sendMessage(new TextComponent("§eUsage: /perms addgroup <Group>"));
                    return;
                }

                String group = args[1];
                Result result = MongoConnection.addGroup(group);
                switch (result) {
                    case RESULT_SUCCESS:
                        sender.sendMessage(new TextComponent("§aGroup " + group + " has been created."));
                        break;
                    case RESULT_GROUP_EXISTS:
                        sender.sendMessage(new TextComponent("§cGroup " + group + " already exists."));
                }

            } else if (subCommand.equalsIgnoreCase("removegroup")) {

                if (args.length != 2) {
                    sender.sendMessage(new TextComponent("§eUsage: /perms removegroup <Group>"));
                    return;
                }

                String group = args[1];
                boolean removed = MongoConnection.removeGroup(group);
                sender.sendMessage(new TextComponent(removed ? "§aGroup " + group + " has been removed." : "§cCan't find group: " + args[1]));

            } else if (subCommand.equalsIgnoreCase("setgroup")) {

                if (args.length != 3) {
                    sender.sendMessage(new TextComponent("§eUsage: /perms setgroup <Player> <Group>"));
                } else {
                    ProxiedPlayer p = BungeeCord.getInstance().getPlayer(args[1]);
                    UUID uuid = p == null ? getUUID(args[1]) : p.getUniqueId();
                    String group = args[2];
                    MongoConnection.setGroup(uuid, group);
                    sender.sendMessage(new TextComponent("§aUser " + (p == null ? args[0] : p.getName()) + " is now a \"" + group + "\""));
                }

            } else if (subCommand.equalsIgnoreCase("group")) {

                if (args.length != 2) {
                    sender.sendMessage(new TextComponent("§eUsage: /perms group <Group>"));
                    return;
                }

                String group = args[1];
                List<String> permissions = MongoConnection.getPermissions(group);

                if (permissions.size() == 0) {
                    sender.sendMessage(new TextComponent("§cNo permissions found for group \"" + group + "\"."));
                    return;
                }

                sender.sendMessage(new TextComponent("§eGroup \"" + group + "\" has the following permissions:"));
                permissions.forEach(s -> sender.sendMessage(new TextComponent(" §e- " + s)));

            } else if (subCommand.equalsIgnoreCase("user")) {

                if (args.length != 2) {
                    sender.sendMessage(new TextComponent("§eUsage: /perms user <Player>"));
                    return;
                }

                ProxiedPlayer p = BungeeCord.getInstance().getPlayer(args[1]);
                String name;
                UUID uuid;

                if (p == null) {
                    name = args[1];
                    uuid = getUUID(name);
                } else {
                    name = p.getName();
                    uuid = p.getUniqueId();
                }

                String group = MongoConnection.getGroup(uuid);

                if (group == null) {
                    sender.sendMessage(new TextComponent("§cCan't find group of player: " + name));
                    return;
                }

                List<String> permissions = MongoConnection.getPermissions(group);
                sender.sendMessage(new TextComponent("§ePlayer \"" + name + "\" has the following permissions:"));
                permissions.forEach(s -> sender.sendMessage(new TextComponent(" §e- " + s)));
                sender.sendMessage(new TextComponent("§eGroup: " + group));

            } else if (subCommand.equalsIgnoreCase("add")) {

                if (args.length != 3) {
                    sender.sendMessage(new TextComponent("§cUsage: /perms add <Group> <Permission>"));
                    return;
                }

                String group = args[1];
                String permission = args[2];
                Result result = MongoConnection.addPermission(group, permission);
                switch (result) {
                    case RESULT_SUCCESS:
                        sender.sendMessage(new ComponentBuilder("Permission \"" + permission + "\" has been added to " + group + ".").color(ChatColor.GREEN).create());
                        break;
                    case RESULT_UNKNOWN_GROUP:
                        sender.sendMessage(new TextComponent("§cCan't find group: " + group));
                        break;
                }

            } else if (subCommand.equalsIgnoreCase("remove")) {

                if (args.length != 3) {
                    sender.sendMessage(new TextComponent("Usage: §c/perms remove <Group> <Permission>"));
                    return;
                }

                String group = args[1];
                String permission = args[2];
                Result result = MongoConnection.removePermission(group, permission);

                switch (result) {
                    case RESULT_SUCCESS:
                        sender.sendMessage(new ComponentBuilder("Permission \"" + permission + "\" has been removed from " + group + ".").color(ChatColor.GREEN).create());
                        break;
                    case RESULT_UNKNOWN_GROUP:
                        sender.sendMessage(new TextComponent("§cCan't find group: " + group));
                        break;
                    case RESULT_UNKNOWN_PERMISSION:
                        sender.sendMessage(new ComponentBuilder("Group \"" + group + "\" doesn't have the permission \"" + permission + "\".").color(ChatColor.RED).create());
                        break;
                }

            } else if (subCommand.equalsIgnoreCase("groups")) {

                List<String> groups = MongoConnection.getGroups().stream().sorted().collect(Collectors.toList());

                if (groups.size() == 0) {
                    sender.sendMessage(new TextComponent("§cNo groups found."));
                    return;
                }

                sender.sendMessage(new TextComponent("§eFollowing groups are available:"));
                sender.sendMessage(new ComponentBuilder(Joiner.on(", ").join(groups)).color(ChatColor.YELLOW).create());

            } else {

                sender.sendMessage(new TextComponent("§cThe subcommand \"" + subCommand + "\" doesn't exist."));

            }
        });
    }

}
