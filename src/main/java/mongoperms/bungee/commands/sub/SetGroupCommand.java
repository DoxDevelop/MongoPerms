package mongoperms.bungee.commands.sub;

import mongoperms.Group;
import mongoperms.Messages;
import mongoperms.MongoPermsAPI;
import mongoperms.bungee.commands.SubCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

import static mongoperms.MongoPermsAPI.getUUID;

public class SetGroupCommand extends SubCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        ProxiedPlayer p = ProxyServer.getInstance().getPlayer(args[0]);
        UUID uuid = p == null ? getUUID(args[1]) : p.getUniqueId();
        String group = args[1];

        if (MongoPermsAPI.setGroup(uuid, Group.getGroup(group).orElse(null))) { //null groups are handled by API
            sender.sendMessage(Messages.USER_GROUP_UPDATE.toComponent((p == null ? args[0] : p.getName()), group));
        } else {
            sender.sendMessage(Messages.UNKNOWN_GROUP.toComponent(group));
        }
    }

    @Override
    public int requiredArgs() {
        return 2;
    }

    @Override
    public String getUsage() {
        return "/perms setgroup <Player> <Group>";
    }

}
