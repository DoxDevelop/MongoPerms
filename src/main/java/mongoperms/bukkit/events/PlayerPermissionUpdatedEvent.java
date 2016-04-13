package mongoperms.bukkit.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerPermissionUpdatedEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    public PlayerPermissionUpdatedEvent(Player who) {
        super(who);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
