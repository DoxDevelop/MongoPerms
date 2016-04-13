package mongoperms;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MongoPermsAPI {

    private static final Map<UUID, String> GROUPS_BY_PLAYER = Maps.newHashMap();
    private static final Map<String, List<String>> PERMISSIONS_BY_GROUP = Maps.newHashMap();
    private static final Map<String, UUID> UUID_MAP = Maps.newHashMap();

    private static final Gson gson = new Gson();

    /**
     * Get a List of all permissions by the player
     * <br>
     * NOTE: Due to Bungee/Bukkit compability, the player can be only provided by the UUID!
     * <br>
     * To get the real (not offline-) UUID of a player, use MongoPermsAPI#getUUID
     *
     * @param uuid the UUID to get the permissions of
     * @return List with permissions or null if group not found
     */
    public static List<String> getPermissionsOfPlayer(UUID uuid) {
        String group = getGroup(uuid);
        if (!PERMISSIONS_BY_GROUP.containsKey(group)) {
            PERMISSIONS_BY_GROUP.put(group, MongoConnection.getPermissions(group));
        }
        return ImmutableList.copyOf(PERMISSIONS_BY_GROUP.get(group));
    }

    public static List<String> getPermissions(String group) {
        List<String> permissions = null;
        for (String groupName : PERMISSIONS_BY_GROUP.keySet()) {
            if (group.equalsIgnoreCase(groupName)) {
                permissions = PERMISSIONS_BY_GROUP.get(groupName);
                break;
            }
        }
        if (permissions == null) {
            PERMISSIONS_BY_GROUP.put(group, MongoConnection.getPermissions(group)); //May cause exception TODO FIX!
            permissions = PERMISSIONS_BY_GROUP.get(group);
        }
        return ImmutableList.copyOf(permissions);
    }

    /**
     * Get group name where player is in
     * <br><br>
     * <b>NOTE:</b> The default is <i>"default"</i>
     *
     * @param uuid thee UUID to get the group of
     * @return the group name
     */
    public static String getGroup(UUID uuid) {
        String group;
        if (GROUPS_BY_PLAYER.containsKey(uuid)) {
            group = GROUPS_BY_PLAYER.get(uuid);
        } else {
            group = MongoConnection.getGroup(uuid);
            GROUPS_BY_PLAYER.put(uuid, MongoConnection.getGroup(uuid));
        }
        return group;
    }

    /**
     * @see MongoConnection#setGroup(UUID, String)
     */
    public static void setGroup(UUID uuid, String group) {
        MongoConnection.setGroup(uuid, group);
    }

    /**
     * Method to get the UUID of a player
     * <br>All UUID's are being cached
     *
     * @param name name of the player
     * @return the UUID of the player
     * @throws IllegalArgumentException if name is invalid / can't be found
     */
    @SneakyThrows
    public static UUID getUUID(String name) {
        if (UUID_MAP.containsKey(name)) {
            return UUID_MAP.get(name);
        } else {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openConnection();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IllegalArgumentException("Name is invalid. Response code: " + connection.getResponseCode());
            }
            UUID uuid = UUID.fromString(gson.fromJson(new BufferedReader(new InputStreamReader(connection.getInputStream())), JsonObject.class)
                    .get("id")
                    .getAsString()
                    .replaceAll("(?i)(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w+)", "$1-$2-$3-$4-$5"));
            UUID_MAP.put(name, uuid);
            return uuid;
        }
    }

    /**
     * Do not use this unless you know what you're doing
     */
    public static void clear() {
        PERMISSIONS_BY_GROUP.clear();
        GROUPS_BY_PLAYER.clear();
    }

    /**
     * Do not use this unless you know what you're doing
     * @param uuid player to get cleaned
     */
    public static void clear(UUID uuid) {
        GROUPS_BY_PLAYER.remove(uuid);
    }

}
