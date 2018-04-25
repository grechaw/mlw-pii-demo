package com.marklogic.example.loan;

import ch.qos.logback.core.net.SyslogOutputStream;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LoanHistoryTest extends LoanTestBase {

    private static Logger logger = LoggerFactory.getLogger(LoanHistoryTest.class);


    @Test
    public void testHistoryReturns() throws IOException {
        HubConfig hubConfig = HubConfigBuilder.newHubConfigBuilder(".").withPropertiesFromEnvironment().build();
        loanHistory = new LoanHistory(clerkClient);

        logger.info("Calling getCustomerHistory as clerk");
        ObjectNode history = getCustomerHistory("clerk", "Carter");
        logger.info(writer.writeValueAsString(history));
        assertEquals("Carter Schneider", history.get("fullName").asText());
        assertNull(history.get("ssn"));

        loanHistory = new LoanHistory(officerClient);

        logger.info("Calling getCustomerHistory as compliance officer.");
        history = getCustomerHistory("officer", "Carter");
        assertEquals("Carter Schneider", history.get("fullName").asText());
        assertNotNull(history.get("ssn").asText());
        logger.info(writer.writeValueAsString(history));
    }

    public ObjectNode getCustomerHistory(String role, String name) throws IOException {
        LoanHistory loanHistory;
        if (role.equals("clerk")) {
            loanHistory = new LoanHistory(clerkClient);
        }
        else if (role.equals("officer")) {
            loanHistory = new LoanHistory(officerClient);
        } else {
            throw new RuntimeException("Role must be 'clerk' or 'officer'");
        }
        return loanHistory.getCustomerHistory(name);
    }

    public static void main(String[] args) throws IOException {
        String role = args[0];
        String customerName = "None";
        if (args.length > 1) {
            customerName = args[1];
        }
        logger.info("Called main with customerName " + customerName);
        LoanHistoryTest  tester = new LoanHistoryTest();
        System.out.println(writer.writeValueAsString(tester.getCustomerHistory(role, customerName)));
    }

}

