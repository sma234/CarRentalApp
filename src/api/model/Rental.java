package api.model;

import java.time.LocalDate;
import java.util.Objects;

public class Rental {
    private long rentalId;
    private int carId;
    private String customerAfm;
    private String employeeUsername;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean returned;
    private LocalDate actualReturnDate;

    public Rental(long rentalId, int carId, String customerAfm, String employeeUsername,
                  LocalDate startDate, LocalDate endDate, boolean returned, LocalDate actualReturnDate) {
        this.rentalId = rentalId;
        this.carId = carId;
        this.customerAfm = customerAfm;
        this.employeeUsername = employeeUsername;
        this.startDate = startDate;
        this.endDate = endDate;
        this.returned = returned;
        this.actualReturnDate = actualReturnDate;
    }

    public long getRentalId() { return rentalId; }
    public int getCarId() { return carId; }
    public String getCustomerAfm() { return customerAfm; }
    public String getEmployeeUsername() { return employeeUsername; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public boolean isReturned() { return returned; }
    public LocalDate getActualReturnDate() { return actualReturnDate; }

    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public void markReturned(LocalDate when) {
        this.returned = true;
        this.actualReturnDate = when;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rental)) return false;
        Rental rental = (Rental) o;
        return rentalId == rental.rentalId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rentalId);
    }
}
