PII demo

2018-04-10 made first test data

Creating entities:

`./gradlew hubCreateEntity -PentityName=SupportCall`

This just makes an empty directory... so i'm gonna make the entity services
model for my support call



Got users and roles set up

support-technician, support-manager


How changing to the hub-admin user i made for further deployments:


The script:


* I'm a MarkLogic developer for a bank, 


I've just been given this story:

As a clerk ... I need to ... see customer history data.

But not PII

When I bring up customer history, I should see the entire document associated with a support call, but not the customer's PII

As a compliance officer, I need to verify that an applicant's ssn is correct.


Task list:

Givens:

* A test data set (OK to see PII in test data)
* Provide getCustomerHistory(name) to Java Developers
* Implement database function
* Verify database function
* Define the roles

demo:

* Implement PII
* Deploy PII configuration to Server
* Verify PII





steps:


1. ./gradlew test
2. look at log
3. edit Customer.entity.json
4. ./gradlew mlLoadModules
5. ./gradlew generatePii
6. ./gradlew test
7. look at log.
