package mongoperms.bungee.commands.sub;

import mongoperms.Group;
import mongoperms.Messages;
import mongoperms.bungee.commands.SubCommand;
import net.md_5.bungee.api.CommandSender;

import java.util.Optional;

public class AddAllCommand extends SubCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        Optional<Group> from = Group.getGroup(args[0]);
        Optional<Group> to = Group.getGroup(args[1]);

        if (!from.isPresent()) {
            sender.sendMessage(Messages.UNKNOWN_GROUP.toComponent(args[0]));
            return;
        }
        if (!to.isPresent()) {
            sender.sendMessage(Messages.UNKNOWN_GROUP.toComponent(args[1]));
            return;
        }

        if (from.get().getPermissions().size() == 0) {
            sender.sendMessage(Messages.NO_PERMISSIONS_IN_GROUP.toComponent(from.get().getName()));
            return;
        }

        to.get().addAll(from.get().getPermissions());
        sender.sendMessage(Messages.SUCCESSFUL_ADD_MANY_PERMISSIONS.toComponent(from.get().getName(), to.get().getName()));
    }

    @Override
    public int requiredArgs() {
        return 2;
    }

    @Override
    public String getUsage() {
        return "/perms addall <FromGroup> <ToGroup>";
    }

}
