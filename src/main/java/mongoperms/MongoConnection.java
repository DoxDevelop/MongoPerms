package mongoperms;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mongodb.client.model.Filters.*;

public class MongoConnection {

    @Getter
    private static MongoClient client;
    private static boolean initialized = false;

    public static void load() {
        Preconditions.checkArgument(!initialized, "MongoConnection already initialized.");
        client = new MongoClient("localhost"); //TODO add configuration support
        initialized = true;
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
                .append("group", "default"));
    }

    public static void setGroup(UUID uuid, String group) {
        MongoCollection<Document> collection = getCollection("perms", "users_in_groups");
        collection.replaceOne(eq("uuid", uuid.toString()), new Document("uuid", uuid.toString()).append("group", group));
    }

    public static String getGroup(UUID uuid) {
        MongoCollection<Document> collection = getCollection("perms", "users_in_groups");

        return collection.find(eq("uuid", uuid.toString())).first().getString("group");
    }

    public static void addGroup(String group) {
        MongoCollection<Document> collection = getCollection("perms", "groups");

        collection.insertOne(new Document("group", group)
                .append("permissions", new ArrayList<>()));
    }

    public static List<String> getGroups() {
        MongoCollection<Document> collection = getCollection("perms", "groups");

        List<String> groups = Lists.newArrayList();
        collection.find().iterator().forEachRemaining(document -> groups.add(document.getString("group")));
        return groups;
    }

    public static void removeGroup(String name) {
        MongoCollection<Document> collection = getCollection("perms", "groups");

        collection.deleteOne(eq("group", name));
    }

    @SuppressWarnings("unchecked")
    public static void addPermission(String group, String permission) {
        MongoCollection<Document> collection = getCollection("perms", "groups");

        Document old = collection.find(eq("group", group)).first();
        List<String> perms = (List<String>) old.get("permissions");
        perms.add(permission);
        collection.replaceOne(eq("group", group), new Document("group", group).append("permissions", perms));
    }

    @SuppressWarnings("unchecked")
    public static void removePermission(String group, String permission) {
        MongoCollection<Document> collection = getCollection("perms", "groups");

        Document old = collection.find(eq("group", group)).first();
        List<String> perms = (List<String>) old.get("permissions");
        if (perms.contains(permission)) {
            perms.remove(permission);
            collection.replaceOne(eq("group", group), new Document("group", group).append("permissions", perms));
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> getPermissions(String group) {
        MongoCollection<Document> collection = getCollection("perms", "groups");
        if (collection.find(eq("group", group)).first() == null) {
            return Lists.newArrayList();
        }
        return (List<String>) collection.find(eq("group", group)).first().get("permissions");
    }

}
