package banduty.stoneycore.items.armor;

public interface SCUnderArmorItem {
    double slashingResistance();
    double bludgeoningResistance();
    double piercingResistance();

    default int getDefaultColor() {
        return 0;
    }
}