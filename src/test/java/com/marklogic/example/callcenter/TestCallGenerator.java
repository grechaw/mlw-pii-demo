package com.marklogic.example.callcenter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.codearte.jfairy.Fairy;
import io.codearte.jfairy.producer.DateProducer;
import io.codearte.jfairy.producer.person.Person;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class TestCallGenerator {

    Fairy fairy;


    Person manager;
    SupportCall.Employee[] employees;

    Random random;

    int MAXEMPS = 3;

    public TestCallGenerator() {
        fairy = Fairy.create();
        manager = fairy.person();
        random = new Random();
    }

    public void makeEmployees() {
        List<SupportCall.Employee> employeeList = new ArrayList();
        for (int i = 0; i < MAXEMPS; i++) {
            Person employeeFairy = fairy.person();
            SupportCall.Employee employee = new SupportCall.Employee();
            employee.fullName = employeeFairy.getFullName();

            employee.manager = new SupportCall.Employee();
            employee.manager.fullName = manager.getFullName();
            employeeList.add(employee);
        }
        employees = employeeList.toArray(new SupportCall.Employee[] {} );
    }

    public SupportCall getCall() {

        SupportCall.Employee employee = employees[random.nextInt(MAXEMPS)];
        Person customerFairy = fairy.person();
        SupportCall.Customer customer = new SupportCall.Customer();
        customer.fullName = customerFairy.getFullName();
        customer.company = customerFairy.getCompany().getName();
        customer.email = customerFairy.getCompanyEmail();
        String telephoneNumber = customerFairy.getTelephoneNumber();
        // remove a digit from telephone to make ssn
        customer.ssn = telephoneNumber.substring(0,4) + telephoneNumber.substring(5);

        SupportCall call = new SupportCall();
        call.supportTech = employee;
        call.customer = customer;

        DateProducer dateProducer = fairy.dateProducer();
        DateTime callStart = dateProducer.randomDateInThePast(1);
        call.id = customerFairy.getPassportNumber();
        call.callStartTime = callStart.toDate();
        DateTime callEnd = dateProducer.randomDateBetweenTwoDates(callStart, callStart.plus(100000));
        call.callEndTime = callEnd.toDate();
        return call;
    }


    @Test
    public void makeSome() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        makeEmployees();

        Paths.get("test-data").toFile().mkdirs();
        int howMany = 12;
        for (int i=0; i < howMany; i++) {
            SupportCall call = getCall();
            File targetFile = Paths.get("test-data/" + call.id + ".json").toFile();
            mapper.writerWithDefaultPrettyPrinter().writeValue(targetFile, call);
        }
    }

}
