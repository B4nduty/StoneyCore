package banduty.stoneycore.combat.range;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class RangedWeaponHandlers {
    // Looked up on every use tick; ConcurrentHashMap reads don't take a lock.
    private static final Map<String, IRangedWeaponHandler> HANDLERS = new ConcurrentHashMap<>();

    private RangedWeaponHandlers() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void register(IRangedWeaponHandler handler) {
        if (handler == null || handler.getTypeId() == null) return;
        HANDLERS.put(handler.getTypeId(), handler);
    }

    public static Optional<IRangedWeaponHandler> get(String typeId) {
        if (typeId == null) return Optional.empty();
        return Optional.ofNullable(HANDLERS.get(typeId));
    }
}
