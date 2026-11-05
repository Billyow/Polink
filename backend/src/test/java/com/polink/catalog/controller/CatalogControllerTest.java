package com.polink.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polink.catalog.dto.BranchRequestDto;
import com.polink.catalog.dto.BranchResponseDto;
import com.polink.catalog.dto.SubjectRequestDto;
import com.polink.catalog.dto.SubjectResponseDto;
import com.polink.catalog.service.CatalogService;
import com.polink.common.config.SecurityConfig;
import com.polink.common.exception.GlobalExceptionHandler;
import com.polink.common.security.SecurityUserPrincipal;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({CatalogController.class, AdminCatalogController.class})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CatalogService catalogService;

    @Test
    void publicCatalogEndpointsShouldBeAccessibleWithoutAuthentication() throws Exception {
        UUID branchId = UUID.randomUUID();
        when(catalogService.listPublicBranches()).thenReturn(List.of(
                new BranchResponseDto(branchId, "Ciencias Básicas", "Desc", true, Instant.now())
        ));
        when(catalogService.listPublicSubjects(isNull())).thenReturn(List.of(
                new SubjectResponseDto(UUID.randomUUID(), branchId, "Ciencias Básicas", "CB-101", "Cálculo Diferencial", true, Instant.now())
        ));

        mockMvc.perform(get("/api/v1/catalog/branches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Ciencias Básicas"));

        mockMvc.perform(get("/api/v1/catalog/subjects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("CB-101"));
    }

    @Test
    void adminCatalogEndpointsShouldEnforceRoleAndSupportFullCrud() throws Exception {
        UUID adminId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        SecurityUserPrincipal admin = new SecurityUserPrincipal(
                adminId, "admin@polink.edu.co", "h", true, Set.of("ROLE_ADMIN")
        );
        SecurityUserPrincipal student = new SecurityUserPrincipal(
                UUID.randomUUID(), "st@polink.edu.co", "h", true, Set.of("ROLE_STUDENT")
        );

        BranchRequestDto branchReq = new BranchRequestDto("IA", "Inteligencia Artificial", true);
        BranchResponseDto branchResp = new BranchResponseDto(branchId, "IA", "Inteligencia Artificial", true, Instant.now());
        SubjectRequestDto subjectReq = new SubjectRequestDto(branchId, "IA-101", "Machine Learning", true);
        SubjectResponseDto subjectResp = new SubjectResponseDto(subjectId, branchId, "IA", "IA-101", "Machine Learning", true, Instant.now());

        when(catalogService.listAllBranches()).thenReturn(List.of(branchResp));
        when(catalogService.listAllSubjects(isNull())).thenReturn(List.of(subjectResp));
        when(catalogService.createBranch(eq(adminId), any(BranchRequestDto.class))).thenReturn(branchResp);
        when(catalogService.updateBranch(eq(adminId), eq(branchId), any(BranchRequestDto.class))).thenReturn(branchResp);
        when(catalogService.createSubject(eq(adminId), any(SubjectRequestDto.class))).thenReturn(subjectResp);
        when(catalogService.updateSubject(eq(adminId), eq(subjectId), any(SubjectRequestDto.class))).thenReturn(subjectResp);

        mockMvc.perform(get("/api/v1/admin/catalog/branches").with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("IA"));

        mockMvc.perform(get("/api/v1/admin/catalog/subjects").with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("IA-101"));

        mockMvc.perform(post("/api/v1/admin/catalog/branches")
                        .with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(branchReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("IA"));

        mockMvc.perform(put("/api/v1/admin/catalog/branches/{id}", branchId)
                        .with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(branchReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(branchId.toString()));

        mockMvc.perform(post("/api/v1/admin/catalog/subjects")
                        .with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subjectReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("IA-101"));

        mockMvc.perform(put("/api/v1/admin/catalog/subjects/{id}", subjectId)
                        .with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subjectReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(subjectId.toString()));

        mockMvc.perform(post("/api/v1/admin/catalog/branches")
                        .with(user(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(branchReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(get("/api/v1/admin/catalog/branches"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
