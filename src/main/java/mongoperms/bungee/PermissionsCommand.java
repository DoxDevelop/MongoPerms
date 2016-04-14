package mongoperms.bungee;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
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
                sender.sendMessage(new ComponentBuilder("Available options: addgroup, removegroup, setgroup, group, user, add, remove, groups, addall, putall").color(ChatColor.YELLOW).create());
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

            } else if (subCommand.equalsIgnoreCase("addall")) {

                if (args.length != 3) {
                    sender.sendMessage(new TextComponent("§eUsage: /perms addall <FromGroup> <ToGroup>"));
                    return;
                }

                String fromGroup = args[1];
                String toGroup = args[2];

                List<String> groups = MongoConnection.getGroups();

                if (!Iterables.contains(groups, fromGroup)) {
                    sender.sendMessage(new TextComponent("§cCan't find group: " + fromGroup));
                    return;
                } else if (!Iterables.contains(groups, toGroup)) {
                    sender.sendMessage(new TextComponent("§cCan't find group: " + toGroup));
                    return;
                }

                List<String> fromPermissions = MongoConnection.getPermissions(fromGroup);

                if (fromPermissions.size() == 0) {
                    sender.sendMessage(new TextComponent("§cNo permissions found in group \"" + fromGroup + "\"."));
                    return;
                }

                List<String> toPermissions = MongoConnection.getPermissions(toGroup);

                Iterables.addAll(toPermissions, fromPermissions);

                switch (MongoConnection.setPermissions(toGroup, toPermissions)) {
                    case RESULT_SUCCESS:
                        sender.sendMessage(new ComponentBuilder("Successfully added all permissions from group \"" + fromGroup + "\" to group \"" + toGroup + "\".").color(ChatColor.GREEN).create());
                        break;
                    case RESULT_UNKNOWN_ERROR:
                        sender.sendMessage(new TextComponent("§cCouldn't transfer permissions."));
                        break;
                }

            } else if (subCommand.equalsIgnoreCase("putall")) {

                if (args.length != 3) {
                    sender.sendMessage(new TextComponent("§eUsage: /perms putall <FromGroup> <ToGroup>"));
                    return;
                }

                String fromGroup = args[1];
                String toGroup = args[2];

                List<String> groups = MongoConnection.getGroups();

                if (!Iterables.contains(groups, fromGroup)) {
                    sender.sendMessage(new TextComponent("§cCan't find group: " + fromGroup));
                    return;
                } else if (!Iterables.contains(groups, toGroup)) {
                    sender.sendMessage(new TextComponent("§cCan't find group: " + toGroup));
                    return;
                }

                List<String> fromPermissions = MongoConnection.getPermissions(fromGroup);

                if (fromPermissions.size() == 0) {
                    sender.sendMessage(new TextComponent("§cNo permissions found in group \"" + fromGroup + "\"."));
                    return;
                }

                switch (MongoConnection.setPermissions(toGroup, fromPermissions)) {
                    case RESULT_SUCCESS:
                        sender.sendMessage(new ComponentBuilder("Successfully put all permissions from group \"" + fromGroup + "\" into group \"" + toGroup + "\".").color(ChatColor.GREEN).create());
                        break;
                    case RESULT_UNKNOWN_ERROR:
                        sender.sendMessage(new TextComponent("§cCouldn't transfer permissions."));
                        break;
                }

            } else if (subCommand.equalsIgnoreCase("reload")) {

                if (args.length == 2) {
                    ProxiedPlayer p = BungeeCord.getInstance().getPlayer(args[1]);
                    if (p == null) {
                        sender.sendMessage(new TextComponent("§cCan't find player: " + args[1]));
                        return;
                    }
                    MongoPermsBungee.getInstance().reloadPlayer(p);
                    sender.sendMessage(new TextComponent("§aPlayer \"" + p.getName() + "\" has been reloaded"));
                    return;
                }

                MongoPermsBungee.getInstance().reloadGroups();
                sender.sendMessage(new TextComponent("§aGroups have been reloaded."));

            } else {

                sender.sendMessage(new TextComponent("§cThe subcommand \"" + subCommand + "\" doesn't exist."));

            }
        });
    }

}
