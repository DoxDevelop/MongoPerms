package mongoperms;

import net.md_5.bungee.api.chat.TextComponent;

public class Messages {

    public static TextComponent NO_GROUP(String group) {
        return new TextComponent("§cNo group found with name: " + group);
    }

    public static TextComponent NO_PERMISSIONS_FOUND(String group) {
        return new TextComponent("§cNo permissions found in group: " + group);
    }

}
