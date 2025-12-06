package com.june.model.common;

public class ContactInfo {
    private String name;
    private String phone;
    private String email;
    private Address address;
    private String website;

    public ContactInfo() {}

    public ContactInfo(String name, String phone, String email, Address address, String website) {
        this.name = name; this.phone = phone; this.email = email; this.address = address; this.website = website;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String telHref(){ return phone == null ? null : ("tel:" + phone.replace(" ","")); }
}