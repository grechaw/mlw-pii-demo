'use strict';

const name = xdmp.getRequestField("customerName");

xdmp.setResponseContentType("application/json");
var results = cts.search(cts.jsonPropertyScopeQuery("Customer", cts.jsonPropertyWordQuery("fullName", name)));
results;
