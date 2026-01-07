package api.storage;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class PathsConfig {
    private PathsConfig(){}

    public static final Path DATA_DIR = Paths.get("data");
    public static final Path USERS = DATA_DIR.resolve("users.csv");
    public static final Path VEHICLES = DATA_DIR.resolve("vehicles_with_plates.csv");
    public static final Path CUSTOMERS = DATA_DIR.resolve("customers.csv");
    public static final Path RENTALS = DATA_DIR.resolve("rentals.csv");
    public static final Path META = DATA_DIR.resolve("meta.properties");
}
