package com.example.rqchallenge.service;

import com.example.rqchallenge.model.Employee;
import com.example.rqchallenge.model.EmployeeList;
import com.example.rqchallenge.model.EmployeeResponse;
import com.example.rqchallenge.utils.RestEndPointURL;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j

public class EmployeeService implements IEmployeeService {

    @Autowired
    RestTemplate restTemplate;




    public List<Employee> getAllEmployees() throws IOException {
        ResponseEntity<EmployeeList> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(
                    new URI(RestEndPointURL.GET_ALL_EMPLOYEE),
                    HttpMethod.GET,
                    null,
                    EmployeeList.class
            );
        } catch (URISyntaxException e) {
            log.error(e.getMessage());
        }
        log.info("List of all employee:{} ", responseEntity.getBody().getData());
        return responseEntity.getBody().getData();
    }


    public List<Employee> getEmployeesByNameSearch(String searchString) {
        List<Employee> employeeList = null;
        try {
            employeeList = getAllEmployees();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return employeeList.stream()
                .filter(employee -> employee.getEmployee_name().contains(searchString))
                .collect(Collectors.toList());
    }


    public Employee getEmployeeById(String id) {
        ResponseEntity<EmployeeResponse> responseEntity = restTemplate.exchange(
                RestEndPointURL.GET_EMPLOYEE_BY_ID,
                HttpMethod.GET,
                null,
                EmployeeResponse.class,
                id);

        log.info("Details Found for given employee ID :{} ", responseEntity.getBody().getData());
        return responseEntity.getBody().getData();
    }

    public Integer getHighestSalaryOfEmployees() {
        List<Employee> employeeList = null;
        try {
            employeeList = getAllEmployees();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return employeeList.stream()
                .max(Comparator.comparing(Employee::getEmployee_salary))
                .get().getEmployee_salary();
    }

    public List<String> getTopTenHighestEarningEmployeeNames() {
        List<Employee> employeeList = null;
        try {
            employeeList = getAllEmployees();
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        return employeeList.stream().sorted(Comparator.comparing(Employee::getEmployee_salary).reversed())
                .limit(10)
                .map(Employee::getEmployee_name)
                .collect(Collectors.toList());
    }

    public Employee createEmployee(String name, String salary, String age) {
        Employee employee = Employee.builder()
                .employee_name(name)
                .employee_salary(Integer.parseInt(salary))
                .employee_age(Integer.parseInt(age))
                .build();

        ResponseEntity<EmployeeResponse> employeeResponseResponseEntity = restTemplate.exchange(
                RestEndPointURL.CREATE_NEW_EMPLOYEE,
                HttpMethod.POST,
                new HttpEntity<>(employee),
                EmployeeResponse.class);

        log.info("New Employee created with below details  :{} ", employeeResponseResponseEntity.getBody().getData());

        return employeeResponseResponseEntity.getBody().getData();
    }

    public String deleteEmployee(String id) {

        Employee employee = getEmployeeById(id);

        ResponseEntity<EmployeeResponse> employeeResponseResponseEntity = restTemplate.exchange(
                RestEndPointURL.DELETE_EMPLOYEE_BY_ID,
                HttpMethod.DELETE,
                null,
                EmployeeResponse.class,
                id);

        if (employeeResponseResponseEntity.getStatusCode() == HttpStatus.OK)
            return employee.getEmployee_name();

        return "No record found with given ID";
    }
}
