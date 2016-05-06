package mongoperms.bukkit.vault;

import mongoperms.MongoConnection;
import mongoperms.MongoConnection.Result;
import mongoperms.MongoPermsAPI;
import mongoperms.bukkit.MongoPerms;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

import java.util.List;
import java.util.Set;

public class VaultMongoBridge extends Permission {

    public VaultMongoBridge(Plugin vault, Plugin perms) {
        System.out.printf("[%s][Permission] %s hooked.", vault.getDescription().getName(), "MongoPerms");
        Bukkit.getServicesManager().register(Permission.class, this, perms, ServicePriority.Highest);
    }

    @SuppressWarnings("deprecation") //only for compiler... don't like errors / warnings :D
    @Override
    public String getName() {
        return "MongoPerms";
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isEnabled() {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasSuperPermsCompat() {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean playerHas(String s, String s1, String s2) {
        Player p = Bukkit.getPlayer(s1);
        return !(p == null || !p.isOnline()) && (p.hasPermission(s2) || MongoPermsAPI.getPermissionsOfPlayer(MongoPermsAPI.getUUID(s1)).contains(s2));
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean playerAdd(String s, String s1, String s2) {
        return false; //we're not providing this feature
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean playerRemove(String s, String s1, String s2) {
        return false; //we're not providing this feature
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean groupHas(String s, String s1, String s2) {
        List<String> permissions = MongoPermsAPI.getPermissions(s2);
        return permissions.contains(s2);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean groupAdd(String s, String s1, String s2) {
        return MongoConnection.addPermission(s1, s2) == Result.RESULT_SUCCESS;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean groupRemove(String s, String s1, String s2) {
        return MongoConnection.removePermission(s1, s2) == Result.RESULT_SUCCESS;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean playerInGroup(String s, String s1, String s2) {
        return getPrimaryGroup(s, s1).equals(s2);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean playerAddGroup(String s, String s1, String s2) {
        return false; //we're not providing this feature
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean playerRemoveGroup(String world, String player, String group) {
        return false; //we're not providing this feature
    }

    @SuppressWarnings("deprecation")
    @Override
    public String[] getPlayerGroups(String world, String player) {
        return new String[]{getPrimaryGroup(world, player)};
    }

    @SuppressWarnings("deprecation")
    @Override
    public String getPrimaryGroup(String world, String player) {
        return MongoPermsAPI.getGroup(MongoPermsAPI.getUUID(player));
    }

    @SuppressWarnings("deprecation")
    @Override
    public String[] getGroups() {
        Set<String> groups = MongoPerms.groups.keySet();
        return groups.toArray(new String[0]);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasGroupSupport() {
        return true;
    }
}
