package mongoperms.bungee.commands;

import com.google.common.base.Joiner;
import mongoperms.Messages;
import mongoperms.bungee.MongoPermsBungee;
import mongoperms.bungee.commands.sub.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.util.CaseInsensitiveMap;

import java.util.Arrays;
import java.util.Map;

public class PermissionsCommand extends Command {

    private static final Map<String, SubCommand> COMMANDS = new CaseInsensitiveMap<SubCommand>() {
        {
            put("add", new AddCommand());
            put("addall", new AddAllCommand());
            put("addgroup", new AddGroupCommand());
            put("addinheritance", new AddInheritanceCommand());
            put("group", new GroupCommand());
            put("groups", new GroupsCommand());
            put("putall", new PutAllCommand());
            put("reload", new ReloadCommand());
            put("remove", new RemoveCommand());
            put("removegroup", new RemoveGroupCommand());
            put("removeinheritance", new RemoveInheritanceCommand());
            put("setgroup", new SetGroupCommand());
            put("user", new UserCommand());
        }
    };

    public PermissionsCommand() {
        super("perms", "mongoperms.perms");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ProxyServer.getInstance().getScheduler().runAsync(MongoPermsBungee.getInstance(), () -> {
            if (args.length == 0) {
                sender.sendMessage(new ComponentBuilder("Available options: " + Joiner.on(", ").join(COMMANDS.keySet())).color(ChatColor.YELLOW).create());
                sender.sendMessage(Messages.MORE_INFORMATION.toComponent("/perms <SubCommand>"));
                return;
            }

            String subCommand = args[0];
            if (!COMMANDS.containsKey(subCommand)) {
                sender.sendMessage(Messages.UNKNOWN_SUBCOMMAND.toComponent(subCommand));
                return;
            }

            SubCommand command = COMMANDS.get(subCommand);
            if (command.requiredArgs() > 0 && args.length - 1 < command.requiredArgs()) {
                sender.sendMessage(Messages.USAGE.toComponent(command.getUsage()));
                return;
            }

            String[] newArgs = new String[0];
            if (args.length > 1) {
                newArgs = Arrays.copyOfRange(args, 1, args.length);
            }

            command.execute(sender, newArgs);
        });
    }

}
