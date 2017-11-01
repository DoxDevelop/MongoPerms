package mongoperms.bungee.commands.sub;

import mongoperms.Messages;
import mongoperms.MongoConnection;
import mongoperms.bungee.commands.SubCommand;
import net.md_5.bungee.api.CommandSender;

public class RemoveGroupCommand extends SubCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        String group = args[0];
        if (MongoConnection.removeGroup(group)) {
            sender.sendMessage(Messages.GROUP_REMOVED.toComponent(group));
        } else {
            sender.sendMessage(Messages.UNKNOWN_GROUP.toComponent(group));
        }
    }

    @Override
    public int requiredArgs() {
        return 1;
    }

    @Override
    public String getUsage() {
        return "/perms removegroup <Group>";
    }

}
