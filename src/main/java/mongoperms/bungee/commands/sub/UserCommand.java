package mongoperms.bungee.commands.sub;

import mongoperms.Messages;
import mongoperms.MongoConnection;
import mongoperms.MongoPermsAPI;
import mongoperms.bungee.commands.SubCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collection;
import java.util.UUID;

import static mongoperms.MongoPermsAPI.getUUID;

public class UserCommand extends SubCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        ProxiedPlayer p = ProxyServer.getInstance().getPlayer(args[0]);
        String name;
        UUID uuid;

        if (p == null) {
            name = args[0];
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
    }

    @Override
    public int requiredArgs() {
        return 1;
    }

    @Override
    public String getUsage() {
        return "/perms user <Name>";
    }

}
