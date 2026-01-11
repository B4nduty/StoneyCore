package banduty.stoneycore.smithing;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public record StackIngredient(ItemStack stack, TagKey<Item> tag) {

    public boolean test(ItemStack input) {
        if (tag != null) {
            return input.is(tag);
        }
        if (input.isEmpty()) return false;
        if (!ItemStack.isSameItem(stack, input)) return false;

        if (stack.hasTag()) {
            CompoundTag expected = stack.getTag();
            CompoundTag actual = input.getTag();
            if (actual == null) return false;
            return expected.equals(actual);
        }

        return true;
    }

    public JsonElement toJson() {
        JsonObject obj = new JsonObject();

        if (tag != null) {
            obj.addProperty("tag", tag.location().toString());
            obj.addProperty("count", stack.getCount());
        } else {
            obj.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            obj.addProperty("count", stack.getCount());

            if (stack.hasTag()) {
                obj.addProperty("nbt", stack.getTag().copy().toString());
            }
        }

        return obj;
    }

    public static StackIngredient fromJson(JsonObject json) {
        if (json.has("tag")) {
            TagKey<Item> tag = TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation(GsonHelper.getAsString(json, "tag")));
            int count = GsonHelper.getAsInt(json, "count", 1);
            ItemStack stack = new ItemStack(net.minecraft.world.item.Items.AIR, count);
            return new StackIngredient(stack, tag);
        } else {
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(GsonHelper.getAsString(json, "item")));
            int count = GsonHelper.getAsInt(json, "count", 1);

            ItemStack stack = new ItemStack(item, count);
            if (json.has("nbt")) {
                try {
                    CompoundTag tag = TagParser.parseTag(GsonHelper.getAsString(json, "nbt"));
                    stack.setTag(tag);
                } catch (Exception e) {
                    throw new RuntimeException("Invalid NBT data for StackIngredient: " + json, e);
                }
            }

            return new StackIngredient(stack, null);
        }
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(tag != null);
        if (tag != null) {
            buf.writeResourceLocation(tag.location());
            buf.writeVarInt(stack.getCount());
        } else {
            buf.writeItem(stack);
        }
    }

    public static StackIngredient read(FriendlyByteBuf buf) {
        boolean isTag = buf.readBoolean();
        if (isTag) {
            ResourceLocation tagId = buf.readResourceLocation();
            int count = buf.readVarInt();
            TagKey<Item> tag = TagKey.create(BuiltInRegistries.ITEM.key(), tagId);
            ItemStack stack = new ItemStack(net.minecraft.world.item.Items.AIR, count);
            return new StackIngredient(stack, tag);
        } else {
            ItemStack stack = buf.readItem();
            return new StackIngredient(stack, null);
        }
    }

    public List<ItemStack> asItemStacks() {
        if (tag != null) {
            return BuiltInRegistries.ITEM.stream()
                    .filter(item -> item.builtInRegistryHolder().is(tag))
                    .map(item -> {
                        ItemStack s = new ItemStack(item);
                        s.setCount(stack.getCount());
                        return s;
                    })
                    .collect(Collectors.toList());
        } else if (!stack.isEmpty()) {
            return Collections.singletonList(stack);
        }
        return Collections.emptyList();
    }
}