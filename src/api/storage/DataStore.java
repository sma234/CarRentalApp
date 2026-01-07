package api.storage;

import api.model.Car;
import api.model.CarStatus;
import api.model.Customer;
import api.model.Employee;
import api.model.Rental;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class DataStore {

    public DataStore() {}

    public void ensureInitialized() throws IOException {
        if (!Files.exists(PathsConfig.DATA_DIR)) {
            Files.createDirectories(PathsConfig.DATA_DIR);
        }
        if (!Files.exists(PathsConfig.USERS)) {
            writeDefaultUsers();
        }
        if (!Files.exists(PathsConfig.VEHICLES)) {
            writeDefaultVehicles();
        }
        if (!Files.exists(PathsConfig.CUSTOMERS)) {
            writeDefaultCustomers();
        }
        if (!Files.exists(PathsConfig.RENTALS)) {
            writeDefaultRentals();
        }
        if (!Files.exists(PathsConfig.META)) {
            writeDefaultMeta();
        }
    }

    private void writeDefaultUsers() throws IOException {
        String[] lines = new String[] {
                "name,surname,username,email,password",
                "John,Smith,jsmith,john.smith@test.com,password1",
                "Mary,Jones,mjones,mary.jones@test.com,password2",
                "Tom,Brown,tbrown,tom.brown@test.com,password3",
                "Anna,White,awhite,anna.white@test.com,password4",
                "Luke,Hall,lhall,luke.hall@test.com,password5"
        };
        Files.write(PathsConfig.USERS, Arrays.asList(lines), StandardCharsets.UTF_8);
    }

    private void writeDefaultVehicles() throws IOException {
        String[] lines = new String[] {
                "id,plate,brand,type,model,year,color,status",
                "1,ΙΚΥ1234,Toyota,Sedan,Corolla,2019,Ασημί,Διαθέσιμο",
                "2,ΝΒΡ5678,Honda,Hatchback,Civic,2020,Μπλε,Διαθέσιμο",
                "3,ΡΤΛ9012,Ford,SUV,Focus,2021,Μαύρο,Διαθέσιμο",
                "4,ΧΖΑ3456,Volkswagen,Sedan,Passat,2018,Λευκό,Διαθέσιμο",
                "5,ΕΜΚ7890,Nissan,Crossover,Qashqai,2022,Κόκκινο,Διαθέσιμο"
        };
        Files.write(PathsConfig.VEHICLES, Arrays.asList(lines), StandardCharsets.UTF_8);
    }

    private void writeDefaultCustomers() throws IOException {
        String[] lines = new String[] {
                "afm,fullName,phone,email",
                "123456789,Γιώργος Παπαδόπουλος,6900000000,gpap@test.com",
                "987654321,Μαρία Ιωάννου,6911111111,mi@test.com"
        };
        Files.write(PathsConfig.CUSTOMERS, Arrays.asList(lines), StandardCharsets.UTF_8);
    }

    private void writeDefaultRentals() throws IOException {
        String[] lines = new String[] {
                "rentalId,carId,customerAfm,employeeUsername,startDate,endDate,returned,actualReturnDate"
        };
        Files.write(PathsConfig.RENTALS, Arrays.asList(lines), StandardCharsets.UTF_8);
    }

private void writeDefaultMeta() throws IOException {
    Properties p = new Properties();
    p.setProperty("nextRentalId", "1");
    try (OutputStream out = Files.newOutputStream(PathsConfig.META)) {
        p.store(out, "CarRentalApp metadata");
    }
}

    public List<Employee> loadEmployees() throws IOException {
        ensureInitialized();
        List<String> lines = Files.readAllLines(PathsConfig.USERS, StandardCharsets.UTF_8);
        List<Employee> out = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            List<String> cols = CsvUtil.parseLine(lines.get(i));
            if (cols.size() < 5) continue;
            String fullName = cols.get(0) + " " + cols.get(1);
            out.add(new Employee(fullName, cols.get(2), cols.get(3), cols.get(4)));
        }
        return out;
    }

    public void saveEmployees(List<Employee> employees) throws IOException {
        ensureInitialized();
        List<String> lines = new ArrayList<>();
        lines.add("name,surname,username,email,password");
        for (Employee e : employees) {
            String[] parts = splitName(e.getFullName());
            lines.add(String.join(",",
                    CsvUtil.esc(parts[0]),
                    CsvUtil.esc(parts[1]),
                    CsvUtil.esc(e.getUsername()),
                    CsvUtil.esc(e.getEmail()),
                    CsvUtil.esc(e.getPassword())
            ));
        }
        Files.write(PathsConfig.USERS, lines, StandardCharsets.UTF_8);
    }

    private String[] splitName(String fullName) {
        if (fullName == null) return new String[]{"", ""};
        String[] parts = fullName.trim().split("\\s+", 2);
        if (parts.length == 1) return new String[]{parts[0], ""};
        return new String[]{parts[0], parts[1]};
    }

    public List<Car> loadCars() throws IOException {
        ensureInitialized();
        List<String> lines = Files.readAllLines(PathsConfig.VEHICLES, StandardCharsets.UTF_8);
        List<Car> out = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            List<String> c = CsvUtil.parseLine(lines.get(i));
            if (c.size() < 8) continue;
            int id = Integer.parseInt(c.get(0));
            String plate = c.get(1);
            String brand = c.get(2);
            String type = c.get(3);
            String model = c.get(4);
            int year = Integer.parseInt(c.get(5));
            String color = c.get(6);
            CarStatus status = CarStatus.fromGreek(c.get(7));
            out.add(new Car(id, plate, brand, type, model, year, color, status));
        }
        return out;
    }

    public void saveCars(List<Car> cars) throws IOException {
        ensureInitialized();
        List<String> lines = new ArrayList<>();
        lines.add("id,plate,brand,type,model,year,color,status");
        for (Car car : cars) {
            lines.add(String.join(",",
                    String.valueOf(car.getId()),
                    CsvUtil.esc(car.getPlate()),
                    CsvUtil.esc(car.getBrand()),
                    CsvUtil.esc(car.getType()),
                    CsvUtil.esc(car.getModel()),
                    String.valueOf(car.getYear()),
                    CsvUtil.esc(car.getColor()),
                    CsvUtil.esc(car.getStatus().toGreek())
            ));
        }
        Files.write(PathsConfig.VEHICLES, lines, StandardCharsets.UTF_8);
    }

    public List<Customer> loadCustomers() throws IOException {
        ensureInitialized();
        List<String> lines = Files.readAllLines(PathsConfig.CUSTOMERS, StandardCharsets.UTF_8);
        List<Customer> out = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            List<String> c = CsvUtil.parseLine(lines.get(i));
            if (c.size() < 4) continue;
            out.add(new Customer(c.get(0), c.get(1), c.get(2), c.get(3)));
        }
        return out;
    }

    public void saveCustomers(List<Customer> customers) throws IOException {
        ensureInitialized();
        List<String> lines = new ArrayList<>();
        lines.add("afm,fullName,phone,email");
        for (Customer cu : customers) {
            lines.add(String.join(",",
                    CsvUtil.esc(cu.getAfm()),
                    CsvUtil.esc(cu.getFullName()),
                    CsvUtil.esc(cu.getPhone()),
                    CsvUtil.esc(cu.getEmail())
            ));
        }
        Files.write(PathsConfig.CUSTOMERS, lines, StandardCharsets.UTF_8);
    }

    public List<Rental> loadRentals() throws IOException {
        ensureInitialized();
        List<String> lines = Files.readAllLines(PathsConfig.RENTALS, StandardCharsets.UTF_8);
        List<Rental> out = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            List<String> c = CsvUtil.parseLine(lines.get(i));
            if (c.size() < 8) continue;
            long rentalId = Long.parseLong(c.get(0));
            int carId = Integer.parseInt(c.get(1));
            String customerAfm = c.get(2);
            String employeeUsername = c.get(3);
            LocalDate startDate = LocalDate.parse(c.get(4));
            LocalDate endDate = LocalDate.parse(c.get(5));
            boolean returned = Boolean.parseBoolean(c.get(6));
            LocalDate actual = c.get(7) == null || c.get(7).trim().isEmpty() ? null : LocalDate.parse(c.get(7));
            out.add(new Rental(rentalId, carId, customerAfm, employeeUsername, startDate, endDate, returned, actual));
        }
        return out;
    }

    public void saveRentals(List<Rental> rentals) throws IOException {
        ensureInitialized();
        List<String> lines = new ArrayList<>();
        lines.add("rentalId,carId,customerAfm,employeeUsername,startDate,endDate,returned,actualReturnDate");
        for (Rental r : rentals) {
            lines.add(String.join(",",
                    String.valueOf(r.getRentalId()),
                    String.valueOf(r.getCarId()),
                    CsvUtil.esc(r.getCustomerAfm()),
                    CsvUtil.esc(r.getEmployeeUsername()),
                    CsvUtil.esc(r.getStartDate().toString()),
                    CsvUtil.esc(r.getEndDate().toString()),
                    String.valueOf(r.isReturned()),
                    CsvUtil.esc(r.getActualReturnDate() == null ? "" : r.getActualReturnDate().toString())
            ));
        }
        Files.write(PathsConfig.RENTALS, lines, StandardCharsets.UTF_8);
    }

    public Properties loadMeta() throws IOException {
        // meta should exist after ensureInitialized() is called by the system at startup
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(PathsConfig.META)) {
            p.load(in);
        }
        return p;
    }

    public void saveMeta(Properties p) throws IOException {
        // meta file should exist; do not call ensureInitialized() here to avoid recursion
        try (OutputStream out = Files.newOutputStream(PathsConfig.META)) {
            p.store(out, "CarRentalApp metadata");
        }
    }
}