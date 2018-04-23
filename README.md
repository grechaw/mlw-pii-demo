PII demo
--------

## Setup before running demo:

1.  have MarkLogic on localhost
1.  ensure this project is clean and matches repo
1.  if anything doesn't work, undeploy hub to clean up completely.
1.  run quick-start and navigate to this directory for your project
1.  look at, admire the entities (don't modify tho)
1.  look at, admire, and run both the ingest and the harmonize flows.

## The script:

* I'm a MarkLogic developer for a bank, 

I've just been given this story:

As a clerk ... I need to ... see customer history data.

But not PII

When I bring up customer history, I should see the entire document associated with a support call, but not the customer's PII

As a compliance officer, I need to verify that an applicant's ssn is correct.


Givens:
-------

* A test data set (OK to see PII in test data)
* Provide getCustomerHistory(name) to Java Developers
* Implement database function
* Verify database function
* Define the roles


Demo:
-----

* Implement PII
* Deploy PII configuration to Server
* Verify PII


steps:


1. ./gradlew getCustomerHistory -PcustomerName=Carter -Prole=officer
1. edit Customer.entity.json    edit the "pii" property to include ssn.
1. ./gradlew mlLoadModules
1. ./gradlew generatePii
1. ./gradlew mlDeploySecurity
1. ./gradlew getCustomerHistory -PcustomerName=Carter -Prole=officer
1. ./gradlew getCustomerHistory -PcustomerName=Carter -Prole=clerk

TODO:
1. ./gradlew getCustomerHistory -Pssn=xxx -Prole=clerk
1. ./gradlew getCustomerHistory -Pssn=xxx -Prole=officer

Note that there is no 'ssn' in the search result for clerk.
Note that clerk cannot search by ssn
