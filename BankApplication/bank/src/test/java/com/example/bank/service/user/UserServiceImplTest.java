package com.example.bank.service.user;

import com.example.bank.common.enums.Role;
import com.example.bank.common.enums.UserStatus;
import com.example.bank.common.exception.BusinessException;
import com.example.bank.common.exception.ResourceNotFoundException;
import com.example.bank.user.dto.request.UserRequest;
import com.example.bank.user.dto.response.UserResponse;
import com.example.bank.user.entity.User;
import com.example.bank.user.mapper.UserMapper;
import com.example.bank.user.repository.UserRepository;
import com.example.bank.user.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRequest request;
    private User user;
    private UserResponse response;

    @BeforeEach
    void setUp() {

        request = UserRequest.builder()
                .username("john123")
                .password("Password@123")
                .role(Role.CUSTOMER)
                .build();

        user = User.builder()
                .id(1L)
                .username("john123")
                .passwordHash("old-hash")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        response = UserResponse.builder()
                .id(1L)
                .username("john123")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    void createUser_shouldCreateUser() {

        when(userRepository.existsByUsername("john123"))
                .thenReturn(false);

        when(userMapper.toEntity(request))
                .thenReturn(user);

        when(passwordEncoder.encode("Password@123"))
                .thenReturn("encoded-password");

        when(userRepository.save(user))
                .thenReturn(user);

        when(userMapper.toResponse(user))
                .thenReturn(response);

        UserResponse result =
                userService.createUser(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("john123", result.getUsername());

        assertEquals(
                "encoded-password",
                user.getPasswordHash()
        );

        verify(userRepository)
                .existsByUsername("john123");

        verify(userMapper)
                .toEntity(request);

        verify(passwordEncoder)
                .encode("Password@123");

        verify(userRepository)
                .save(user);

        verify(userMapper)
                .toResponse(user);
    }

    @Test
    void createUser_whenUsernameExists_shouldThrowException() {

        when(userRepository.existsByUsername("john123"))
                .thenReturn(true);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> userService.createUser(request)
                );

        assertEquals(
                "Username already exists: john123",
                exception.getMessage()
        );

        verify(userRepository)
                .existsByUsername("john123");

        verify(userRepository, never())
                .save(any(User.class));

        verifyNoInteractions(
                userMapper,
                passwordEncoder
        );
    }

    @Test
    void updateUser_shouldUpdateUser() {

        UserRequest updateRequest =
                UserRequest.builder()
                        .username("john_updated")
                        .password("NewPassword@123")
                        .role(Role.CUSTOMER)
                        .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByUsername("john_updated"))
                .thenReturn(false);

        when(passwordEncoder.encode("NewPassword@123"))
                .thenReturn("new-encoded-password");

        when(userRepository.save(user))
                .thenReturn(user);

        when(userMapper.toResponse(user))
                .thenReturn(response);

        UserResponse result =
                userService.updateUser(1L, updateRequest);

        assertNotNull(result);

        assertEquals(
                "new-encoded-password",
                user.getPasswordHash()
        );

        verify(userRepository)
                .findById(1L);

        verify(userRepository)
                .existsByUsername("john_updated");

        verify(userMapper)
                .updateEntity(updateRequest, user);

        verify(passwordEncoder)
                .encode("NewPassword@123");

        verify(userRepository)
                .save(user);

        verify(userMapper)
                .toResponse(user);
    }

    @Test
    void updateUser_whenUserNotFound_shouldThrowException() {

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> userService.updateUser(
                                99L,
                                request
                        )
                );

        assertEquals(
                "User not found with id: 99",
                exception.getMessage()
        );

        verify(userRepository)
                .findById(99L);

        verify(userRepository, never())
                .save(any(User.class));

        verifyNoInteractions(
                userMapper,
                passwordEncoder
        );
    }

    @Test
    void updateUser_whenUsernameBelongsToAnotherUser_shouldThrowException() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserRequest updateRequest =
                UserRequest.builder()
                        .username("another-user")
                        .password("Password@123")
                        .role(Role.CUSTOMER)
                        .build();

        when(userRepository.existsByUsername("another-user"))
                .thenReturn(true);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> userService.updateUser(
                                1L,
                                updateRequest
                        )
                );

        assertEquals(
                "Username already exists: another-user",
                exception.getMessage()
        );

        verify(userRepository)
                .findById(1L);

        verify(userRepository)
                .existsByUsername("another-user");

        verify(userRepository, never())
                .save(any(User.class));

        verify(passwordEncoder, never())
                .encode(anyString());
    }

    @Test
    void updateUser_withSameUsername_shouldNotCheckUsernameConflict()
    {

        UserRequest updateRequest =
                UserRequest.builder()
                        .username("john123")
                        .password("NewPassword@123")
                        .role(Role.CUSTOMER)
                        .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.encode("NewPassword@123"))
                .thenReturn("new-hash");

        when(userRepository.save(user))
                .thenReturn(user);

        when(userMapper.toResponse(user))
                .thenReturn(response);

        userService.updateUser(1L, updateRequest);

        verify(userRepository)
                .findById(1L);

        verify(userRepository, never())
                .existsByUsername("john123");

        verify(userMapper)
                .updateEntity(updateRequest, user);

        verify(passwordEncoder)
                .encode("NewPassword@123");

        verify(userRepository)
                .save(user);
    }

    @Test
    void updateUser_whenPasswordIsNull_shouldKeepExistingPassword()
    {

        String oldPasswordHash =
                user.getPasswordHash();

        UserRequest updateRequest =
                UserRequest.builder()
                        .username("john123")
                        .password(null)
                        .role(Role.CUSTOMER)
                        .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        when(userMapper.toResponse(user))
                .thenReturn(response);

        userService.updateUser(1L, updateRequest);

        assertEquals(
                oldPasswordHash,
                user.getPasswordHash()
        );

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository)
                .save(user);
    }

    @Test
    void updateUser_whenPasswordBlank_shouldKeepExistingPassword()
    {

        String oldPasswordHash =
                user.getPasswordHash();

        UserRequest updateRequest =
                UserRequest.builder()
                        .username("john123")
                        .password("   ")
                        .role(Role.CUSTOMER)
                        .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        when(userMapper.toResponse(user))
                .thenReturn(response);

        userService.updateUser(1L, updateRequest);

        assertEquals(
                oldPasswordHash,
                user.getPasswordHash()
        );

        verify(passwordEncoder, never())
                .encode(anyString());
    }

    @Test
    void getUserById_shouldReturnUser() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponse(user))
                .thenReturn(response);

        UserResponse result =
                userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("john123", result.getUsername());

        verify(userRepository)
                .findById(1L);

        verify(userMapper)
                .toResponse(user);
    }

    @Test
    void getUserById_whenNotFound_shouldThrowException() {

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(99L)
        );

        verify(userRepository)
                .findById(99L);

        verifyNoInteractions(userMapper);
    }

    @Test
    void getUserByUsername_shouldReturnUser() {

        when(userRepository.findByUsername("john123"))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponse(user))
                .thenReturn(response);

        UserResponse result =
                userService.getUserByUsername("john123");

        assertNotNull(result);
        assertEquals(
                "john123",
                result.getUsername()
        );

        verify(userRepository)
                .findByUsername("john123");

        verify(userMapper)
                .toResponse(user);
    }

    @Test
    void getUserByUsername_whenNotFound_shouldThrowException() {

        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> userService.getUserByUsername(
                                "unknown"
                        )
                );

        assertEquals(
                "User not found with username: unknown",
                exception.getMessage()
        );

        verify(userRepository)
                .findByUsername("unknown");

        verifyNoInteractions(userMapper);
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {

        User user2 = User.builder()
                .id(2L)
                .username("jane123")
                .passwordHash("hash")
                .role(Role.EMPLOYEE)
                .status(UserStatus.ACTIVE)
                .build();

        UserResponse response2 =
                UserResponse.builder()
                        .id(2L)
                        .username("jane123")
                        .role(Role.EMPLOYEE)
                        .status(UserStatus.ACTIVE)
                        .build();

        when(userRepository.findAll())
                .thenReturn(List.of(user, user2));

        when(userMapper.toResponse(user))
                .thenReturn(response);

        when(userMapper.toResponse(user2))
                .thenReturn(response2);

        List<UserResponse> result =
                userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(
                "john123",
                result.get(0).getUsername()
        );
        assertEquals(
                "jane123",
                result.get(1).getUsername()
        );

        verify(userRepository)
                .findAll();

        verify(userMapper)
                .toResponse(user);

        verify(userMapper)
                .toResponse(user2);
    }

    @Test
    void getAllUsers_whenNoUsers_shouldReturnEmptyList() {

        when(userRepository.findAll())
                .thenReturn(List.of());

        List<UserResponse> result =
                userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository)
                .findAll();

        verifyNoInteractions(userMapper);
    }

    @Test
    void deleteUser_whenUserExists_shouldRejectPhysicalDeletion() {

        when(userRepository.existsById(1L))
                .thenReturn(true);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> userService.deleteUser(1L)
                );

        assertEquals(
                "Physical deletion of users is not allowed",
                exception.getMessage()
        );

        verify(userRepository)
                .existsById(1L);

        verify(userRepository, never())
                .deleteById(anyLong());
    }

    @Test
    void deleteUser_whenUserDoesNotExist_shouldThrowNotFound()
    {

        when(userRepository.existsById(99L))
                .thenReturn(false);

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> userService.deleteUser(99L)
                );

        assertEquals(
                "User not found with id: 99",
                exception.getMessage()
        );

        verify(userRepository)
                .existsById(99L);

        verify(userRepository, never())
                .deleteById(anyLong());
    }
}