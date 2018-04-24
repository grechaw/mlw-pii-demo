'use strict';

const es = require("/MarkLogic/entity-services/entity-services.xqy");

const ssn = xdmp.getRequestField("ssn");

xdmp.log("SSN passed in as " + ssn);
xdmp.setResponseContentType("application/json");
var results = cts.search(cts.jsonPropertyScopeQuery("Customer", cts.jsonPropertyValueQuery("ssn", ssn)));
var jsonResult;
if (fn.head(results)) {
    jsonResult = es.instanceJsonFromDocument(fn.head(results));
    jsonResult = jsonResult.toObject()[0].SupportCall.caller.Customer
} else {
    jsonResult = { };
}
jsonResult