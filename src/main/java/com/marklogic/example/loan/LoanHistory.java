package com.marklogic.example.loan;

// IMPORTANT: Do not edit. This file is generated.



import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.Format;

import java.io.IOException;
import java.io.Reader;

public class LoanHistory extends DBFunctionBase {

    public static LoanHistory on(DatabaseClient db) {
        return new LoanHistory(db);
    }
    ObjectMapper mapper = new ObjectMapper();

    public LoanHistory(DatabaseClient db) {
        super(db);
    }

    public ObjectNode getCustomerHistory(String customerName) throws IOException {
        Reader reader = asReader(postForDocument("/dbf/demo/history/getCustomerHistory.sjs",
                urlencodedParams(
                        paramEncoded("customerName", false, customerName)
                ), Format.JSON), Format.JSON, false);
        return mapper.readValue(reader, ObjectNode.class);
    }


}
