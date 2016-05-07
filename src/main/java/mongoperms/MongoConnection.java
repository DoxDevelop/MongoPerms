package mongoperms;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import lombok.Getter;
import org.bson.Document;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

public class MongoConnection {

    @Getter
    private static MongoClient client;
    @Getter
    private static boolean initialized = false;
    private static String DEFAULT_GROUP;

    public static void load(String host, int port, String defaultGroup, String username, String password, boolean isBungee, boolean useAuthentication) {
        Preconditions.checkArgument(!initialized, "MongoConnection already initialized.");

        if (useAuthentication) {
            client = new MongoClient(new ServerAddress(host, port), Collections.singletonList(MongoCredential.createCredential(username, "admin", password.toCharArray())));
        } else {
            client = new MongoClient(host, port);
        }

        DEFAULT_GROUP = defaultGroup;
        initialized = true;
        addGroup(DEFAULT_GROUP);
        loadGroups();
    }

    public static MongoCollection<Document> getCollection(String database, String name) {
        return client.getDatabase(database).getCollection(name);
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
                .append("permissions", Lists.newArrayList())
                .append("inherits", Lists.newArrayList())
        );

        Group.create(group, Lists.newArrayList(), Lists.newArrayList());
        return Result.RESULT_SUCCESS;
    }

    @SuppressWarnings("unchecked")
    public static void loadGroups() {
        MongoCollection<Document> collection = getCollection("perms", "groups");

        for (Document doc : collection.find()) {
            if (!doc.containsKey("inherits")) { //convert from 1.2
                Group.create(doc.getString("group"), (List<String>) doc.get("permissions"), Lists.newArrayList());
                collection.replaceOne(eq("group", doc.getString("group")), doc.append("inherits", Lists.newArrayList()));
            } else {
                Group.create(doc.getString("group"), (List<String>) doc.get("permissions"), (List<String>) doc.get("inherits"));
            }
        }

    }

    public static boolean removeGroup(String name) {
        MongoCollection<Document> collection = getCollection("perms", "groups");

        if (Group.removeGroup(name)) {
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

        Group.getGroup(group).addPermission(permission);

        List<String> perms = (List<String>) old.get("permissions");
        perms.add(permission);
        collection.replaceOne(eq("group", group), new Document("group", group).append("permissions", perms).append("inherits", old.get("inherits")));
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
            Group.getGroup(group).removePermission(permission);
            perms.remove(permission);
            collection.replaceOne(eq("group", group), new Document("group", group).append("permissions", perms).append("inherits", old.get("inherits")));
            return Result.RESULT_SUCCESS;
        }
        return Result.RESULT_UNKNOWN_PERMISSION;
    }

    public static Result setPermissions(String group, Set<String> permissions) {
        MongoCollection<Document> collection = getCollection("perms", "groups");

        Document old = collection.find(eq("group", group)).first();

        if (old == null) {
            return Result.RESULT_UNKNOWN_ERROR;
        }

        Group.getGroup(group).setPermissions(permissions);

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
