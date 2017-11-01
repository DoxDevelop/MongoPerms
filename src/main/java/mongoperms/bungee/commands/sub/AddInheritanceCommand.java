package mongoperms.bungee.commands.sub;

import mongoperms.Group;
import mongoperms.Messages;
import mongoperms.bungee.commands.SubCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.Optional;

public class AddInheritanceCommand extends SubCommand {

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

        group.get().addInheritance(inherits.get());

        sender.sendMessage(Messages.SUCCESSFUL_ADD_INHERITANCE.toComponent(group.get().getName(), inherits.get().getName()));
    }

    @Override
    public int requiredArgs() {
        return 2;
    }

    @Override
    public String getUsage() {
        return "/perms addinheritance <Group> <Group>";
    }

    @Override
    public BaseComponent[] additionalComponents() {
        return Messages.ADD_INHERITANCE_INFO.toComponent();
    }

}
