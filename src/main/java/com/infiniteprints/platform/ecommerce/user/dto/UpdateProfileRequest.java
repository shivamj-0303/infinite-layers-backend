package com.infiniteprints.platform.ecommerce.user.dto;

public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String addressLine1;
    private String addressCity;
    private String addressState;
    private String addressPincode;

    // Getters and Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String s) { this.addressLine1 = s; }
    public String getAddressCity() { return addressCity; }
    public void setAddressCity(String s) { this.addressCity = s; }
    public String getAddressState() { return addressState; }
    public void setAddressState(String s) { this.addressState = s; }
    public String getAddressPincode() { return addressPincode; }
    public void setAddressPincode(String s) { this.addressPincode = s; }
}