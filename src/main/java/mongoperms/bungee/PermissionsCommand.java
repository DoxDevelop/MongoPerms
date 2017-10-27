package mongoperms.bungee;

import com.google.common.base.Joiner;
import mongoperms.Group;
import mongoperms.Messages;
import mongoperms.MongoConnection;
import mongoperms.MongoConnection.Result;
import mongoperms.MongoPermsAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
                //TODO move subcommands to separate classes
                sender.sendMessage(new ComponentBuilder("Available options: addgroup, removegroup, setgroup, group, user, add, remove, groups, addall, putall, reload").color(ChatColor.YELLOW).create());
                sender.sendMessage(Messages.MORE_INFORMATION.toComponent("/perms <subcommand>"));
                return;
            }

            String subCommand = args[0];

            if (subCommand.equalsIgnoreCase("addgroup")) {

                if (args.length != 2) {
                    sender.sendMessage(Messages.USAGE.toComponent("/perms addgroup <Group>"));
                    return;
                }

                String group = args[1];
                Result result = MongoConnection.addGroup(group);
                switch (result) {
                    case SUCCESS:
                        sender.sendMessage(Messages.GROUP_CREATED.toComponent(group));
                        break;
                    case GROUP_ALREADY_EXISTS:
                        sender.sendMessage(Messages.GROUP_ALREADY_EXISTS.toComponent(group));
                }

            } else if (subCommand.equalsIgnoreCase("removegroup")) {

                if (args.length != 2) {
                    sender.sendMessage(Messages.USAGE.toComponent("/perms removegroup <Group>"));
                    return;
                }

                String group = args[1];
                if (MongoConnection.removeGroup(group)) {
                    sender.sendMessage(Messages.GROUP_REMOVED.toComponent(group));
                } else {
                    sender.sendMessage(Messages.UNKNOWN_GROUP.toComponent(group));
                }

            } else if (subCommand.equalsIgnoreCase("setgroup")) {

                if (args.length != 3) {
                    sender.sendMessage(Messages.USAGE.toComponent("/perms setgroup <Player> <Group>"));
                } else {
                    ProxiedPlayer p = ProxyServer.getInstance().getPlayer(args[1]);
                    UUID uuid = p == null ? getUUID(args[1]) : p.getUniqueId();
                    String group = args[2];

                    if (MongoPermsAPI.setGroup(uuid, Group.getGroup(group).orElse(null))) { //null groups are handled by API
                        sender.sendMessage(Messages.USER_GROUP_UPDATE.toComponent((p == null ? args[0] : p.getName()), group));
                    } else {
                        sender.sendMessage(Messages.UNKNOWN_GROUP.toComponent(group));
                    }
                }

            } else if (subCommand.equalsIgnoreCase("group")) {

                if (args.length != 2) {
                    sender.sendMessage(Messages.USAGE.toComponent("/perms group <Group>"));
                    return;
                }

                Optional<Group> foundGroup = Group.getGroup(args[1]);

                if (!foundGroup.isPresent()) {
                    sender.sendMessage(Messages.UNKNOWN_GROUP.toComponent(args[1]));
                    return;
                }

                Group group = foundGroup.get();

                if (group.getPermissions().size() == 0) {
                    sender.sendMessage(Messages.NO_PERMISSIONS_IN_GROUP.toComponent(group.getName()));
                    return;
                }

                if (group.getInherits().size() > 0) {
                    sender.sendMessage(Messages.GROUP_INHERITS_PERMISSIONS_FROM.toComponent(group.getName(), Joiner.on(", ").join(group.getInherits())));
                }
                sender.sendMessage(Messages.GROUP_HAS_PERMISSIONS.toComponent(group.getName()));
                group.getPermissions().forEach(permission -> sender.sendMessage(Messages.PERMISSION_LIST_ENTRY.toComponent(permission)));

            } else if (subCommand.equalsIgnoreCase("user")) {

                if (args.length != 2) {
                    sender.sendMessage(Messages.USAGE.toComponent("/perms user <Player>"));
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
                    sender.sendMessage(Messages.UNKNOWN_GROUP_PLAYER.toComponent(name));
                    return;
                }

                Collection<String> permissions = MongoPermsAPI.getPermissions(group);
                sender.sendMessage(Messages.PLAYER_HAS_PERMISSIONS.toComponent(name));
                permissions.forEach(permission -> sender.sendMessage(Messages.PERMISSION_LIST_ENTRY.toComponent(permission)));
                sender.sendMessage(Messages.GROUP_OF_PLAYER.toComponent(group));

            } else if (subCommand.equalsIgnoreCase("add")) {

                if (args.length != 3) {
                    sender.sendMessage(Messages.USAGE.toComponent("/perms add <Group> <Permission>"));
                    return;
                }

                String group = args[1];
                String permission = args[2];
                Result result = MongoConnection.addPermission(group, permission);
                switch (result) {
                    case SUCCESS:
                        sender.sendMessage(Messages.PERMISSION_ADDED_TO_GROUP.toComponent(permission, group));
                        break;
                    case UNKNOWN_GROUP:
                        sender.sendMessage(Messages.UNKNOWN_GROUP.toComponent(group));
                        break;
                }

            } else if (subCommand.equalsIgnoreCase("addinheritance")) {

                if (args.length != 3) {
                    sender.sendMessage(Messages.USAGE.toComponent("/perms addinheritance <Group> <Group>"));
                    sender.sendMessage(Messages.ADD_INHERITANCE_INFO.toComponent());
                    return;
                }

                Optional<Group> group = Group.getGroup(args[1]);
                Optional<Group> inherits = Group.getGroup(args[2]);

                if (!group.isPresent()) {
                    sender.sendMessage(Messages.UNKNOWN_GROUP.toComponent(args[1]));
                    return;
                }
                if (!inherits.isPresent()) {
                    sender.sendMessage(Messages.UNKNOWN_GROUP.toComponent(args[2]));
                    return;
                }

                group.get().addInheritance(inherits.get());

                sender.sendMessage(Messages.SUCCESSFUL_ADD_INHERITANCE.toComponent(group.get().getName(), inherits.get().getName()));

            } else if (subCommand.equalsIgnoreCase("removeinheritance")) {

                if (args.length != 3) {
                    sender.sendMessage(Messages.USAGE.toComponent("/perms removeinheritance <Group> <Group>"));
                    sender.sendMessage(Messages.REMOVE_INHERITANCE_INFO.toComponent());
                    return;
                }

                Optional<Group> group = Group.getGroup(args[1]);
                Optional<Group> inherits = Group.getGroup(args[2]);

                if (!group.isPresent()) {
                    sender.sendMessage(Messages.UNKNOWN_GROUP.toComponent(args[1]));
                    return;
                }
                if (!inherits.isPresent()) {
                    sender.sendMessage(Messages.UNKNOWN_GROUP.toComponent(args[2]));
                    return;
                }

                group.get().removeInheritance(inherits.get());

                sender.sendMessage(Messages.SUCCESSFUL_REMOVE_INHERITANCE.toComponent(group.get().getName(), inherits.get().getName()));

            } else if (subCommand.equalsIgnoreCase("remove")) {

                if (args.length != 3) {
                    sender.sendMessage(Messages.USAGE.toComponent("/perms remove <Group> <Permission>"));
                    return;
                }

                String group = args[1];
                String permission = args[2];
                Result result = MongoConnection.removePermission(group, permission);

                switch (result) {
                    case SUCCESS:
                        sender.sendMessage(Messages.PERMISSION_REMOVED.toComponent(permission, group));
                        break;
                    case UNKNOWN_GROUP:
                        sender.sendMessage(Messages.UNKNOWN_GROUP.toComponent(group));
                        break;
                    case UNKNOWN_PERMISSION:
                        sender.sendMessage(Messages.UNKNOWN_PERMISSION.toComponent(group, permission));
                        break;
                }

            } else if (subCommand.equalsIgnoreCase("groups")) {

                List<String> groups = Group.getGroups().stream().map(Group::getName).sorted().collect(Collectors.toList());

                if (groups.size() == 0) {
                    sender.sendMessage(Messages.NO_GROUPS_FOUND.toComponent());
                    return;
                }

                sender.sendMessage(Messages.GROUP_LIST_HEADER.toComponent());
                sender.sendMessage(new ComponentBuilder(Joiner.on(", ").join(groups)).color(ChatColor.YELLOW).create());

            } else if (subCommand.equalsIgnoreCase("addall")) {

                if (args.length != 3) {
                    sender.sendMessage(new TextComponent(Messages.USAGE.toComponent("/perms addall <FromGroup> <ToGroup>")));
                    return;
                }

                Optional<Group> from = Group.getGroup(args[1]);
                Optional<Group> to = Group.getGroup(args[2]);

                if (!from.isPresent()) {
                    sender.sendMessage(Messages.UNKNOWN_GROUP.toComponent(args[1]));
                    return;
                }
                if (!to.isPresent()) {
                    sender.sendMessage(Messages.UNKNOWN_GROUP.toComponent(args[2]));
                    return;
                }

                if (from.get().getPermissions().size() == 0) {
                    sender.sendMessage(Messages.NO_PERMISSIONS_IN_GROUP.toComponent(from.get().getName()));
                    return;
                }

                to.get().addAll(from.get().getPermissions());
                sender.sendMessage(Messages.SUCCESSFUL_ADD_MANY_PERMISSIONS.toComponent(from.get().getName(), to.get().getName()));
            } else if (subCommand.equalsIgnoreCase("putall")) {

                if (args.length != 3) {
                    sender.sendMessage(Messages.USAGE.toComponent("/perms putall <FromGroup> <ToGroup>"));
                    return;
                }

                Optional<Group> from = Group.getGroup(args[1]);
                Optional<Group> to = Group.getGroup(args[2]);

                if (!from.isPresent()) {
                    sender.sendMessage(Messages.UNKNOWN_GROUP.toComponent(args[1]));
                    return;
                }
                if (!to.isPresent()) {
                    sender.sendMessage(Messages.UNKNOWN_GROUP.toComponent(args[2]));
                    return;
                }

                if (from.get().getPermissions().size() == 0) {
                    sender.sendMessage(Messages.NO_PERMISSIONS_IN_GROUP.toComponent(from.get().getName()));
                    return;
                }

                to.get().setPermissions(from.get().getPermissions());
                sender.sendMessage(Messages.SUCCESSFUL_PUT_MANY_PERMISSIONS.toComponent(from.get().getName(), to.get().getName()));

            } else if (subCommand.equalsIgnoreCase("reload")) {

                if (args.length == 2) {
                    ProxiedPlayer p = ProxyServer.getInstance().getPlayer(args[1]);
                    if (p == null) {
                        sender.sendMessage(Messages.CANT_FIND_PLAYER.toComponent(args[1]));
                        return;
                    }
                    MongoPermsAPI.clear(p.getUniqueId());
                    sender.sendMessage(Messages.RELOADED_PLAYER.toComponent(p.getName()));
                    return;
                }

                Group.reloadGroups();
                sender.sendMessage(Messages.RELOADED_GROUPS.toComponent(Group.getGroups().size()));

            } else {

                sender.sendMessage(Messages.UNKNOWN_SUBCOMMAND.toComponent(subCommand));

            }
        });
    }

}
