package mongoperms.bungee;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Configuration {

    private boolean needOp;
    private String permissionNode;
    private String mongoHost;
    private int mongoPort;
    private String defaultGroup;
    private boolean useVault;
    private String mongoUsername;
    private String mongoPassword;
    private boolean useAuthentication;

    @SneakyThrows
    public static Configuration load(Plugin plugin) {
        Configuration config = new Configuration();
        net.md_5.bungee.config.Configuration cfg = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(plugin.getDataFolder(), "config.yml"));

        config.needOp = cfg.getBoolean("need-op");
        config.permissionNode = cfg.getString("all-permissions");
        config.defaultGroup = cfg.getString("default-group-name");
        config.mongoHost = cfg.getString("host");
        config.mongoPort = cfg.getInt("port");
        config.useVault = cfg.getBoolean("use-vault");
        config.mongoUsername = cfg.getString("username");
        config.mongoPassword = cfg.getString("password");
        config.useAuthentication = cfg.getBoolean("useAuthentication");

        cfg.getSection("Gruppen_Rechte").getKeys();

        return config;
    }

}

