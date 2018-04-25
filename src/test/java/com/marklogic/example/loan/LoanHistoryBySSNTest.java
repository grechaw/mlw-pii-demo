package com.marklogic.example.loan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.hub.HubConfig;
import com.marklogic.hub.HubConfigBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.*;

public class LoanHistoryBySSNTest extends LoanTestBase {

    private static Logger logger = LoggerFactory.getLogger(LoanHistoryBySSNTest.class);

    @Test
    public void testHistoryBySsn() throws IOException {
        HubConfig hubConfig = HubConfigBuilder.newHubConfigBuilder(".").withPropertiesFromEnvironment().build();
        loanHistory = new LoanHistory(clerkClient);

        logger.info("Calling getCustomerHistoryBySSN as clerk");
        ObjectNode history = getCustomerHistoryBySSN("clerk", "307-63-428");
        assertEquals(0, history.size());

        loanHistory = new LoanHistory(officerClient);

        logger.info("Calling getCustomerHistoryBySSN as compliance officer.");
        history = getCustomerHistoryBySSN("officer", "307-63-428");
        assertEquals("Molly Horn", history.get("fullName").asText());
        logger.info(writer.writeValueAsString(history));
    }

    public ObjectNode getCustomerHistoryBySSN(String role, String ssn) throws IOException {
        LoanHistory loanHistory;
        if (role.equals("clerk")) {
            loanHistory = new LoanHistory(clerkClient);
        }
        else if (role.equals("officer")) {
            loanHistory = new LoanHistory(officerClient);
        } else {
            throw new RuntimeException("Role must be 'clerk' or 'officer'");
        }
        return loanHistory.getCustomerHistoryBySSN(ssn);
    }

    public static void main(String[] args) throws IOException {
        String role = args[0];
        String ssn = null;
        if (args.length > 1) {
            ssn = args[1];
        }
        logger.info("Called main with ssn " + ssn);
        LoanHistoryBySSNTest tester = new LoanHistoryBySSNTest();
        System.out.println(writer.writeValueAsString(tester.getCustomerHistoryBySSN(role, ssn)));
    }

}

