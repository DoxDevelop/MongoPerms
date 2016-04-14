package mongoperms.bukkit;

import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.ServerOperator;

public class CustomPermissibleBase extends PermissibleBase {

    public CustomPermissibleBase(ServerOperator opable) {
        super(opable);
    }

    @Override
    public boolean hasPermission(String inName) {

        if (MongoPerms.getSettings().getPermissionNode().equals("none")) {
            return super.hasPermission(inName);
        }

        return super.hasPermission(MongoPerms.getSettings().getPermissionNode()) || super.hasPermission(inName);
    }

    @Override
    public boolean hasPermission(Permission perm) {

        if (MongoPerms.getSettings().getPermissionNode().equals("none")) {
            return super.hasPermission(perm);
        }

        return super.hasPermission(MongoPerms.getSettings().getPermissionNode()) || super.hasPermission(perm);
    }

}
