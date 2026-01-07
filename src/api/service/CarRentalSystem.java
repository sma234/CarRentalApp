package api.service;

import api.model.Car;
import api.model.CarStatus;
import api.model.Customer;
import api.model.Employee;
import api.model.Rental;
import api.storage.DataStore;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Κεντρική λογική εφαρμογής (CRUD + αναζητήσεις + ενοικιάσεις/επιστροφές).
 * Όλες οι αλλαγές αποθηκεύονται άμεσα στα αρχεία.
 */
public class CarRentalSystem {
    private final DataStore store;

    private List<Employee> employees;
    private List<Car> cars;
    private List<Customer> customers;
    private List<Rental> rentals;

    private Employee loggedIn;

    public CarRentalSystem(DataStore store) throws IOException {
        this.store = store;
        reloadAll();
    }

    /** Επαναφόρτωση από τα αρχεία. */
    public final void reloadAll() throws IOException {
        store.ensureInitialized();
        employees = store.loadEmployees();
        cars = store.loadCars();
        customers = store.loadCustomers();
        rentals = store.loadRentals();
        // Sync car statuses from rentals (in case files were edited):
        Set<Integer> rentedCarIds = rentals.stream().filter(r -> !r.isReturned()).map(Rental::getCarId).collect(Collectors.toSet());
        for (Car c : cars) {
            c.setStatus(rentedCarIds.contains(c.getId()) ? CarStatus.RENTED : c.getStatus());
            if (rentedCarIds.contains(c.getId())) c.setStatus(CarStatus.RENTED);
            else if (c.getStatus() == CarStatus.RENTED && !rentedCarIds.contains(c.getId())) c.setStatus(CarStatus.AVAILABLE);
        }
        store.saveCars(cars);
    }

    // ---------------- Auth ----------------

    /**
     * Login με username/password.
     * @throws ValidationException αν τα credentials είναι λάθος
     */
    public Employee login(String username, String password) throws ValidationException {
        if (username == null || username.trim().isEmpty()) throw new ValidationException("Συμπληρώστε username.");
        if (password == null || password.trim().isEmpty()) throw new ValidationException("Συμπληρώστε password.");
        for (Employee e : employees) {
            if (e.getUsername().equals(username) && e.getPassword().equals(password)) {
                loggedIn = e;
                return e;
            }
        }
        throw new ValidationException("Λάθος username ή password.");
    }

    /** Logout. */
    public void logout() { loggedIn = null; }

    /** Επιστρέφει τον συνδεδεμένο υπάλληλο ή null. */
    public Employee getLoggedIn() { return loggedIn; }

    private void requireLogin() throws ValidationException {
        if (loggedIn == null) throw new ValidationException("Απαιτείται σύνδεση (login).");
    }

    // ---------------- Cars ----------------

    public List<Car> getCars() { return new ArrayList<>(cars); }

    /**
     * Αναζήτηση αυτοκινήτων με συνδυασμό κριτηρίων (όλα προαιρετικά).
     */
    public List<Car> searchCars(String brand, String plate, String model, String color, String type, CarStatus status) {
        return cars.stream().filter(c -> {
            if (brand != null && !brand.trim().isEmpty() && !containsIgnoreCase(c.getBrand(), brand)) return false;
            if (plate != null && !plate.trim().isEmpty() && !containsIgnoreCase(c.getPlate(), plate)) return false;
            if (model != null && !model.trim().isEmpty() && !containsIgnoreCase(c.getModel(), model)) return false;
            if (color != null && !color.trim().isEmpty() && !containsIgnoreCase(c.getColor(), color)) return false;
            if (type != null && !type.trim().isEmpty() && !containsIgnoreCase(c.getType(), type)) return false;
            if (status != null && c.getStatus() != status) return false;
            return true;
        }).collect(Collectors.toList());
    }

    /**
     * Προσθήκη αυτοκινήτου.
     */
    public void addCar(Car car) throws ValidationException, IOException {
        requireLogin();
        validateCar(car, true);
        cars.add(car);
        store.saveCars(cars);
    }

    /**
     * Ενημέρωση αυτοκινήτου.
     */
    public void updateCar(Car car) throws ValidationException, IOException {
        requireLogin();
        validateCar(car, false);
        Car existing = findCarById(car.getId());
        if (existing == null) throw new ValidationException("Το αυτοκίνητο δεν βρέθηκε.");
        existing.setPlate(car.getPlate());
        existing.setBrand(car.getBrand());
        existing.setType(car.getType());
        existing.setModel(car.getModel());
        existing.setYear(car.getYear());
        existing.setColor(car.getColor());
        existing.setStatus(car.getStatus());
        store.saveCars(cars);
    }

    private void validateCar(Car car, boolean isNew) throws ValidationException {
        if (car == null) throw new ValidationException("Κενό αυτοκίνητο.");
        if (car.getId() <= 0) throw new ValidationException("Το id πρέπει να είναι θετικός αριθμός.");
        if (isBlank(car.getPlate())) throw new ValidationException("Η πινακίδα είναι υποχρεωτική.");
        if (isBlank(car.getBrand())) throw new ValidationException("Η μάρκα είναι υποχρεωτική.");
        if (isBlank(car.getModel())) throw new ValidationException("Το μοντέλο είναι υποχρεωτικό.");
        if (isBlank(car.getType())) throw new ValidationException("Ο τύπος είναι υποχρεωτικός.");
        if (car.getYear() <= 0) throw new ValidationException("Το έτος πρέπει να είναι θετικό.");

        // Unique: id & plate
        for (Car c : cars) {
            if (isNew) {
                if (c.getId() == car.getId()) throw new ValidationException("Υπάρχει ήδη αυτοκίνητο με αυτό το id.");
                if (c.getPlate().equalsIgnoreCase(car.getPlate())) throw new ValidationException("Υπάρχει ήδη αυτοκίνητο με αυτή την πινακίδα.");
            } else {
                if (c.getId() != car.getId() && c.getPlate().equalsIgnoreCase(car.getPlate())) {
                    throw new ValidationException("Υπάρχει ήδη άλλο αυτοκίνητο με αυτή την πινακίδα.");
                }
            }
        }
    }

    public Car findCarById(int id) {
        for (Car c : cars) if (c.getId() == id) return c;
        return null;
    }

    public Car findCarByPlate(String plate) {
        if (plate == null) return null;
        for (Car c : cars) if (c.getPlate().equalsIgnoreCase(plate.trim())) return c;
        return null;
    }

    // ---------------- Customers ----------------

    public List<Customer> getCustomers() { return new ArrayList<>(customers); }

    public List<Customer> searchCustomers(String afm, String name, String phone) {
        return customers.stream().filter(c -> {
            if (afm != null && !afm.trim().isEmpty() && !containsIgnoreCase(c.getAfm(), afm)) return false;
            if (name != null && !name.trim().isEmpty() && !containsIgnoreCase(c.getFullName(), name)) return false;
            if (phone != null && !phone.trim().isEmpty() && !containsIgnoreCase(c.getPhone(), phone)) return false;
            return true;
        }).collect(Collectors.toList());
    }

    public void addCustomer(Customer c) throws ValidationException, IOException {
        requireLogin();
        validateCustomer(c, true);
        customers.add(c);
        store.saveCustomers(customers);
    }

    public void updateCustomer(Customer c) throws ValidationException, IOException {
        requireLogin();
        validateCustomer(c, false);
        Customer existing = findCustomerByAfm(c.getAfm());
        if (existing == null) throw new ValidationException("Ο πελάτης δεν βρέθηκε.");
        existing.setFullName(c.getFullName());
        existing.setPhone(c.getPhone());
        existing.setEmail(c.getEmail());
        store.saveCustomers(customers);
    }

    private void validateCustomer(Customer c, boolean isNew) throws ValidationException {
        if (c == null) throw new ValidationException("Κενός πελάτης.");
        if (isBlank(c.getAfm())) throw new ValidationException("Το ΑΦΜ είναι υποχρεωτικό.");
        if (!c.getAfm().matches("\\d{9}")) throw new ValidationException("Το ΑΦΜ πρέπει να έχει 9 ψηφία.");
        if (isBlank(c.getFullName())) throw new ValidationException("Το ονοματεπώνυμο είναι υποχρεωτικό.");
        if (isBlank(c.getPhone())) throw new ValidationException("Το τηλέφωνο είναι υποχρεωτικό.");
        if (isBlank(c.getEmail())) throw new ValidationException("Το email είναι υποχρεωτικό.");
        if (!c.getEmail().contains("@")) throw new ValidationException("Μη έγκυρο email.");

        for (Customer x : customers) {
            if (isNew) {
                if (x.getAfm().equals(c.getAfm())) throw new ValidationException("Υπάρχει ήδη πελάτης με αυτό το ΑΦΜ.");
            } else {
                // AFM is key, not editable in update in our flow
            }
        }
    }

    public Customer findCustomerByAfm(String afm) {
        if (afm == null) return null;
        for (Customer c : customers) if (c.getAfm().equals(afm.trim())) return c;
        return null;
    }

    // ---------------- Employees ----------------

    public List<Employee> getEmployees() { return new ArrayList<>(employees); }

    public void addEmployee(Employee e) throws ValidationException, IOException {
        requireLogin();
        validateEmployee(e, true);
        employees.add(e);
        store.saveEmployees(employees);
    }

    public void deleteEmployee(String username) throws ValidationException, IOException {
        requireLogin();
        if (isBlank(username)) throw new ValidationException("Κενό username.");
        if (loggedIn != null && loggedIn.getUsername().equals(username)) {
            throw new ValidationException("Δεν μπορείτε να διαγράψετε τον εαυτό σας όσο είστε συνδεδεμένος.");
        }
        boolean removed = employees.removeIf(u -> u.getUsername().equals(username));
        if (!removed) throw new ValidationException("Ο χρήστης δεν βρέθηκε.");
        store.saveEmployees(employees);
    }

    private void validateEmployee(Employee e, boolean isNew) throws ValidationException {
        if (e == null) throw new ValidationException("Κενός χρήστης.");
        if (isBlank(e.getFullName())) throw new ValidationException("Το ονοματεπώνυμο είναι υποχρεωτικό.");
        if (isBlank(e.getUsername())) throw new ValidationException("Το username είναι υποχρεωτικό.");
        if (isBlank(e.getEmail())) throw new ValidationException("Το email είναι υποχρεωτικό.");
        if (isBlank(e.getPassword())) throw new ValidationException("Το password είναι υποχρεωτικό.");
        if (!e.getEmail().contains("@")) throw new ValidationException("Μη έγκυρο email.");

        for (Employee x : employees) {
            if (isNew) {
                if (x.getUsername().equalsIgnoreCase(e.getUsername())) throw new ValidationException("Υπάρχει ήδη χρήστης με αυτό το username.");
                if (x.getEmail().equalsIgnoreCase(e.getEmail())) throw new ValidationException("Υπάρχει ήδη χρήστης με αυτό το email.");
            }
        }
    }

    // ---------------- Rentals ----------------

    public List<Rental> getRentals() { return new ArrayList<>(rentals); }

    public List<Rental> getRentalsForCustomer(String afm) {
        return rentals.stream().filter(r -> r.getCustomerAfm().equals(afm)).collect(Collectors.toList());
    }

    public List<Rental> getRentalsForCar(int carId) {
        return rentals.stream().filter(r -> r.getCarId() == carId).collect(Collectors.toList());
    }

    public List<Rental> getActiveRentals() {
        return rentals.stream().filter(r -> !r.isReturned()).collect(Collectors.toList());
    }

    /**
     * Δημιουργία νέας ενοικίασης με έλεγχο διαθεσιμότητας.
     */
    public Rental rentCar(int carId, String customerAfm, LocalDate start, LocalDate end) throws ValidationException, IOException {
        requireLogin();
        if (start == null || end == null) throw new ValidationException("Συμπληρώστε ημερομηνίες.");
        if (end.isBefore(start)) throw new ValidationException("Η ημερομηνία λήξης δεν μπορεί να είναι πριν την έναρξη.");
        Car car = findCarById(carId);
        if (car == null) throw new ValidationException("Το αυτοκίνητο δεν βρέθηκε.");
        if (car.getStatus() != CarStatus.AVAILABLE) throw new ValidationException("Το αυτοκίνητο δεν είναι διαθέσιμο.");
        Customer customer = findCustomerByAfm(customerAfm);
        if (customer == null) throw new ValidationException("Ο πελάτης δεν βρέθηκε.");

        long rentalId = nextRentalId();
        Rental r = new Rental(rentalId, carId, customerAfm, loggedIn.getUsername(), start, end, false, null);
        rentals.add(r);
        car.setStatus(CarStatus.RENTED);

        store.saveRentals(rentals);
        store.saveCars(cars);
        return r;
    }

    /**
     * Επιστροφή ενοικιασμένου οχήματος.
     */
    public void returnRental(long rentalId) throws ValidationException, IOException {
        requireLogin();
        Rental r = findRentalById(rentalId);
        if (r == null) throw new ValidationException("Η ενοικίαση δεν βρέθηκε.");
        if (r.isReturned()) throw new ValidationException("Η ενοικίαση είναι ήδη ολοκληρωμένη.");
        Car car = findCarById(r.getCarId());
        if (car == null) throw new ValidationException("Το αυτοκίνητο δεν βρέθηκε (ασυνέπεια δεδομένων).");

        r.markReturned(LocalDate.now());
        car.setStatus(CarStatus.AVAILABLE);

        store.saveRentals(rentals);
        store.saveCars(cars);
    }

    public Rental findRentalById(long rentalId) {
        for (Rental r : rentals) if (r.getRentalId() == rentalId) return r;
        return null;
    }

    private long nextRentalId() throws IOException {
        Properties p = store.loadMeta();
        long next = Long.parseLong(p.getProperty("nextRentalId", "1"));
        p.setProperty("nextRentalId", String.valueOf(next + 1));
        store.saveMeta(p);
        return next;
    }

    // ---------------- Helpers ----------------

    private static boolean containsIgnoreCase(String a, String b) {
        if (a == null || b == null) return false;
        return a.toLowerCase().contains(b.trim().toLowerCase());
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
