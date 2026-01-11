package banduty.stoneycore.util.data.keys;

import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class NBTDataHelper {

    // ENTITY
    public static <T> void set(IEntityDataSaver entity, SCKey<T> key, T value) {
        set(entity.stoneycore$getPersistentData(), key, value);
    }

    public static <T> T get(IEntityDataSaver entity, SCKey<T> key, T defaultValue) {
        return get(entity.stoneycore$getPersistentData(), key, defaultValue);
    }

    // ITEM
    public static <T> void set(ItemStack stack, SCKey<T> key, T value) {
        set(stack.getOrCreateTag(), key, value);
    }

    public static <T> T get(ItemStack stack, SCKey<T> key, T defaultValue) {
        return get(stack.getTag(), key, defaultValue);
    }

    public static <T> void set(CompoundTag tag, SCKey<T> key, T value) {
        if (tag == null) return;
        if (value instanceof Boolean b) tag.putBoolean(key.name(), b);
        else if (value instanceof Integer i) tag.putInt(key.name(), i);
        else if (value instanceof Long l) tag.putLong(key.name(), l);
        else if (value instanceof Double d) tag.putDouble(key.name(), d);
        else if (value instanceof String s) tag.putString(key.name(), s);
        else if (value instanceof Float f) tag.putFloat(key.name(), f);
        else throw new IllegalArgumentException("Unsupported type: " + key.type());
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(CompoundTag tag, SCKey<T> key, T defaultValue) {
        if (tag == null || !tag.contains(key.name())) return defaultValue;

        Class<T> type = key.type();
        if (type == Float.class) return (T) (Float) tag.getFloat(key.name());
        if (type == Boolean.class) return (T) (Boolean) tag.getBoolean(key.name());
        if (type == Integer.class) return (T) (Integer) tag.getInt(key.name());
        if (type == Long.class) return (T) (Long) tag.getLong(key.name());
        if (type == Double.class) return (T) (Double) tag.getDouble(key.name());
        if (type == String.class) return (T) tag.getString(key.name());

        throw new IllegalArgumentException("Unsupported type: " + type);
    }
}
