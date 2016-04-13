package mongoperms.bukkit;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.SneakyThrows;
import mongoperms.MongoConnection;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static mongoperms.MongoPermsAPI.getUUID;

public class MongoPerms extends JavaPlugin {

    @Getter
    private static MongoPerms instance;

    public static final Map<UUID, PermissionAttachment> attachments = Maps.newLinkedHashMap();
    public static final Map<String, List<String>> groups = Maps.newLinkedHashMap();

    @Override
    public void onEnable() {
        instance = this;

        MongoConnection.load();

        getServer().getPluginManager().registerEvents(new MongoListener(), this);
        getCommand("permrl").setExecutor(new ReloadCommand());

        MongoConnection.getGroups().forEach(group -> {
            List<String> permissions = MongoConnection.getPermissions(group);
            if (permissions != null) {
                groups.put(group, permissions);
            } else {
                groups.put(group, Lists.newArrayList());
            }
        });

        System.out.println("[MongoPerms] Enabled version: " + getDescription().getVersion());

    }

    public static void generateAttachment(Player p) {

        if (p == null || !p.isOnline()) {
            return; //avoid bugs
        }

        try {
            Field f = getCraftHumanEntityClass().getDeclaredField("perm");
            f.setAccessible(true);
            f.set(p, new CustomPermissibleBase(p));
        } catch (ReflectiveOperationException e1) {
            e1.printStackTrace();
        }

        PermissionAttachment attachment = p.addAttachment(instance);

        String group = MongoConnection.getGroup(getUUID(p.getName()));

        if (group == null) {
            group = "default";
        }

        if (groups.containsKey(group)) {
            groups.get(group).forEach(s -> {
                if (s.startsWith("-")) {
                    attachment.setPermission(s.substring(1, s.length()), false);
                } else {
                    attachment.setPermission(s, true);
                }
            });
        }

        attachments.put(getUUID(p.getName()), attachment);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private static Class<? extends HumanEntity> getCraftHumanEntityClass() {
        return (Class<? extends HumanEntity>) Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".entity.CraftHumanEntity");
    }

    public static void unlogAttachment(Player p) {
        PermissionAttachment attachment = attachments.remove(getUUID(p.getName()));

        if (attachment == null) {
            System.err.println("[MongoPerms]" + p.getName() + "'s attachment is null?");
            return;
        }

        p.removeAttachment(attachment);
    }

}
