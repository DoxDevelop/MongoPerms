package mongoperms;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.SneakyThrows;
import net.md_5.bungee.api.Callback;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MongoPermsAPI {

    private static final Map<UUID, Group> GROUPS_BY_PLAYER = Maps.newHashMap();
    private static final Map<String, UUID> UUID_MAP = Maps.newHashMap();
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static final Gson gson = new Gson();

    /**
     * Get a Collection of all permissions by the player
     * <br>
     * NOTE: Due to Bungee/Bukkit compability, the player can be only provided by the UUID!
     * <br>
     * To get the real (not offline-) UUID of a player, use {@link MongoPermsAPI#getUUID(String)}
     *
     * @param uuid the UUID to get the permissions of
     * @return collection with permissions or null if group not found
     */
    public static Collection<String> getPermissionsOfPlayer(UUID uuid) {
        return getGroup(uuid).getPermissions();
    }


    /**
     * Get permissions of specified group
     *
     * @param group the group to get the permissions from
     * @return collection with all permissions
     */
    public static Collection<String> getPermissions(String group) {
        return Group.getGroup(group).getPermissions();
    }

    /**
     * Get group of specified player
     * <br><br>
     * Default group is <i>"default"</i>
     * <br><br>
     * <strong>NOTE</strong> this is not ran asynchronously
     * <br>
     *
     * @param uuid uuid of a player
     * @return group of specified player
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
     * Get group of specified player asynchronously
     *
     * @param uuid     of a player
     * @param consumer consumer accepting the group of the player
     * @see MongoPermsAPI#getGroup(UUID)
     */
    public static void getGroup(UUID uuid, Consumer<Group> consumer) {
        EXECUTOR.execute(() -> consumer.accept(getGroup(uuid)));
    }

    /**
     * Set group of player
     * <p>
     * <strong>NOTE</strong> this is not ran asynchronously
     *
     * @param uuid  UUID of player
     * @param group new group
     * @return false if group is null, otherwise true
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
     * Set group of player asynchronously
     *
     * @param uuid     UUID of player
     * @param group    new group
     * @param consumer false if group is null, otherwise true
     */
    public static void setGroup(UUID uuid, Group group, Consumer<Boolean> consumer) {
        EXECUTOR.execute(() -> consumer.accept(setGroup(uuid, group)));
    }

    /**
     * Get UUID of a player
     * <br>All UUID's are being cached
     *
     * @param name name of player to lookup
     * @return UUID of player
     * @throws IllegalArgumentException if name is invalid / can't be found
     */
    @SneakyThrows
    public static UUID getUUID(String name) {
        if (UUID_MAP.containsKey(name)) {
            return UUID_MAP.get(name);
        }
        HttpURLConnection connection = (HttpURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openConnection();
        Preconditions.checkArgument(connection.getResponseCode() == HttpURLConnection.HTTP_OK, "Name is invalid. Response code: %s", String.valueOf(connection.getResponseCode()));
        UUID uuid = UUID.fromString(gson.fromJson(new BufferedReader(new InputStreamReader(connection.getInputStream())), JsonObject.class)
                .get("id")
                .getAsString()
                .replaceAll("(?i)(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w+)", "$1-$2-$3-$4-$5"));
        UUID_MAP.put(name, uuid);
        return uuid;
    }

    /**
     * Get UUID of a player asynchronously
     *
     * @see MongoPermsAPI#getUUID(String)
     *
     * @param name name of player to lookup
     * @param consumer accepting the uuid of player
     * @throws IllegalArgumentException if name is invalid / can't be found
     */
    public static void getUUID(String name, Consumer<UUID> consumer) {
        EXECUTOR.execute(() -> consumer.accept(getUUID(name)));
    }

    /**
     * Clears map of all known users and their groups
     */
    public static void clear() {
        GROUPS_BY_PLAYER.clear();
    }

    /**
     * Removes known group from player and retrieves group of player form database
     *
     * @param uuid player to be cleaned up
     */
    public static void clear(UUID uuid) {
        GROUPS_BY_PLAYER.remove(uuid);
        GROUPS_BY_PLAYER.put(uuid, getGroup(uuid));
    }

}
