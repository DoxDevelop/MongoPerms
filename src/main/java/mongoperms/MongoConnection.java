package mongoperms;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import lombok.Getter;
import mongoperms.bungee.MongoPermsBungee;
import org.bson.Document;

import java.util.List;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

public class MongoConnection {

    @Getter
    private static MongoClient client;
    @Getter
    private static boolean initialized = false;
    private static String DEFAULT_GROUP;

    public static void load(String host, int port, String defaultGroup) {
        Preconditions.checkArgument(!initialized, "MongoConnection already initialized.");
        client = new MongoClient(host, port);
        DEFAULT_GROUP = defaultGroup;
        initialized = true;
        MongoPermsBungee.getInstance().reloadGroups();
    }

    public static MongoDatabase getDatabase(String name) {
        return client.getDatabase(name);
    }

    public static MongoCollection<Document> getCollection(String database, String name) {
        return getDatabase(database).getCollection(name);
    }

    public static void registerPlayer(UUID uuid) {
        MongoCollection<Document> collection = getCollection("perms", "users_in_groups");

        if (collection.find(eq("uuid", uuid.toString())).first() != null) {
            return;
        }

        collection.insertOne(new Document("uuid", uuid.toString())
                .append("group", DEFAULT_GROUP));
    }

    public static void setGroup(UUID uuid, String group) {
        MongoCollection<Document> collection = getCollection("perms", "users_in_groups");
        collection.replaceOne(eq("uuid", uuid.toString()), new Document("uuid", uuid.toString()).append("group", group));
    }

    public static String getGroup(UUID uuid) {
        MongoCollection<Document> collection = getCollection("perms", "users_in_groups");

        Document doc = collection.find(eq("uuid", uuid.toString())).first();

        return doc == null ? null : doc.getString("group");
    }

    public static Result addGroup(String group) {
        MongoCollection<Document> collection = getCollection("perms", "groups");

        if (collection.find(eq("group", group)).first() != null) {
            return Result.RESULT_GROUP_EXISTS;
        }

        collection.insertOne(new Document("group", group)
                .append("permissions", Lists.newArrayList()));
        return Result.RESULT_SUCCESS;
    }

    public static List<String> getGroups() {
        MongoCollection<Document> collection = getCollection("perms", "groups");

        List<String> groups = Lists.newArrayList();
        collection.find().iterator().forEachRemaining(document -> groups.add(document.getString("group")));
        return groups;
    }

    public static boolean removeGroup(String name) {
        MongoCollection<Document> collection = getCollection("perms", "groups");

        if (collection.find(Filters.eq("group", name)).first() != null) {
            collection.deleteOne(eq("group", name));
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static Result addPermission(String group, String permission) {
        MongoCollection<Document> collection = getCollection("perms", "groups");

        Document old = collection.find(eq("group", group)).first();

        if (old == null) {
            return Result.RESULT_UNKNOWN_GROUP;
        }

        List<String> perms = (List<String>) old.get("permissions");
        perms.add(permission);
        collection.replaceOne(eq("group", group), new Document("group", group).append("permissions", perms));
        return Result.RESULT_SUCCESS;
    }

    @SuppressWarnings("unchecked")
    public static Result removePermission(String group, String permission) {
        MongoCollection<Document> collection = getCollection("perms", "groups");

        Document old = collection.find(eq("group", group)).first();

        if (old == null) {
            return Result.RESULT_UNKNOWN_GROUP;
        }

        List<String> perms = (List<String>) old.get("permissions");
        if (perms.contains(permission)) {
            perms.remove(permission);
            collection.replaceOne(eq("group", group), new Document("group", group).append("permissions", perms));
            return Result.RESULT_SUCCESS;
        }
        return Result.RESULT_UNKNOWN_PERMISSION;
    }

    @SuppressWarnings("unchecked")
    public static List<String> getPermissions(String group) {
        MongoCollection<Document> collection = getCollection("perms", "groups");
        Document doc = collection.find(eq("group", group)).first();
        if (doc == null) {
            return Lists.newArrayList();
        }
        return (List<String>) doc.get("permissions");
    }

    public static Result setPermissions(String group, List<String> permissions) {
        MongoCollection<Document> collection = getCollection("perms", "groups");

        Document old = collection.find(eq("group", group)).first();

        if (old == null) {
            return Result.RESULT_UNKNOWN_ERROR;
        }

        old.put("permissions", permissions);
        collection.replaceOne(eq("group", group), old);
        return Result.RESULT_SUCCESS;
    }

    public enum Result {
        RESULT_SUCCESS,
        RESULT_UNKNOWN_GROUP,
        RESULT_UNKNOWN_PERMISSION,
        RESULT_GROUP_EXISTS,
        RESULT_UNKNOWN_ERROR
    }

}
