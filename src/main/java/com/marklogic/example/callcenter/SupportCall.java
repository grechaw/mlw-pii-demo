package com.marklogic.example.callcenter;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class SupportCall {

    public static class Customer {
        public String fullName;
        public String company;

        // email is something all call center folks should see
        public String email;
        // this info should be secured -- only manager (or amp?) can see.
        public String ssn;
    }

    public static class Employee {
        public String fullName;
        public Employee manager;
    }


    public String id;
    public Customer customer;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    public Date callStartTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    public Date callEndTime;
    public Employee supportTech;
}
