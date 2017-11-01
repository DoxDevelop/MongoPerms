package mongoperms;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

import java.text.MessageFormat;

@RequiredArgsConstructor
@AllArgsConstructor
public enum Messages {

    NO_PERMISSION("§cYou are not allowed to use this command."),
    NO_OP("§cYou need op to execute this command."),
    CANT_FIND_PLAYER("§cCan't find player with name \"{0}\"."),
    RELOADED_PLAYER("§aPlayer \"{0}\" has been reloaded."),
    RELOADED_GROUPS("§a{0} groups have been reloaded."),
    REGISTERED_PLAYERS("§a{0} players registered"),
    MORE_INFORMATION("§eMore information by using: {0}"),
    USAGE("§eUsage: {0}"),
    UNKNOWN_SUBCOMMAND("§cThe subcommand \"{0} doesn't exist."),
    GROUP_CREATED("§aGroup {0} has been created."),
    GROUP_ALREADY_EXISTS("§aGroup {0} already exists."),
    GROUP_REMOVED("§aGroup {0} has been removed."),
    UNKNOWN_GROUP("§cCan't find group \"{0}\"."),
    UNKNOWN_GROUP_PLAYER("§cCan't find group of player: {0}"),
    USER_GROUP_UPDATE("§aUser {0} is now a {1}."),
    NO_PERMISSIONS_IN_GROUP("§cNo permissions found for group \"{0}\"."),
    GROUP_INHERITS_PERMISSIONS_FROM("§eGroup \"{0}\" inherits permissions from groups: {1}."),
    GROUP_HAS_PERMISSIONS("§eGroup \"{0}\" has the following permissions:"),
    PLAYER_HAS_PERMISSIONS("§ePlayer \"{0}\" has the following permissions:"),
    PERMISSION_LIST_ENTRY(" §e- {0}"),
    GROUP_OF_PLAYER("§eGroup of player: {0}"),
    PERMISSION_ADDED_TO_GROUP("§aPermission \"{0}\" has been added to group {1}."),
    ADD_INHERITANCE_INFO("First group is the group, where the inheritance is being added to.", ChatColor.YELLOW),
    REMOVE_INHERITANCE_INFO("First group is the group, where the inheritance is being removed from.", ChatColor.YELLOW),
    SUCCESSFUL_ADD_INHERITANCE("§aGroup \"{0}\" now inherits group \"{1}\"."),
    SUCCESSFUL_REMOVE_INHERITANCE("§aGroup \"{0} no longer inherits group \"{1}\""),
    PERMISSION_REMOVED("§aPermission \"{0}\" has been removed from group \"{1}\"."),
    UNKNOWN_PERMISSION("§aGroup \"{0}\" doesn't have the permission \"{1}\"."),
    GROUP_LIST_HEADER("§eFollowing groups are available:"),
    NO_GROUPS_FOUND("§cNo groups found."),
    SUCCESSFUL_ADD_MANY_PERMISSIONS("Successfully added all permissions from group \"{0}\" to group \"{1}\".", ChatColor.GREEN),
    SUCCESSFUL_PUT_MANY_PERMISSIONS("Successfully put all permissions from group \"{0}\" into group \"{1}\".", ChatColor.GREEN);

    private final String message;
    private ChatColor color = null;

    public String toString(Object... args) {
        if (args.length > 0) {
            return MessageFormat.format(message, args);
        }
        return message;
    }

    public BaseComponent[] toComponent(Object... args) {
        if (color == null) {
            return new BaseComponent[] {new TextComponent(toString(args))};
        }
        return new ComponentBuilder(toString(args)).color(color).create(); //using ComponentBuilder in case of very long messages
    }

}
