package mongoperms.bungee.commands.sub;

import mongoperms.Group;
import mongoperms.Messages;
import mongoperms.MongoPermsAPI;
import mongoperms.bungee.commands.SubCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ReloadCommand extends SubCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 1) {
            ProxiedPlayer p = ProxyServer.getInstance().getPlayer(args[0]);
            if (p == null) {
                sender.sendMessage(Messages.CANT_FIND_PLAYER.toComponent(args[0]));
                return;
            }
            MongoPermsAPI.clear(p.getUniqueId());
            sender.sendMessage(Messages.RELOADED_PLAYER.toComponent(p.getName()));
            return;
        }

        Group.reloadGroups();
        sender.sendMessage(Messages.RELOADED_GROUPS.toComponent(Group.getGroups().size()));
    }

    @Override
    public int requiredArgs() {
        return -1;
    }

    @Override
    public String getUsage() {
        return null;
    }

}
