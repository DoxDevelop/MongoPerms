package mongoperms.bukkit.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@RequiredArgsConstructor
public class PermissionUpdatedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final boolean success;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
