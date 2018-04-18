'use strict';

const es = require("/MarkLogic/entity-services/entity-services.xqy");

const name = xdmp.getRequestField("customerName");

xdmp.setResponseContentType("application/json");
var results = cts.search(cts.jsonPropertyScopeQuery("Customer", cts.jsonPropertyWordQuery("fullName", name)));
var json = es.instanceJsonFromDocument(fn.head(results));
json.toObject()[0].SupportCall.caller.Customer
