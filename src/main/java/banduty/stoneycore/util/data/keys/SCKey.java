package banduty.stoneycore.util.data.keys;

public record SCKey<T>(String name, Class<T> type) {

    // factory methods
    public static SCKey<Boolean> bool(String name) {
        return new SCKey<>(name, Boolean.class);
    }

    public static SCKey<Integer> integer(String name) {
        return new SCKey<>(name, Integer.class);
    }

    public static SCKey<Long> lng(String name) {
        return new SCKey<>(name, Long.class);
    }

    public static SCKey<Double> dbl(String name) {
        return new SCKey<>(name, Double.class);
    }

    public static SCKey<String> str(String name) {
        return new SCKey<>(name, String.class);
    }

    public static SCKey<Float> flt(String name) {
        return new SCKey<>(name, Float.class);
    }
}

