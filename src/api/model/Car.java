package api.model;

import java.util.Objects;

public class Car {
    private int id;
    private String plate;
    private String brand;
    private String type;
    private String model;
    private int year;
    private String color;
    private CarStatus status;

    public Car(int id, String plate, String brand, String type, String model, int year, String color, CarStatus status) {
        this.id = id;
        this.plate = plate;
        this.brand = brand;
        this.type = type;
        this.model = model;
        this.year = year;
        this.color = color;
        this.status = status == null ? CarStatus.AVAILABLE : status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPlate() { return plate; }
    public void setPlate(String plate) { this.plate = plate; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public CarStatus getStatus() { return status; }
    public void setStatus(CarStatus status) { this.status = status; }

    public boolean isAvailable() { return status == CarStatus.AVAILABLE; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Car)) return false;
        Car car = (Car) o;
        return id == car.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s %s (%s) [%s]", brand, model, plate, status.toGreek());
    }
}
