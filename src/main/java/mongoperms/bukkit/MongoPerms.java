package mongoperms.bukkit;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.SneakyThrows;
import mongoperms.MongoConnection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
                groups.put(group, new ArrayList<>());
            }
        });

    }

    public static void generateAttachment(Player p) {

        try {
            Field f = CraftHumanEntity.class.getDeclaredField("perm");
            f.setAccessible(true);
            f.set(p, new CustomPermissibleBase(p));
        } catch (ReflectiveOperationException e1) {
            e1.printStackTrace();
        }

        PermissionAttachment attachment = p.addAttachment(instance);

        String group = MongoConnection.getGroup(getUUID(p.getName()));

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

    private static final Map<String, UUID> uuidMap = Maps.newHashMap();
    private static final Gson gson = new Gson();

    @SneakyThrows
    public static UUID getUUID(String p) {
        if (uuidMap.containsKey(p)) {
            return uuidMap.get(p);
        } else {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + p).openConnection();
            UUID uuid =  UUID.fromString(gson.fromJson(new BufferedReader(new InputStreamReader(connection.getInputStream())), JsonObject.class)
                    .get("id")
                    .getAsString()
                    .replaceAll("(?i)(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w+)", "$1-$2-$3-$4-$5"));
            uuidMap.put(p, uuid);
            return uuid;
        }
    }

}
