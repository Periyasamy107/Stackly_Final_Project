package com.example.bank.employee.service;

import com.example.bank.common.enums.Role;
import com.example.bank.common.exception.BusinessException;
import com.example.bank.common.exception.ResourceNotFoundException;
import com.example.bank.employee.dto.request.EmployeeRequest;
import com.example.bank.employee.dto.response.EmployeeResponse;
import com.example.bank.employee.entity.Employee;
import com.example.bank.employee.mapper.EmployeeMapper;
import com.example.bank.employee.repository.EmployeeRepository;
import com.example.bank.user.entity.User;
import com.example.bank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional
    public EmployeeResponse createEmployee(
            EmployeeRequest request) {

        validateUniqueData(request);

        User user =
                userRepository.findById(request.getUserId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found with id: "
                                                + request.getUserId()
                                ));

        if (user.getRole() != Role.EMPLOYEE) {
            throw new BusinessException(
                    "User must have EMPLOYEE role"
            );
        }

        if (employeeRepository.existsByUserId(
                request.getUserId())) {

            throw new BusinessException(
                    "Employee profile already exists for user id: "
                            + request.getUserId()
            );
        }

        Employee employee =
                employeeMapper.toEntity(request);

        employee.setUser(user);

        Employee savedEmployee =
                employeeRepository.save(employee);

        return employeeMapper.toResponse(savedEmployee);
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {
                    "employeeById",
                    "employeeByUserId"
            },
            allEntries = true
    )
    public EmployeeResponse updateEmployee(
            Long id,
            EmployeeRequest request) {

        Employee employee =
                employeeRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Employee not found with id: "
                                                + id
                                ));

        validateUniqueDataForUpdate(
                id,
                request
        );

        if (!employee.getUser().getId()
                .equals(request.getUserId())) {

            User user =
                    userRepository.findById(
                            request.getUserId()
                    ).orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "User not found with id: "
                                            + request.getUserId()
                            ));

            if (user.getRole() != Role.EMPLOYEE) {
                throw new BusinessException(
                        "User must have EMPLOYEE role"
                );
            }

            if (employeeRepository.existsByUserId(
                    request.getUserId())) {

                throw new BusinessException(
                        "Another employee already uses this user"
                );
            }

            employee.setUser(user);
        }

        employeeMapper.updateEntity(
                request,
                employee
        );

        return employeeMapper.toResponse(employee);
    }

    @Override
    @Cacheable(
            cacheNames = "employeeById",
            key = "#id"
    )
    public EmployeeResponse getEmployeeById(Long id) {

        Employee employee =
                employeeRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Employee not found with id: "
                                                + id
                                ));

        return employeeMapper.toResponse(employee);
    }

    @Override
    @Cacheable(
            cacheNames = "employeeByUserId",
            key = "#userId"
    )
    public EmployeeResponse getEmployeeByUserId(
            Long userId) {

        Employee employee =
                employeeRepository.findByUserId(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Employee not found for user id: "
                                                + userId
                                ));

        return employeeMapper.toResponse(employee);
    }

    @Override
    public List<EmployeeResponse> getAllEmployees() {

        return employeeRepository.findAll()
                .stream()
                .map(employeeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {
                    "employeeById",
                    "employeeByUserId"
            },
            allEntries = true
    )
    public void deleteEmployee(Long id) {

        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Employee not found with id: " + id
            );
        }

        throw new BusinessException(
                "Physical deletion of employees is not allowed"
        );
    }

    private void validateUniqueData(
            EmployeeRequest request) {

        if (employeeRepository.existsByEmployeeCode(
                request.getEmployeeCode())) {

            throw new BusinessException(
                    "Employee code already exists: "
                            + request.getEmployeeCode()
            );
        }

        if (employeeRepository.existsByEmail(
                request.getEmail())) {

            throw new BusinessException(
                    "Email already exists: "
                            + request.getEmail()
            );
        }

        if (employeeRepository.existsByPhone(
                request.getPhone())) {

            throw new BusinessException(
                    "Phone already exists: "
                            + request.getPhone()
            );
        }
    }

    private void validateUniqueDataForUpdate(
            Long employeeId,
            EmployeeRequest request) {

        employeeRepository
                .findByEmployeeCode(
                        request.getEmployeeCode()
                )
                .ifPresent(existing -> {

                    if (!existing.getId()
                            .equals(employeeId)) {

                        throw new BusinessException(
                                "Employee code already exists: "
                                        + request.getEmployeeCode()
                        );
                    }
                });

        employeeRepository
                .findByEmail(request.getEmail())
                .ifPresent(existing -> {

                    if (!existing.getId()
                            .equals(employeeId)) {

                        throw new BusinessException(
                                "Email already exists: "
                                        + request.getEmail()
                        );
                    }
                });

        employeeRepository
                .findByPhone(request.getPhone())
                .ifPresent(existing -> {

                    if (!existing.getId()
                            .equals(employeeId)) {

                        throw new BusinessException(
                                "Phone already exists: "
                                        + request.getPhone()
                        );
                    }
                });
    }
}