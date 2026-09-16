package com.example.bank.controller.user;

import com.example.bank.common.enums.Role;
import com.example.bank.common.enums.UserStatus;
import com.example.bank.common.exception.BusinessException;
import com.example.bank.common.exception.ResourceNotFoundException;
import com.example.bank.user.controller.rest.UserRestController;
import com.example.bank.user.dto.request.UserRequest;
import com.example.bank.user.dto.response.UserResponse;
import com.example.bank.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper =
            JsonMapper.builder()
                    .findAndAddModules()
                    .build();

    @MockitoBean
    private UserService userService;

    private UserRequest validRequest() {
        return UserRequest.builder()
                .username("john123")
                .password("Password@123")
                .role(Role.CUSTOMER)
                .build();
    }

    private UserResponse userResponse() {
        return UserResponse.builder()
                .id(1L)
                .username("john123")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createUser_shouldReturnCreated() throws Exception {

        when(userService.createUser(any(UserRequest.class)))
                .thenReturn(userResponse());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("john123"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(userService).createUser(any(UserRequest.class));
    }

    @Test
    void createUser_whenUsernameBlank_shouldReturnBadRequest()
            throws Exception {

        UserRequest request = validRequest();
        request.setUsername("");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void createUser_whenUsernameTooShort_shouldReturnBadRequest()
            throws Exception {

        UserRequest request = validRequest();
        request.setUsername("abc");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void createUser_whenPasswordBlank_shouldReturnBadRequest()
            throws Exception {

        UserRequest request = validRequest();
        request.setPassword("");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void createUser_whenPasswordTooShort_shouldReturnBadRequest()
            throws Exception {

        UserRequest request = validRequest();
        request.setPassword("Abc@1");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void createUser_whenRoleNull_shouldReturnBadRequest()
            throws Exception {

        UserRequest request = validRequest();
        request.setRole(null);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void createUser_whenBodyMissing_shouldReturnBadRequest()
            throws Exception {

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void createUser_whenUsernameAlreadyExists_shouldReturnConflict()
            throws Exception {

        when(userService.createUser(any(UserRequest.class)))
                .thenThrow(new BusinessException(
                        "Username already exists: john123"
                ));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict());

        verify(userService).createUser(any(UserRequest.class));
    }

    @Test
    void updateUser_shouldReturnOk() throws Exception {

        UserRequest request = validRequest();
        request.setUsername("john_updated");

        UserResponse response = userResponse();
        response.setUsername("john_updated");

        when(userService.updateUser(eq(1L), any(UserRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("john_updated"));

        verify(userService).updateUser(eq(1L), any(UserRequest.class));
    }

    @Test
    void updateUser_whenInvalidRequest_shouldReturnBadRequest()
            throws Exception {

        UserRequest request = validRequest();
        request.setUsername("");

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void updateUser_whenUserNotFound_shouldReturnNotFound()
            throws Exception {

        when(userService.updateUser(eq(99L), any(UserRequest.class)))
                .thenThrow(new ResourceNotFoundException(
                        "User not found with id: 99"
                ));

        mockMvc.perform(put("/api/v1/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound());

        verify(userService).updateUser(eq(99L), any(UserRequest.class));
    }

    @Test
    void updateUser_whenUsernameAlreadyExists_shouldReturnConflict()
            throws Exception {

        when(userService.updateUser(eq(1L), any(UserRequest.class)))
                .thenThrow(new BusinessException(
                        "Username already exists: john123"
                ));

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict());
    }

    @Test
    void getUserById_shouldReturnOk() throws Exception {

        when(userService.getUserById(1L))
                .thenReturn(userResponse());

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("john123"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(userService).getUserById(1L);
    }

    @Test
    void getUserById_whenNotFound_shouldReturnNotFound()
            throws Exception {

        when(userService.getUserById(99L))
                .thenThrow(new ResourceNotFoundException(
                        "User not found with id: 99"
                ));

        mockMvc.perform(get("/api/v1/users/99"))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(99L);
    }

    @Test
    void getUserByUsername_shouldReturnOk() throws Exception {

        when(userService.getUserByUsername("john123"))
                .thenReturn(userResponse());

        mockMvc.perform(get("/api/v1/users/username/john123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("john123"));

        verify(userService).getUserByUsername("john123");
    }

    @Test
    void getUserByUsername_whenNotFound_shouldReturnNotFound()
            throws Exception {

        when(userService.getUserByUsername("unknown"))
                .thenThrow(new ResourceNotFoundException(
                        "User not found with username: unknown"
                ));

        mockMvc.perform(get("/api/v1/users/username/unknown"))
                .andExpect(status().isNotFound());

        verify(userService).getUserByUsername("unknown");
    }

    @Test
    void getAllUsers_shouldReturnOk() throws Exception {

        UserResponse user1 = userResponse();

        UserResponse user2 = UserResponse.builder()
                .id(2L)
                .username("jane123")
                .role(Role.EMPLOYEE)
                .status(UserStatus.ACTIVE)
                .build();

        when(userService.getAllUsers())
                .thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("john123"))
                .andExpect(jsonPath("$[1].username").value("jane123"));

        verify(userService).getAllUsers();
    }

    @Test
    void getAllUsers_whenEmpty_shouldReturnEmptyList()
            throws Exception {

        when(userService.getAllUsers())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userService).getAllUsers();
    }

    @Test
    void deleteUser_whenServiceRejectsDeletion_shouldReturnConflict()
            throws Exception {

        doThrow(new BusinessException(
                "Physical deletion of users is not allowed"
        ))
                .when(userService)
                .deleteUser(1L);

        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isConflict());

        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_whenUserNotFound_shouldReturnNotFound()
            throws Exception {

        doThrow(new ResourceNotFoundException(
                "User not found with id: 99"
        ))
                .when(userService)
                .deleteUser(99L);

        mockMvc.perform(delete("/api/v1/users/99"))
                .andExpect(status().isNotFound());

        verify(userService).deleteUser(99L);
    }
}