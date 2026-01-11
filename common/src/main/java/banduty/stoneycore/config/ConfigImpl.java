package banduty.stoneycore.config;

public abstract class ConfigImpl implements IConfig {
    @Override
    public CombatOptions combatOptions() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public VisualOptions visualOptions() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public TechnicalOptions technicalOptions() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public LandOptions landOptions() {
        throw new UnsupportedOperationException("Not implemented");
    }
}