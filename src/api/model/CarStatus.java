package api.model;

public enum CarStatus {
    AVAILABLE,
    RENTED;

    public static CarStatus fromGreek(String s) {
        if (s == null) return AVAILABLE;
        s = s.trim();
        if (s.equalsIgnoreCase("Διαθέσιμο")) return AVAILABLE;
        if (s.equalsIgnoreCase("Ενοικιασμένο")) return RENTED;
        // fallback
        return CarStatus.valueOf(s.toUpperCase());
    }

    public String toGreek() {
        return this == AVAILABLE ? "Διαθέσιμο" : "Ενοικιασμένο";
    }
}
