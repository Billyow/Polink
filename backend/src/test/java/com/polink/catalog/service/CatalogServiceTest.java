package com.polink.catalog.service;

import com.polink.audit.service.AuditLogService;
import com.polink.catalog.dto.BranchRequestDto;
import com.polink.catalog.dto.BranchResponseDto;
import com.polink.catalog.dto.SubjectRequestDto;
import com.polink.catalog.dto.SubjectResponseDto;
import com.polink.catalog.entity.SubjectBranchEntity;
import com.polink.catalog.entity.SubjectEntity;
import com.polink.catalog.repository.SubjectBranchRepository;
import com.polink.catalog.repository.SubjectRepository;
import com.polink.common.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private SubjectBranchRepository branchRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private CatalogService catalogService;

    @Test
    void createAndUpdateBranchShouldPersistAndRecordAudit() {
        UUID adminId = UUID.randomUUID();
        when(branchRepository.existsByNameIgnoreCase("Ciencias Básicas")).thenReturn(false);
        when(branchRepository.save(any(SubjectBranchEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        BranchResponseDto created = catalogService.createBranch(
                adminId,
                new BranchRequestDto("Ciencias Básicas", "Matemáticas y física", true)
        );

        assertThat(created.name()).isEqualTo("Ciencias Básicas");
        assertThat(created.active()).isTrue();
        verify(auditLogService).recordAction(
                eq(adminId),
                eq("ROLE_ADMIN"),
                eq("CREATE_BRANCH"),
                eq("SUBJECT_BRANCH"),
                eq(created.id()),
                any()
        );
    }

    @Test
    void createSubjectShouldValidateUniqueCodeAndBranchAndRecordAudit() {
        UUID adminId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        SubjectBranchEntity branch = SubjectBranchEntity.builder()
                .id(branchId)
                .name("Ciencias Básicas")
                .active(true)
                .createdAt(Instant.now())
                .build();

        when(subjectRepository.existsByCodeIgnoreCase("CB-105")).thenReturn(false);
        when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
        when(subjectRepository.save(any(SubjectEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        SubjectResponseDto subject = catalogService.createSubject(
                adminId,
                new SubjectRequestDto(branchId, "cb-105", "Ecuaciones Diferenciales", true)
        );

        assertThat(subject.code()).isEqualTo("CB-105");
        assertThat(subject.branchName()).isEqualTo("Ciencias Básicas");
        verify(auditLogService).recordAction(
                eq(adminId),
                eq("ROLE_ADMIN"),
                eq("CREATE_SUBJECT"),
                eq("SUBJECT"),
                eq(subject.id()),
                any()
        );

        when(subjectRepository.existsByCodeIgnoreCase("CB-105")).thenReturn(true);
        assertThatThrownBy(() -> catalogService.createSubject(
                adminId,
                new SubjectRequestDto(branchId, "CB-105", "Duplicada", true)
        )).isInstanceOf(ConflictException.class);
    }

    @Test
    void listPublicSubjectsShouldFilterByBranchWhenProvided() {
        UUID branchId = UUID.randomUUID();
        SubjectBranchEntity branch = new SubjectBranchEntity(branchId, "Ciencias Básicas", "Desc", true, Instant.now());
        SubjectEntity subj = new SubjectEntity(UUID.randomUUID(), branch, "CB-101", "Cálculo Diferencial", true, Instant.now());

        when(subjectRepository.findByBranchIdAndActiveTrueOrderByNameAsc(branchId)).thenReturn(List.of(subj));

        List<SubjectResponseDto> result = catalogService.listPublicSubjects(branchId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("CB-101");
    }
}
