package mongoperms.bukkit.command;

import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Command {

    String name();

    String description();

    String usage() default "";

    String[] aliases() default {};

    String permission() default "";

    String permissionMessage() default "";

    Class<? extends TabCompleter> tabCompleter() default DefaultCompleter.class;

    final class DefaultCompleter implements TabCompleter {

        @Override
        public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
            return Lists.newArrayList();
        }

    }

}
