package mongoperms.bungee.commands.sub;

import mongoperms.Messages;
import mongoperms.MongoConnection;
import mongoperms.bungee.commands.SubCommand;
import net.md_5.bungee.api.CommandSender;

public class AddGroupCommand extends SubCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        String group = args[0];
        MongoConnection.Result result = MongoConnection.addGroup(group);
        switch (result) {
            case SUCCESS:
                sender.sendMessage(Messages.GROUP_CREATED.toComponent(group));
                break;
            case GROUP_ALREADY_EXISTS:
                sender.sendMessage(Messages.GROUP_ALREADY_EXISTS.toComponent(group));
        }
    }

    @Override
    public int requiredArgs() {
        return 1;
    }

    @Override
    public String getUsage() {
        return "/perms addgroup <Group>";
    }
}
