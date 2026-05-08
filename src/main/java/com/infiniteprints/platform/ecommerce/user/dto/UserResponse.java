package com.infiniteprints.platform.ecommerce.user.dto;

import java.util.UUID;

public class UserResponse {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String addressLine1;
    private String addressCity;
    private String addressState;
    private String addressPincode;

    public UserResponse(UUID id, String email, String firstName, String lastName,
                        String phone, String addressLine1,
                        String addressCity, String addressState,
                        String addressPincode) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.addressLine1 = addressLine1;
        this.addressCity = addressCity;
        this.addressState = addressState;
        this.addressPincode = addressPincode;
    }

    // getters only (keep it simple)
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public String getAddressLine1() { return addressLine1; }
    public String getAddressCity() { return addressCity; }
    public String getAddressState() { return addressState; }
    public String getAddressPincode() { return addressPincode; }
}