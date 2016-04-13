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
        //override to check * permission which is not usually implemented by bukkit
        return super.hasPermission("*") || super.hasPermission(inName);
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return super.hasPermission("*") || super.hasPermission(perm);
    }

}
