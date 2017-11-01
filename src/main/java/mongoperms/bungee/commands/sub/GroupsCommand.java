package mongoperms.bungee.commands.sub;

import com.google.common.base.Joiner;
import mongoperms.Group;
import mongoperms.Messages;
import mongoperms.bungee.commands.SubCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.util.List;
import java.util.stream.Collectors;

public class GroupsCommand extends SubCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        List<String> groups = Group.getGroups().stream().map(Group::getName).sorted().collect(Collectors.toList());

        if (groups.size() == 0) {
            sender.sendMessage(Messages.NO_GROUPS_FOUND.toComponent());
            return;
        }

        sender.sendMessage(Messages.GROUP_LIST_HEADER.toComponent());
        sender.sendMessage(new ComponentBuilder(Joiner.on(", ").join(groups)).color(ChatColor.YELLOW).create());
    }

    @Override
    public int requiredArgs() {
        return 0;
    }

    @Override
    public String getUsage() {
        return null;
    }

}
