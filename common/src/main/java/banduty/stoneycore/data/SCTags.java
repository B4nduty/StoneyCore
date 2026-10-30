package banduty.stoneycore.data;

import banduty.stoneycore.StoneyCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public enum SCTags {

    // Weapon Tags
    /**
     * If you want to have your Weapon deals x2 of its original baseDamage when hit from Behind
     */
    WEAPONS_DAMAGE_BEHIND("weapons_damage_behind"),
    /**
     * If you want to have your Weapon ignore Armor
     */
    WEAPONS_IGNORES_ARMOR("weapons_ignores_armor"),
    /**
     * If you want your Weapon disable the Shield/Parrying of another Weapon as Axes with the Shield
     */
    WEAPONS_DISABLE_SHIELD("weapons_disable_shield"),
    /**
     * If you want your Weapon bypass Shield/Parrying
     */
    WEAPONS_BYPASS_BLOCK("weapons_bypass_block"),
    /**
     * If you want your Weapon can Harvest
     */
    WEAPONS_HARVEST("weapons_harvest"),
    /**
     * If you want your Weapon to be Broken
     */
    BROKEN_WEAPONS("broken_weapons"),

    // Armor Tags
    /**
     * Armor that can Hide Name Tag
     */
    HIDE_NAME_TAG("hide_name_tag");

    private final TagKey<Item> tag;

    SCTags(String name) {
        this.tag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, name));
    }

    public TagKey<Item> getTag() {
        return tag;
    }
}