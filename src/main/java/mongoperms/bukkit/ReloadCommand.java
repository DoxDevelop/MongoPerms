package mongoperms.bukkit;

import com.google.common.collect.Lists;
import mongoperms.MongoConnection;
import mongoperms.MongoPermsAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ReloadCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("permissions.reload") && !sender.isOp()) {
            sender.sendMessage("§cYou are not allowed to use this command.");
            return true;
        }

        if (args.length > 0) {
            Player p = Bukkit.getPlayer(args[0]);
            if (p == null) {
                return true;
            }
            MongoPerms.unlogAttachment(p);
            MongoPerms.generateAttachment(p);
            MongoPermsAPI.clear(MongoPermsAPI.getUUID(p.getName()));
            Bukkit.getPluginManager().callEvent(new PlayerPermissionUpdatedEvent(p));
            return true;
        }

        Bukkit.getOnlinePlayers().forEach(MongoPerms::unlogAttachment);

        synchronized (MongoPerms.groups) {
            MongoPerms.groups.clear();
            MongoConnection.getGroups().forEach(group -> {
                List<String> permissions = MongoConnection.getPermissions(group);
                if (permissions != null) {
                    MongoPerms.groups.put(group, permissions);
                } else {
                    MongoPerms.groups.put(group, Lists.newArrayList());
                }
            });
        }

        MongoPermsAPI.clear();
        Bukkit.getPluginManager().callEvent(new PermissionUpdatedEvent(true));

        Bukkit.getOnlinePlayers().forEach(MongoPerms::generateAttachment);
        return false;
    }
}
