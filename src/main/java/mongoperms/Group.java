package mongoperms;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.mongodb.client.MongoCollection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bson.Document;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Group {

    @Getter
    private static final Set<Group> groups = Sets.newHashSet();

    private final String name;
    private final List<String> permissions;
    private final List<String> inherits;
    private final Set<String> permissionsWithInheritances = Sets.newHashSet();

    public Collection<String> getPermissions() {
        return Collections.unmodifiableCollection(permissionsWithInheritances);
    }

    public void addPermission(String node) {
        permissions.add(node);
    }

    public void removePermission(String node) {
        if (permissions.contains(node)) {
            permissions.remove(node);
        }
    }

    public void setPermissions(Collection<String> permissions) {
        this.permissions.clear();
        Iterables.addAll(this.permissions, permissions);
    }

    public void addAll(Collection<String> permissions) {
        Iterables.addAll(this.permissions, permissions);
    }

    public void addInheritance(Group group) {
        this.inherits.add(group.getName());

        reloadInheritances();

        MongoCollection<Document> collection = MongoConnection.getCollection("perms", "groups");
        Document doc = collection.find(eq("group", name)).first();
        doc.put("inherits", inherits);
        collection.replaceOne(eq("group", name), doc);
    }

    public void removeInheritance(Group group) {
        inherits.remove(group.getName());

        reloadInheritances();

        MongoCollection<Document> collection = MongoConnection.getCollection("perms", "groups");
        Document doc = collection.find(eq("group", name)).first();
        doc.put("inherits", inherits);
        collection.replaceOne(eq("group", name), doc);
    }

    private void reloadInheritances() {
        permissionsWithInheritances.clear();
        Iterables.addAll(permissionsWithInheritances, permissions);

        inherits.forEach(name -> getGroup(name).ifPresent(group -> Iterables.addAll(permissionsWithInheritances, group.permissions)));
    }

    public boolean hasPermission(String node) {
        return permissions.contains(node);
    }

    public static Optional<Group> getGroup(String name) {
        return groups.stream().filter(group -> group.getName().equalsIgnoreCase(name)).findAny();
    }

    public static boolean removeGroup(String name) {
        Optional<Group> group = getGroup(name);
        if (group.isPresent()) {
            groups.remove(group.get());
            return true;
        }
        return false;
    }

    public static void reloadGroups() {
        synchronized (groups) {
            saveGroups();
            groups.clear();
            MongoConnection.loadGroups();
        }
    }

    public static void create(String name, List<String> permissions, List<String> inherits) {
        Group group = new Group(name, permissions, inherits);
        groups.add(group);
    }

    public static void saveGroups() {
        MongoCollection<Document> collection = MongoConnection.getCollection("perms", "groups");
        for (Group group : groups) {
            collection.replaceOne(eq("group", group.getName()), new Document("group", group.getName()).append("permissions", group.permissions).append("inherits", group.inherits));
        }
    }

}
