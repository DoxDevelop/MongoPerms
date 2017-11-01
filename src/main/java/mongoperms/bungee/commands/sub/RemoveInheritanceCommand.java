package mongoperms.bungee.commands.sub;

import mongoperms.Group;
import mongoperms.Messages;
import mongoperms.bungee.commands.SubCommand;
import net.md_5.bungee.api.CommandSender;

import java.util.Optional;

public class RemoveInheritanceCommand extends SubCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        Optional<Group> group = Group.getGroup(args[0]);
        Optional<Group> inherits = Group.getGroup(args[1]);

        if (!group.isPresent()) {
            sender.sendMessage(Messages.UNKNOWN_GROUP.toComponent(args[0]));
            return;
        }
        if (!inherits.isPresent()) {
            sender.sendMessage(Messages.UNKNOWN_GROUP.toComponent(args[1]));
            return;
        }

        group.get().removeInheritance(inherits.get());

        sender.sendMessage(Messages.SUCCESSFUL_REMOVE_INHERITANCE.toComponent(group.get().getName(), inherits.get().getName()));
    }

    @Override
    public int requiredArgs() {
        return 2;
    }

    @Override
    public String getUsage() {
        return "/perms removeinheritance <Group> <Group>";
    }

}
