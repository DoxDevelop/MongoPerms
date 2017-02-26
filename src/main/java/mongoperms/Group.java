package mongoperms;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.mongodb.client.MongoCollection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bson.Document;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.mongodb.client.model.Filters.eq;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Group {

    @Getter
    private static final Set<Group> groups = Sets.newHashSet();

    private final String name;
    private final List<String> permissions;
    private final List<String> inherits;

    public Collection<String> getPermissions() {
        return getPermissions(true);
    }

    public Collection<String> getPermissions(boolean inherit) {
        Set<String> perms = Sets.newHashSet();
        Iterables.addAll(perms, permissions);

        if (inherit) {
            inherits.forEach(name -> {
                Group group = getGroup(name);
                if (group != null) {
                    Iterables.addAll(perms, group.permissions);
                }
            });
        }

        return Collections.unmodifiableCollection(perms);
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

        MongoCollection<Document> collection = MongoConnection.getCollection("perms", "groups");
        Document doc = collection.find(eq("group", name)).first();
        doc.put("inherits", inherits);
        collection.replaceOne(eq("group", name), doc);
    }

    public void removeInheritance(Group group) {
        inherits.remove(group.getName());

        MongoCollection<Document> collection = MongoConnection.getCollection("perms", "groups");
        Document doc = collection.find(eq("group", name)).first();
        doc.put("inherits", inherits);
        collection.replaceOne(eq("group", name), doc);
    }

    public boolean hasPermission(String node) {
        return permissions.contains(node);
    }

    public static Group create(String name, List<String> permissions, List<String> inherits) {
        Preconditions.checkArgument(getGroup(name) == null, "Group with name " + name + " already exists!");
        Group group = new Group(name, permissions, inherits);
        groups.add(group);
        return group;
    }

    public static Group getGroup(String name) {
        for (Group group : groups) {
            if (group.getName().equalsIgnoreCase(name)) {
                return group;
            }
        }
        return null;
    }

    public static boolean removeGroup(String name) {
        Group group = getGroup(name);
        if (name != null) {
            groups.remove(group);
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

    public static void saveGroups() {
        MongoCollection<Document> collection = MongoConnection.getCollection("perms", "groups");
        for (Group group : groups) {
            collection.replaceOne(eq("group", group.getName()), new Document("group", group.getName()).append("permissions", group.permissions).append("inherits", group.inherits));
        }
    }

}
