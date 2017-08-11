package mongoperms.bukkit.command;

import mongoperms.MongoPermsAPI;
import mongoperms.Group;
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
            sender.sendMessage("§cYou are not allowed to use this command.");
            return true;
        }

        if (MongoPerms.getSettings().isNeedOp() && !sender.isOp()) {
            sender.sendMessage("§cYou need op to execute this command.");
            return true;
        }

        if (args.length > 0) {
            Player p = Bukkit.getPlayer(args[0]);
            if (p == null || !p.isOnline()) {
                sender.sendMessage("§cCan't find player: " + args[0]);
                return true;
            }
            MongoPerms.unlogAttachment(p);
            MongoPerms.generateAttachment(p);
            MongoPermsAPI.clear(getUUID(p.getName()));
            Bukkit.getPluginManager().callEvent(new PlayerPermissionUpdatedEvent(p));
            sender.sendMessage("§aPlayer " + p.getName() + " has been reloaded.");
            return true;
        }

        Bukkit.getOnlinePlayers().forEach(MongoPerms::unlogAttachment);

        sender.sendMessage("§a" + Group.getGroups().size() + " groups loaded.");
        MongoPermsAPI.clear();
        Bukkit.getPluginManager().callEvent(new PermissionUpdatedEvent());

        Bukkit.getOnlinePlayers().forEach(MongoPerms::generateAttachment);
        sender.sendMessage("§a" + MongoPerms.attachments.size() + " players registered.");
        return false;
    }

}
