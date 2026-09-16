package com.example.bank.customer.controller.rest;

import com.example.bank.common.response.ApiResponse;
import com.example.bank.customer.dto.request.CustomerRequest;
import com.example.bank.customer.dto.response.CustomerResponse;
import com.example.bank.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(
        name = "Customers",
        description = "Customer management operations"
)
public class CustomerRestController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CustomerRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        customerService.createCustomer(request)
                );
    }

    @PutMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'EMPLOYEE') or " +
                    "(hasRole('CUSTOMER') and " +
                    "@customerAuthorizationService.canAccessCustomer(#id, authentication))"
    )
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {

        CustomerResponse response = customerService.updateCustomer(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "@customerAuthorizationService" +
                    ".canAccessCustomer(#id, authentication)"
    )
    public ResponseEntity<CustomerResponse> getCustomerById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                customerService.getCustomerById(id)
        );
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'EMPLOYEE') or " +
                    "(hasRole('CUSTOMER') and " +
                    "@customerAuthorizationService" +
                    ".canAccessCustomerByUserId(#userId, authentication))"
    )
    public ResponseEntity<CustomerResponse> getCustomerByUserId(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                customerService.getCustomerByUserId(userId)
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {

        return ResponseEntity.ok(
                customerService.getAllCustomers()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCustomer(
            @PathVariable Long id) {

        customerService.deleteCustomer(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{customerId}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<CustomerResponse>>
    deactivateCustomer(
            @PathVariable Long customerId) {

        CustomerResponse response =
                customerService.deactivateCustomer(
                        customerId
                );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    @PatchMapping("/{customerId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerResponse>>
    activateCustomer(
            @PathVariable Long customerId) {

        CustomerResponse response =
                customerService.activateCustomer(
                        customerId
                );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }
}