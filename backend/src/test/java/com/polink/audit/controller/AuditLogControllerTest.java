package com.polink.audit.controller;

import com.polink.audit.dto.AuditLogResponseDto;
import com.polink.audit.service.AuditLogService;
import com.polink.common.config.SecurityConfig;
import com.polink.common.dto.PagedResponseDto;
import com.polink.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditLogController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditLogService auditLogService;

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void listAuditLogsShouldReturn200ForAdmin() throws Exception {
        UUID logId = UUID.randomUUID();
        AuditLogResponseDto item = new AuditLogResponseDto(
                logId,
                UUID.randomUUID(),
                "ROLE_SUPERVISOR",
                "APPROVE_TUTOR",
                "TUTOR_PROFILE",
                UUID.randomUUID(),
                "Promedio verificado en 4.5",
                Instant.now()
        );
        when(auditLogService.listAuditLogs(eq("APPROVE_TUTOR"), isNull(), eq(0), eq(20)))
                .thenReturn(new PagedResponseDto<>(List.of(item), 0, 20, 1, 1));

        mockMvc.perform(get("/api/v1/admin/audit-logs")
                        .param("actionType", "APPROVE_TUTOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(logId.toString()))
                .andExpect(jsonPath("$.content[0].actionType").value("APPROVE_TUTOR"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_STUDENT")
    void listAuditLogsShouldReturn403ForStudent() throws Exception {
        mockMvc.perform(get("/api/v1/admin/audit-logs"))
                .andExpect(status().isForbidden());
    }
}
