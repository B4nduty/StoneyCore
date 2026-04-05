package banduty.stoneycore.platform;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.combat.melee.ICombatHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

public class ServiceLoaderHelper {
    @SuppressWarnings("unchecked")
    public static <T> T loadHighestPriority(Class<T> clazz) {
        List<T> services = new ArrayList<>();
        ServiceLoader.load(clazz).forEach(services::add);

        T selected = null;

        // Only try to select from services if any exist
        if (!services.isEmpty()) {
            // Filter available services and sort by priority (highest first)
            selected = services.stream()
                    .filter(service -> {
                        try {
                            Method method = service.getClass().getMethod("isAvailable");
                            return (boolean) method.invoke(service);
                        } catch (NoSuchMethodException e) {
                            // No isAvailable method, assume available
                            return true;
                        } catch (Exception e) {
                            StoneyCore.LOG.warn("Error checking availability for {}: {}",
                                    service.getClass().getName(), e.getMessage());
                            return false;
                        }
                    })
                    .max(Comparator.comparingInt(service -> {
                        try {
                            Method method = service.getClass().getMethod("getPriority");
                            return (int) method.invoke(service);
                        } catch (Exception e) {
                            return 0;
                        }
                    }))
                    .orElse(null);
        }

        // If no service selected (either no services or none available), create a default instance
        if (selected == null) {
            StoneyCore.LOG.debug("No suitable ICombatHelper service found, using default interface implementation");
            try {
                selected = clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                StoneyCore.LOG.warn("Could not instantiate {} using default constructor, using anonymous fallback", clazz.getName());
                if (clazz == ICombatHelper.class) {
                    selected = (T) new ICombatHelper() {};
                } else {
                    throw new NullPointerException("Failed to load any service for " + clazz.getName());
                }
            }
        }

        StoneyCore.LOG.debug("Selected {} for service {}", selected.getClass().getName(), clazz);
        return selected;
    }
}