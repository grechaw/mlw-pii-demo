package com.marklogic.example.loan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.codearte.jfairy.Fairy;
import io.codearte.jfairy.producer.DateProducer;
import io.codearte.jfairy.producer.person.Person;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestCallGenerator {

    Fairy fairy;


    Person officerPerson;
    SupportCall.Employee[] employees;
    SupportCall.Employee officer;

    Random random;

    int MAXEMPS = 3;

    public TestCallGenerator() {
        fairy = Fairy.create();
        officerPerson = fairy.person();
        random = new Random();
    }

    public void makeEmployees() {
        List<SupportCall.Employee> employeeList = new ArrayList();
        for (int i = 0; i < MAXEMPS; i++) {
            Person employeeFairy = fairy.person();
            SupportCall.Employee employee = new SupportCall.Employee();
            employee.fullName = employeeFairy.getFullName();

            employeeList.add(employee);
        }
        officer = new SupportCall.Employee();
        officer.fullName = officerPerson.getFullName();
        employees = employeeList.toArray(new SupportCall.Employee[] {} );
    }

    public SupportCall getCall() {

        SupportCall.Employee employee = employees[random.nextInt(MAXEMPS)];
        Person customerFairy = fairy.person();
        SupportCall.Customer customer = new SupportCall.Customer();
        customer.fullName = customerFairy.getFullName();
        customer.worksFor = customerFairy.getCompany().getName();
        customer.email = customerFairy.getCompanyEmail();
        String telephoneNumber = customerFairy.getTelephoneNumber();
        // remove a digit from telephone to make ssn
        customer.ssn = telephoneNumber.substring(0,4) + telephoneNumber.substring(5);

        SupportCall call = new SupportCall();
        call.clerk = employee;
        call.caller = customer;

        DateProducer dateProducer = fairy.dateProducer();
        DateTime callStart = dateProducer.randomDateInThePast(1);
        call.id = customerFairy.getPassportNumber();
        call.callStartTime = callStart.toDate();
        DateTime callEnd = dateProducer.randomDateBetweenTwoDates(callStart, callStart.plus(100000));
        call.callEndTime = callEnd.toDate();
        call.complianceOfficer = officer;
        call.description = fairy.textProducer().latinSentence();
        return call;
    }


    @Test
    public void makeSome() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        makeEmployees();

        Paths.get("test-data").toFile().mkdirs();
        int howMany = 12;
        SupportCall call = null;
        for (int i=0; i < howMany; i++) {
            call = getCall();

            File targetFile = Paths.get("test-data/" + call.id + ".json").toFile();
            mapper.writerWithDefaultPrettyPrinter().writeValue(targetFile, call);

            String noSpace = call.clerk.fullName.replace(" ","");
            File targetUserFile = Paths.get("user-config/security/users/" + noSpace + ".json").toFile();
            ObjectNode userNode = mapper.createObjectNode();
            ArrayNode role = userNode.putArray("role");
            role.add("clerk");
            userNode.put("user-name", noSpace);
            userNode.put("description","A test clerk");
            userNode.put("password","x");
            mapper.writerWithDefaultPrettyPrinter().writeValue(targetUserFile, userNode);

        }
        // get officer off last call
        String noSpace = call.complianceOfficer.fullName.replace(" ","");
        File mgrUserFile = Paths.get("user-config/security/users/" + noSpace + ".json").toFile();
        ObjectNode userNode = mapper.createObjectNode();
        ArrayNode role = userNode.putArray("role");
        role.add("compliance-officer");
        userNode.put("user-name", noSpace);
        userNode.put("description","A compliance officer.");
        userNode.put("password","x");
        mapper.writerWithDefaultPrettyPrinter().writeValue(mgrUserFile, userNode);
    }

}
