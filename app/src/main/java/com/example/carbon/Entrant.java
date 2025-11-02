package com.example.carbon;

public class Entrant {
    String firstName;
    String lastName;
    String email;
    String phoneNo;
    String address1;
    String address2;
    String postcode;
    String city;
    String province;

    public Entrant(String firstName,
                   String lastName,
                   String email,
                   String phoneNo,
                   String address1,
                   String address2,
                   String postcode,
                   String city,
                   String province){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNo = phoneNo;
        this.address1 = address1;
        this.address2 = address2;
        this.postcode = postcode;
        this.city = city;
        this.province = province;
    }
}
