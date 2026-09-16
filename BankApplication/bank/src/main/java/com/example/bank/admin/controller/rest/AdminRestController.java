package com.example.bank.admin.controller.rest;

import com.example.bank.admin.dto.request.AdminRequest;
import com.example.bank.admin.dto.response.AdminResponse;
import com.example.bank.admin.service.AdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admins")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(
        name = "Admins",
        description = "Administrator management operations"
)
public class AdminRestController {

    private final AdminService adminService;

    @PostMapping
    public ResponseEntity<AdminResponse> createAdmin(
            @Valid @RequestBody AdminRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        adminService.createAdmin(request)
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminResponse> updateAdmin(
            @PathVariable Long id,
            @Valid @RequestBody AdminRequest request) {

        return ResponseEntity.ok(
                adminService.updateAdmin(
                        id,
                        request
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminResponse> getAdminById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                adminService.getAdminById(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<AdminResponse>> getAllAdmins() {

        return ResponseEntity.ok(
                adminService.getAllAdmins()
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateAdmin(
            @PathVariable Long id) {

        adminService.deactivateAdmin(id);

        return ResponseEntity.noContent().build();
    }
}