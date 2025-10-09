package banduty.stoneycore.combat.range;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class RangedWeaponHandlers {
    private static final Map<String, IRangedWeaponHandler> HANDLERS = Collections.synchronizedMap(new HashMap<>());

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
