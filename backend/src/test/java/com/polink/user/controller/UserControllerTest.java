package com.polink.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polink.common.config.SecurityConfig;
import com.polink.common.dto.PagedResponseDto;
import com.polink.common.exception.GlobalExceptionHandler;
import com.polink.common.security.SecurityUserPrincipal;
import com.polink.user.dto.CreateSupervisorRequestDto;
import com.polink.user.dto.UpdateUserStatusRequestDto;
import com.polink.user.dto.UserResponseDto;
import com.polink.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({UserController.class, AdminUserController.class})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void getMyProfileShouldReturnUserProfileForAuthenticatedUserAnd401WhenAnonymous() throws Exception {
        UUID userId = UUID.randomUUID();
        SecurityUserPrincipal principal = new SecurityUserPrincipal(
                userId,
                "laura@polink.edu.co",
                "hash",
                true,
                Set.of("ROLE_STUDENT")
        );
        when(userService.getCurrentUser(userId)).thenReturn(new UserResponseDto(
                userId,
                "Laura Gómez",
                "laura@polink.edu.co",
                "ACTIVE",
                Set.of("ROLE_STUDENT"),
                Instant.now()
        ));

        mockMvc.perform(get("/api/v1/users/me").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("laura@polink.edu.co"))
                .andExpect(jsonPath("$.fullName").value("Laura Gómez"));

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void adminUserEndpointsShouldSupportListingCreatingSupervisorAndUpdatingStatus() throws Exception {
        UUID adminId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        SecurityUserPrincipal adminPrincipal = new SecurityUserPrincipal(
                adminId,
                "admin@polink.edu.co",
                "hash",
                true,
                Set.of("ROLE_ADMIN")
        );
        SecurityUserPrincipal studentPrincipal = new SecurityUserPrincipal(
                UUID.randomUUID(),
                "student@polink.edu.co",
                "hash",
                true,
                Set.of("ROLE_STUDENT")
        );

        UserResponseDto supervisorDto = new UserResponseDto(
                targetUserId,
                "Carlos Ruiz",
                "carlos@polink.edu.co",
                "ACTIVE",
                Set.of("ROLE_SUPERVISOR"),
                Instant.now()
        );
        UserResponseDto suspendedDto = new UserResponseDto(
                targetUserId,
                "Carlos Ruiz",
                "carlos@polink.edu.co",
                "SUSPENDED",
                Set.of("ROLE_SUPERVISOR"),
                Instant.now()
        );

        when(userService.listUsers(eq("ACTIVE"), eq(0), eq(20)))
                .thenReturn(new PagedResponseDto<>(List.of(supervisorDto), 0, 20, 1, 1));
        when(userService.createSupervisor(eq(adminId), any(CreateSupervisorRequestDto.class)))
                .thenReturn(supervisorDto);
        when(userService.updateUserStatus(eq(adminId), eq(targetUserId), any(UpdateUserStatusRequestDto.class)))
                .thenReturn(suspendedDto);

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("status", "ACTIVE")
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("carlos@polink.edu.co"));

        CreateSupervisorRequestDto createReq = new CreateSupervisorRequestDto(
                "Carlos Ruiz",
                "carlos@polink.edu.co",
                "Supervisor2026!"
        );
        mockMvc.perform(post("/api/v1/admin/supervisors")
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("carlos@polink.edu.co"));

        UpdateUserStatusRequestDto statusReq = new UpdateUserStatusRequestDto("SUSPENDED", "Revisión administrativa");
        mockMvc.perform(patch("/api/v1/admin/users/{id}/status", targetUserId)
                        .with(user(adminPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));

        mockMvc.perform(post("/api/v1/admin/supervisors")
                        .with(user(studentPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
