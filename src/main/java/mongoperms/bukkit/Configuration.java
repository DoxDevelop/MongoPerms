package mongoperms.bukkit;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Configuration {

    private boolean needOp;
    private String permissionNode;
    private String mongoHost;
    private int mongoPort;
    private String defaultGroup;
    private boolean useVault;

    public static Configuration load(Plugin plugin) {
        Configuration config = new Configuration();
        FileConfiguration cfg = plugin.getConfig();

        config.needOp = cfg.getBoolean("need-op");
        config.permissionNode = cfg.getString("all-permissions");
        config.defaultGroup = cfg.getString("default-group-name");
        config.mongoHost = cfg.getString("host");
        config.mongoPort = cfg.getInt("port");
        config.useVault = cfg.getBoolean("use-vault");

        return config;
    }

}
