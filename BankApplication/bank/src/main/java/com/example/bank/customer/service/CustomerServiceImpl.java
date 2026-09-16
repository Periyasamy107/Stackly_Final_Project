package com.example.bank.customer.service;

import com.example.bank.common.enums.CustomerStatus;
import com.example.bank.common.enums.Role;
import com.example.bank.common.exception.BusinessException;
import com.example.bank.common.exception.ResourceNotFoundException;
import com.example.bank.common.exception.ValidationException;
import com.example.bank.customer.dto.request.CustomerRequest;
import com.example.bank.customer.dto.response.CustomerResponse;
import com.example.bank.customer.entity.Customer;
import com.example.bank.customer.mapper.CustomerMapper;
import com.example.bank.customer.repository.CustomerRepository;
import com.example.bank.event.CustomerCreatedEvent;
import com.example.bank.user.entity.User;
import com.example.bank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final CustomerMapper customerMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public CustomerResponse createCustomer(
            CustomerRequest request) {

        validateUniqueCustomerData(request);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: "
                                        + request.getUserId()
                        ));

        if (user.getRole() != Role.CUSTOMER) {
            throw new BusinessException(
                    "User must have CUSTOMER role"
            );
        }

        if (customerRepository.existsByUserId(
                request.getUserId())) {

            throw new BusinessException(
                    "Customer profile already exists for user id: "
                            + request.getUserId()
            );
        }

        Customer customer =
                customerMapper.toEntity(request);

        customer.setUser(user);

        Customer savedCustomer =
                customerRepository.save(customer);

        eventPublisher.publishEvent(
                new CustomerCreatedEvent(
                        savedCustomer.getUser().getId(),
                        savedCustomer.getId(),
                        savedCustomer.getName()
                )
        );

        return customerMapper.toResponse(savedCustomer);
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {
                    "customerById",
                    "customerByUserId"
            },
            allEntries = true
    )
    public CustomerResponse updateCustomer(
            Long id,
            CustomerRequest request) {

        Customer customer =
                customerRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found with id: "
                                                + id
                                ));

        validateUniqueCustomerDataForUpdate(
                id,
                request
        );

        if (!customer.getUser().getId()
                .equals(request.getUserId())) {

            User user =
                    userRepository.findById(
                            request.getUserId()
                    ).orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "User not found with id: "
                                            + request.getUserId()
                            ));

            if (user.getRole() != Role.CUSTOMER) {
                throw new BusinessException(
                        "User must have CUSTOMER role"
                );
            }

            if (customerRepository.existsByUserId(
                    request.getUserId())) {

                throw new BusinessException(
                        "Another customer already uses this user"
                );
            }

            customer.setUser(user);
        }

        customerMapper.updateEntity(
                request,
                customer
        );

        return customerMapper.toResponse(customer);
    }

    @Override
    @Cacheable(
            cacheNames = "customerById",
            key = "#id"
    )
    public CustomerResponse getCustomerById(Long id) {

        Customer customer =
                customerRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found with id: "
                                                + id
                                ));

        return customerMapper.toResponse(customer);
    }

    @Override
    @Cacheable(
            cacheNames = "customerByUserId",
            key = "#userId"
    )
    public CustomerResponse getCustomerByUserId(
            Long userId) {

        Customer customer =
                customerRepository.findByUserId(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found for user id: "
                                                + userId
                                ));

        return customerMapper.toResponse(customer);
    }

    @Override
    public List<CustomerResponse> getAllCustomers() {

        return customerRepository.findAll()
                .stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteCustomer(Long customerId) {

        if (customerId == null) {
            throw new ValidationException(
                    "Customer id is required"
            );
        }

        Customer customer =
                customerRepository.findByIdForUpdate(
                                customerId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found with id: "
                                                + customerId
                                )
                        );

        throw new BusinessException(
                "Customer deletion is not permitted. "
                        + "Deactivate the customer instead."
        );
    }

    private void validateUniqueCustomerData(
            CustomerRequest request) {

        if (customerRepository.existsByEmail(
                request.getEmail())) {

            throw new BusinessException(
                    "Email already exists: "
                            + request.getEmail()
            );
        }

        if (customerRepository.existsByPhone(
                request.getPhone())) {

            throw new BusinessException(
                    "Phone already exists: "
                            + request.getPhone()
            );
        }
    }

    private void validateUniqueCustomerDataForUpdate(
            Long customerId,
            CustomerRequest request) {

        customerRepository.findByEmail(request.getEmail())
                .ifPresent(existing -> {

                    if (!existing.getId().equals(customerId)) {
                        throw new BusinessException(
                                "Email already exists: "
                                        + request.getEmail()
                        );
                    }
                });

        customerRepository.findByPhone(request.getPhone())
                .ifPresent(existing -> {

                    if (!existing.getId().equals(customerId)) {
                        throw new BusinessException(
                                "Phone already exists: "
                                        + request.getPhone()
                        );
                    }
                });
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {
                    "customerById",
                    "customerByUserId"
            },
            allEntries = true
    )
    public CustomerResponse deactivateCustomer(
            Long customerId) {

        Customer customer =
                customerRepository.findByIdForUpdate(
                                customerId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found with id: "
                                                + customerId
                                )
                        );

        if (customer.getStatus()
                == CustomerStatus.INACTIVE) {

            throw new BusinessException(
                    "Customer is already inactive"
            );
        }

        customer.setStatus(
                CustomerStatus.INACTIVE
        );

        Customer savedCustomer =
                customerRepository.save(customer);

        return customerMapper.toResponse(
                savedCustomer
        );
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = {
                    "customerById",
                    "customerByUserId"
            },
            allEntries = true
    )
    public CustomerResponse activateCustomer(
            Long customerId) {

        Customer customer =
                customerRepository.findByIdForUpdate(
                                customerId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found with id: "
                                                + customerId
                                )
                        );

        if (customer.getStatus()
                == CustomerStatus.ACTIVE) {

            throw new BusinessException(
                    "Customer is already active"
            );
        }

        customer.setStatus(
                CustomerStatus.ACTIVE
        );

        Customer savedCustomer =
                customerRepository.save(customer);

        return customerMapper.toResponse(
                savedCustomer
        );
    }
}