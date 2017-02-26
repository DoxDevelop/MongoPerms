package mongoperms.bungee;

import com.google.common.base.Joiner;
import mongoperms.Messages;
import mongoperms.MongoConnection;
import mongoperms.MongoConnection.Result;
import mongoperms.MongoPermsAPI;
import mongoperms.Group;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Collection;
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
        ProxyServer.getInstance().getScheduler().runAsync(MongoPermsBungee.getInstance(), () -> {
            if (args.length == 0) {
                sender.sendMessage(new ComponentBuilder("Available options: addgroup, removegroup, setgroup, group, user, add, remove, groups, addall, putall, reload").color(ChatColor.YELLOW).create());
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
                sender.sendMessage(new TextComponent(removed ? "§aGroup " + group + " has been removed." : Messages.NO_GROUP(args[0]).getText()));

            } else if (subCommand.equalsIgnoreCase("setgroup")) {

                if (args.length != 3) {
                    sender.sendMessage(new TextComponent("§eUsage: /perms setgroup <Player> <Group>"));
                } else {
                    ProxiedPlayer p = ProxyServer.getInstance().getPlayer(args[1]);
                    UUID uuid = p == null ? getUUID(args[1]) : p.getUniqueId();
                    String group = args[2];
                    if (MongoPermsAPI.setGroup(uuid, Group.getGroup(group))) {
                        sender.sendMessage(new TextComponent("§aUser " + (p == null ? args[0] : p.getName()) + " is now a \"" + group + "\""));
                    } else {
                        sender.sendMessage(Messages.NO_GROUP(group));
                    }
                }

            } else if (subCommand.equalsIgnoreCase("group")) {

                if (args.length != 2) {
                    sender.sendMessage(new TextComponent("§eUsage: /perms group <Group>"));
                    return;
                }

                Group group = Group.getGroup(args[1]);

                if (group == null) {
                    sender.sendMessage(Messages.NO_GROUP(args[1]));
                    return;
                }

                if (group.getPermissions().size() == 0) {
                    sender.sendMessage(Messages.NO_PERMISSIONS_FOUND(group.getName()));
                    return;
                }

                if (group.getInherits().size() > 0) {
                    sender.sendMessage(new ComponentBuilder("Group \"" + group.getName() + "\" inherits permissions from groups: ").color(ChatColor.YELLOW).append(Joiner.on(", ").join(group.getInherits())).create());
                }
                sender.sendMessage(new ComponentBuilder("Group \"" + group.getName() + "\" has the following permissions:").color(ChatColor.YELLOW).create());
                group.getPermissions().forEach(s -> sender.sendMessage(new TextComponent(" §e- " + s)));

            } else if (subCommand.equalsIgnoreCase("user")) {

                if (args.length != 2) {
                    sender.sendMessage(new TextComponent("§eUsage: /perms user <Player>"));
                    return;
                }

                ProxiedPlayer p = ProxyServer.getInstance().getPlayer(args[1]);
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

                Collection<String> permissions = MongoPermsAPI.getPermissions(group);
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
                        sender.sendMessage(Messages.NO_GROUP(group));
                        break;
                }

            } else if (subCommand.equalsIgnoreCase("addinheritance")) {

                if (args.length != 3) {
                    sender.sendMessage(new TextComponent("§cUsage: /perms addinheritance <Group> <Group>"));
                    sender.sendMessage(new ComponentBuilder("First group is the group, where the inheritance is being added to.").color(ChatColor.YELLOW).create());
                    return;
                }

                Group group = Group.getGroup(args[1]);
                Group inherits = Group.getGroup(args[2]);

                if (group == null) {
                    sender.sendMessage(Messages.NO_GROUP(args[1]));
                    return;
                }
                if (inherits == null) {
                    sender.sendMessage(Messages.NO_GROUP(args[2]));
                    return;
                }

                group.addInheritance(inherits);

                sender.sendMessage(new ComponentBuilder("Group \"" + group.getName() + "\" now inherits group \"" + inherits.getName() + "\".").color(ChatColor.GREEN).create());

            } else if (subCommand.equalsIgnoreCase("removeinheritance")) {

                if (args.length != 3) {
                    sender.sendMessage(new TextComponent("§cUsage: /perms removeinheritance <Group> <Group>"));
                    sender.sendMessage(new ComponentBuilder("First group is the group, where the inheritance is being removed from.").color(ChatColor.YELLOW).create());
                    return;
                }

                Group group = Group.getGroup(args[1]);
                Group inherits = Group.getGroup(args[2]);

                if (group == null) {
                    sender.sendMessage(Messages.NO_GROUP(args[1]));
                    return;
                }
                if (inherits == null) {
                    sender.sendMessage(Messages.NO_GROUP(args[2]));
                    return;
                }

                group.removeInheritance(inherits);

                sender.sendMessage(new ComponentBuilder("Group \"" + group.getName() + "\" no longer inherits group \"" + inherits.getName() + "\".").color(ChatColor.GREEN).create());

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
                        sender.sendMessage(Messages.NO_GROUP(group));
                        break;
                    case RESULT_UNKNOWN_PERMISSION:
                        sender.sendMessage(new ComponentBuilder("Group \"" + group + "\" doesn't have the permission \"" + permission + "\".").color(ChatColor.RED).create());
                        break;
                }

            } else if (subCommand.equalsIgnoreCase("groups")) {

                List<String> groups = Group.getGroups().stream().map(Group::getName).sorted().collect(Collectors.toList());

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

                if (Group.getGroup(fromGroup) == null) {
                    sender.sendMessage(new TextComponent("§cCan't find group: " + fromGroup));
                    return;
                } else if (Group.getGroup(toGroup) == null) {
                    sender.sendMessage(new TextComponent("§cCan't find group: " + toGroup));
                    return;
                }

                Group from = Group.getGroup(fromGroup);
                Group to = Group.getGroup(toGroup);

                if (from.getPermissions(false).size() == 0) {
                    sender.sendMessage(new TextComponent("§cNo permissions found in group \"" + fromGroup + "\"."));
                    return;
                }

                to.addAll(from.getPermissions(false));
                sender.sendMessage(new ComponentBuilder("Successfully added all permissions from group \"" + fromGroup + "\" to group \"" + toGroup + "\".").color(ChatColor.GREEN).create());
            } else if (subCommand.equalsIgnoreCase("putall")) {

                if (args.length != 3) {
                    sender.sendMessage(new TextComponent("§eUsage: /perms putall <FromGroup> <ToGroup>"));
                    return;
                }

                String fromGroup = args[1];
                String toGroup = args[2];

                if (Group.getGroup(fromGroup) == null) {
                    sender.sendMessage(new TextComponent("§cCan't find group: " + fromGroup));
                    return;
                } else if (Group.getGroup(toGroup) == null) {
                    sender.sendMessage(new TextComponent("§cCan't find group: " + toGroup));
                    return;
                }

                Group from = Group.getGroup(fromGroup);
                Group to = Group.getGroup(toGroup);

                if (from.getPermissions(false).size() == 0) {
                    sender.sendMessage(new TextComponent("§cNo permissions found in group \"" + fromGroup + "\"."));
                    return;
                }

                to.setPermissions(from.getPermissions(false));
                sender.sendMessage(new ComponentBuilder("Successfully put all permissions from group \"" + fromGroup + "\" into group \"" + toGroup + "\".").color(ChatColor.GREEN).create());

            } else if (subCommand.equalsIgnoreCase("reload")) {

                if (args.length == 2) {
                    ProxiedPlayer p = ProxyServer.getInstance().getPlayer(args[1]);
                    if (p == null) {
                        sender.sendMessage(new TextComponent("§cCan't find player: " + args[1]));
                        return;
                    }
                    MongoPermsAPI.clear(p.getUniqueId());
                    sender.sendMessage(new TextComponent("§aPlayer \"" + p.getName() + "\" has been reloaded"));
                    return;
                }

                Group.reloadGroups();
                sender.sendMessage(new TextComponent("§aGroups have been reloaded."));

            } else {

                sender.sendMessage(new TextComponent("§cThe subcommand \"" + subCommand + "\" doesn't exist."));

            }
        });
    }

}
