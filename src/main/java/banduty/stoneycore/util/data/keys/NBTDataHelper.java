package banduty.stoneycore.util.data.keys;

import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.CheckReturnValue;

public class NBTDataHelper {

    // ENTITY
    public static <T> void set(IEntityDataSaver entity, SCKey<T> key, T value) {
        set(entity.stoneycore$getPersistentData(), key, value);
    }

    @CheckReturnValue
    public static <T> T get(IEntityDataSaver entity, SCKey<T> key, T defaultValue) {
        return get(entity.stoneycore$getPersistentData(), key, defaultValue);
    }

    // ITEM
    public static <T> void set(ItemStack stack, SCKey<T> key, T value) {
        set(stack.getOrCreateNbt(), key, value);
    }

    @CheckReturnValue
    public static <T> T get(ItemStack stack, SCKey<T> key, T defaultValue) {
        return get(stack.getNbt(), key, defaultValue);
    }

    public static <T> void set(NbtCompound nbt, SCKey<T> key, T value) {
        if (nbt == null) return;
        if (value instanceof Boolean b) nbt.putBoolean(key.name(), b);
        else if (value instanceof Integer i) nbt.putInt(key.name(), i);
        else if (value instanceof Long l) nbt.putLong(key.name(), l);
        else if (value instanceof Double d) nbt.putDouble(key.name(), d);
        else if (value instanceof String s) nbt.putString(key.name(), s);
        else if (value instanceof Float f) nbt.putFloat(key.name(), f);
        else throw new IllegalArgumentException("Unsupported type: " + key.type());
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(NbtCompound nbt, SCKey<T> key, T defaultValue) {
        if (nbt == null || !nbt.contains(key.name())) return defaultValue;

        Class<T> type = key.type();
        if (type == Float.class) return (T) (Float) nbt.getFloat(key.name());
        if (type == Boolean.class) return (T) (Boolean) nbt.getBoolean(key.name());
        if (type == Integer.class) return (T) (Integer) nbt.getInt(key.name());
        if (type == Long.class) return (T) (Long) nbt.getLong(key.name());
        if (type == Double.class) return (T) (Double) nbt.getDouble(key.name());
        if (type == String.class) return (T) nbt.getString(key.name());

        throw new IllegalArgumentException("Unsupported type: " + type);
    }
}
