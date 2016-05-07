package mongoperms;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class MongoPermsAPI {

    private static final Map<UUID, Group> GROUPS_BY_PLAYER = Maps.newHashMap();
    private static final Map<String, UUID> UUID_MAP = Maps.newHashMap();

    private static final Gson gson = new Gson();

    /**
     * Get a Collection of all permissions by the player
     * <br>
     * NOTE: Due to Bungee/Bukkit compability, the player can be only provided by the UUID!
     * <br>
     * To get the real (not offline-) UUID of a player, use MongoPermsAPI#getUUID
     *
     * @param uuid the UUID to get the permissions of
     * @return List with permissions or null if group not found
     */
    public static Collection<String> getPermissionsOfPlayer(UUID uuid) {
        return getGroup(uuid).getPermissions();
    }

    public static Collection<String> getPermissions(String group) {
        return Group.getGroup(group).getPermissions();
    }

    /**
     * Get group where player is in
     * <br><br>
     * <b>NOTE:</b> The default is <i>"default"</i>
     *
     * @param uuid the UUID to get the group of
     * @return the group name
     */
    public static Group getGroup(UUID uuid) {
        if (GROUPS_BY_PLAYER.containsKey(uuid)) {
            return GROUPS_BY_PLAYER.get(uuid);
        } else {
            Group group = Group.getGroup(MongoConnection.getGroup(uuid));
            GROUPS_BY_PLAYER.put(uuid, group);
            return group;
        }
    }

    /**
     * Set group of player
     * @param uuid the UUID of the player
     * @param group new group
     */
    public static boolean setGroup(UUID uuid, Group group) {
        if (group == null) {
            return false;
        }
        GROUPS_BY_PLAYER.put(uuid, group);
        MongoConnection.setGroup(uuid, group.getName());
        return true;
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
            Preconditions.checkArgument(connection.getResponseCode() == HttpURLConnection.HTTP_OK, "Name is invalid. Response code: %s", String.valueOf(connection.getResponseCode()));
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
        GROUPS_BY_PLAYER.clear();
    }

    /**
     * Do not use this unless you know what you're doing
     *
     * @param uuid player to get cleaned
     */
    public static void clear(UUID uuid) {
        GROUPS_BY_PLAYER.remove(uuid);
        GROUPS_BY_PLAYER.put(uuid, getGroup(uuid));
    }

}
