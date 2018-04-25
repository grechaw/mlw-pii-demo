package com.marklogic.example.loan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;

public class LoanTestBase {

    LoanHistory loanHistory;
    DatabaseClient clerkClient, officerClient;
    static ObjectWriter writer = new ObjectMapper().writerWithDefaultPrettyPrinter();

    public LoanTestBase() {
        clerkClient = DatabaseClientFactory.newClient("localhost",8015,new DatabaseClientFactory.DigestAuthContext("SydneyGardner", "x"));
        officerClient = DatabaseClientFactory.newClient("localhost",8015, new DatabaseClientFactory.DigestAuthContext("GiannaEmerson", "x"));
    }
}
