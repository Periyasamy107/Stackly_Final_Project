package com.example.bank.admin.service;

import com.example.bank.admin.dto.request.AdminRequest;
import com.example.bank.admin.dto.response.AdminResponse;

import java.util.List;

public interface AdminService {

    AdminResponse createAdmin(AdminRequest request);

    AdminResponse updateAdmin(
            Long id,
            AdminRequest request
    );

    AdminResponse getAdminById(Long id);

    List<AdminResponse> getAllAdmins();

    void deactivateAdmin(Long id);
}