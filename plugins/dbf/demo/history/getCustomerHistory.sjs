'use strict';

const es = require("/MarkLogic/entity-services/entity-services.xqy");

const name = xdmp.getRequestField("customerName");


xdmp.log("Name passed in as " + name);
xdmp.setResponseContentType("application/json");
var results = cts.search(cts.jsonPropertyScopeQuery("Customer", cts.jsonPropertyWordQuery("fullName", name)));
var jsonResult;
if (fn.head(results)) {
    jsonResult = es.instanceJsonFromDocument(fn.head(results));
    jsonResult = jsonResult.toObject()[0].SupportCall.caller.Customer
} else {
    jsonResult = { };
}
jsonResult
