package mongoperms.bukkit.command;

import mongoperms.Group;
import mongoperms.Messages;
import mongoperms.MongoPermsAPI;
import mongoperms.bukkit.MongoPerms;
import mongoperms.bukkit.events.PermissionUpdatedEvent;
import mongoperms.bukkit.events.PlayerPermissionUpdatedEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static mongoperms.MongoPermsAPI.getUUID;

@mongoperms.bukkit.command.Command(name = "permrl", description = "reloads all groups or player permissions", aliases = {"permreload", "permissionreload"})
public class ReloadCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("mongoperms.reload")) {
            sender.sendMessage(Messages.NO_PERMISSION.toString());
            return true;
        }

        if (MongoPerms.getSettings().isNeedOp() && !sender.isOp()) {
            sender.sendMessage(Messages.NO_OP.toString());
            return true;
        }

        if (args.length > 0) {
            Player p = Bukkit.getPlayer(args[0]);

            if (p == null || !p.isOnline()) {
                sender.sendMessage(Messages.CANT_FIND_PLAYER.toString(args[0]));
                return true;
            }

            MongoPerms.unLogAttachment(p);
            MongoPerms.generateAttachment(p);
            MongoPermsAPI.clear(getUUID(p.getName()));
            Bukkit.getPluginManager().callEvent(new PlayerPermissionUpdatedEvent(p));
            sender.sendMessage(Messages.RELOADED_PLAYER.toString(p.getName()));
            return true;
        }

        Bukkit.getOnlinePlayers().forEach(MongoPerms::unLogAttachment);

        sender.sendMessage(Messages.RELOADED_GROUPS.toString(Group.getGroups().size()));
        MongoPermsAPI.clear();
        Bukkit.getPluginManager().callEvent(new PermissionUpdatedEvent());

        Bukkit.getOnlinePlayers().forEach(MongoPerms::generateAttachment);
        sender.sendMessage(Messages.REGISTERED_PLAYERS.toString(MongoPerms.ATTACHMENTS.size()));
        return false;
    }

}
