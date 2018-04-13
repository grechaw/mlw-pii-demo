package com.marklogic.example.loan;

// IMPORTANT: Do not edit. This file is generated.



import com.marklogic.client.DatabaseClient;
import com.marklogic.client.io.Format;

import java.io.Reader;

public class LoanHistory extends DBFunctionBase {

    public static LoanHistory on(DatabaseClient db) {
        return new LoanHistory(db);
    }

    public LoanHistory(DatabaseClient db) {
        super(db);
    }

    public Reader getCustomerHistory(String customerName) {
        return asReader(postForDocument("/dbf/demo/history/getCustomerHistory.sjs",
                urlencodedParams(
                        paramEncoded("customerName", false, customerName)
                ), Format.JSON), Format.JSON, false);
    }


}
