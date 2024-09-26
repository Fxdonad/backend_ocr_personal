package com.ocrweb.backend_ocr.entity.profile;
import jakarta.persistence.*;

@Entity
@Table(name = "BusinessProfile")
public class BusinessProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer businessID;

    @Column(nullable = false)
    private Integer userID;

    @Column(length = 100)
    private String companyName;

    @Column(length = 50)
    private String contactName;

    @Column(length = 100)
    private String contactEmail;

    @Column(length = 255)
    private String address;

    @Column(length = 15)
    private String phoneNumber;

    // Getters and Setters
    public Integer getBusinessID() {
        return businessID;
    }

    public void setBusinessID(Integer businessID) {
        this.businessID = businessID;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }
}
