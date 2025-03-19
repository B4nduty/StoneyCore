package banduty.stoneycore.items.armor.underarmor;

import banduty.stoneycore.items.armor.SCUnderArmorItem;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;

public class SCUnderArmor extends ArmorItem implements SCUnderArmorItem {
    double slashingResistance;
    double bludgeoningResistance;
    double piercingResistance;

    public SCUnderArmor(Settings settings, ArmorMaterial material, Type type,
                        double slashingResistance, double bludgeoningResistance, double piercingResistance) {
        super(material, type, settings);
        this.slashingResistance = slashingResistance;
        this.bludgeoningResistance = bludgeoningResistance;
        this.piercingResistance = piercingResistance;
    }

    @Override
    public double slashingResistance() {
        return slashingResistance;
    }

    @Override
    public double bludgeoningResistance() {
        return bludgeoningResistance;
    }

    @Override
    public double piercingResistance() {
        return piercingResistance;
    }
}
