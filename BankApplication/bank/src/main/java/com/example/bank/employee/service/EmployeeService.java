package com.example.bank.employee.service;

import com.example.bank.employee.dto.request.EmployeeRequest;
import com.example.bank.employee.dto.response.EmployeeResponse;

import java.util.List;

public interface EmployeeService {

    EmployeeResponse createEmployee(
            EmployeeRequest request
    );

    EmployeeResponse updateEmployee(
            Long id,
            EmployeeRequest request
    );

    EmployeeResponse getEmployeeById(Long id);

    EmployeeResponse getEmployeeByUserId(Long userId);

    List<EmployeeResponse> getAllEmployees();

    void deleteEmployee(Long id);
}