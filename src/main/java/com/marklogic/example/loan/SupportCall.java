package com.marklogic.example.loan;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class SupportCall {

    public static class Customer {
        public String fullName;

        // a company name
        public String worksFor;

        // email is something all clerks can see
        public String email;
        // this info should be secured -- only compliance officer can see
        public String ssn;
    }

    public static class Employee {
        public String fullName;
    }


    /**
     * @id a unique generated identifier for the application
     */
    public String id;

    /**
     * what happened during the call
     */
    public String description;

    public Employee clerk;
    // a call has an officer too, who is another employee
    public Employee complianceOfficer;
    public Customer caller;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    public Date callStartTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    public Date callEndTime;
}
