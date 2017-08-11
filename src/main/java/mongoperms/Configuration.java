package mongoperms;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Configuration {

    private static final Yaml YAML;

    static {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        YAML = new Yaml(new SafeConstructor(), new Representer(), options);
    }

    private boolean needOp;
    private String permissionNode;
    private String mongoHost;
    private int mongoPort;
    private String defaultGroup;
    private boolean useVault;
    private String mongoUsername;
    private String mongoPassword;
    private boolean useAuthentication;

    public static Configuration load() throws IOException {
        CustomMapImpl map = YAML.loadAs(new InputStreamReader(new FileInputStream(new File("plugins/MongoPerms/config.yml")), "UTF-8"), CustomMapImpl.class);
        Configuration config = new Configuration();

        config.needOp = map.get("need-op");
        config.permissionNode = map.get("all-permissions");
        config.defaultGroup = map.get("default-group-name");
        config.mongoHost = map.get("host");
        config.mongoPort = map.get("port");
        config.useVault = map.get("use-vault");
        config.useVault = map.get("use-vault");
        config.mongoUsername = map.get("username");
        config.mongoPassword = map.get("password");
        config.useAuthentication = map.get("useAuthentication");

        return config;
    }

    private static class CustomMapImpl extends LinkedHashMap<String, Object> {

        @SuppressWarnings("unchecked")
        public <T> T get(String key) {
            return (T) super.get(key);
        }

    }

}
