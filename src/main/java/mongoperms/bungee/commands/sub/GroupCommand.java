package mongoperms.bungee.commands.sub;

import com.google.common.base.Joiner;
import mongoperms.Group;
import mongoperms.Messages;
import mongoperms.bungee.commands.SubCommand;
import net.md_5.bungee.api.CommandSender;

import java.util.Optional;

public class GroupCommand extends SubCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        Optional<Group> foundGroup = Group.getGroup(args[0]);

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
    }

    @Override
    public int requiredArgs() {
        return 1;
    }

    @Override
    public String getUsage() {
        return "/perms group <Group>";
    }

}

