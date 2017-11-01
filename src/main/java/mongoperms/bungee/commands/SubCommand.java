package mongoperms.bungee.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;

public abstract class SubCommand {

    public abstract void execute(CommandSender sender, String[] args);

    public abstract int requiredArgs();

    public abstract String getUsage();

    public BaseComponent[] additionalComponents() {
        return null;
    }


}
