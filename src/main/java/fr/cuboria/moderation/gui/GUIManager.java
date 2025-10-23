package fr.cuboria.moderation.gui;

import fr.cuboria.moderation.models.Sanction;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIManager {

    private final Map<UUID, GUIContext> contexts;

    public GUIManager() {
        this.contexts = new HashMap<>();
    }

    public void setContext(UUID staffUuid, Player target, Sanction.SanctionType type) {
        contexts.put(staffUuid, new GUIContext(target, type));
    }

    public GUIContext getContext(UUID staffUuid) {
        return contexts.get(staffUuid);
    }

    public void clearContext(UUID staffUuid) {
        contexts.remove(staffUuid);
    }

    public static class GUIContext {
        private final Player target;
        private final Sanction.SanctionType type;

        public GUIContext(Player target, Sanction.SanctionType type) {
            this.target = target;
            this.type = type;
        }

        public Player getTarget() {
            return target;
        }

        public Sanction.SanctionType getType() {
            return type;
        }
    }
}
