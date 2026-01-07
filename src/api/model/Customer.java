package api.model;

import java.util.Objects;

public class Customer {
    private String afm;
    private String fullName;
    private String phone;
    private String email;

    public Customer(String afm, String fullName, String phone, String email) {
        this.afm = afm;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
    }

    public String getAfm() { return afm; }
    public void setAfm(String afm) { this.afm = afm; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        Customer customer = (Customer) o;
        return Objects.equals(afm, customer.afm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(afm);
    }

    @Override
    public String toString() {
        return fullName + " (ΑΦΜ: " + afm + ")";
    }
}
