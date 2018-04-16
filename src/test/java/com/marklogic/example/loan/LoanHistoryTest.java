package com.marklogic.example.loan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.hub.HubConfig;
import com.marklogic.hub.HubConfigBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LoanHistoryTest {

    LoanHistory loanHistory;
    private static Logger logger = LoggerFactory.getLogger(LoanHistoryTest.class);

    DatabaseClient clerkClient, officerClient;

    public LoanHistoryTest() {
        clerkClient = DatabaseClientFactory.newClient("localhost",8015,new DatabaseClientFactory.DigestAuthContext("SydneyGardner", "x"));
        officerClient = DatabaseClientFactory.newClient("localhost",8015, new DatabaseClientFactory.DigestAuthContext("GiannaEmerson", "x"));
    }

    @Test
    public void testHistoryReturns() throws IOException {
        HubConfig hubConfig = HubConfigBuilder.newHubConfigBuilder(".").withPropertiesFromEnvironment().build();
        loanHistory = new LoanHistory(clerkClient);

        ObjectNode history = loanHistory.getCustomerHistory("Carter");
        assertEquals("Carter Schneider", history.get("envelope").get("instance").get("SupportCall").get("caller").get("Customer").get("fullName").asText());
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(System.out, history);



        loanHistory = new LoanHistory(officerClient);

        history = loanHistory.getCustomerHistory("Carter");
        assertEquals("Carter Schneider", history.get("envelope").get("instance").get("SupportCall").get("caller").get("Customer").get("fullName").asText());
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(System.out, history);
    }

}
