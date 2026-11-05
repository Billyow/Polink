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
import com.polink.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final SubjectBranchRepository branchRepository;
    private final SubjectRepository subjectRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<BranchResponseDto> listPublicBranches() {
        return branchRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(BranchResponseDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubjectResponseDto> listPublicSubjects(UUID branchId) {
        List<SubjectEntity> subjects = (branchId == null)
                ? subjectRepository.findByActiveTrueAndBranchActiveTrueOrderByNameAsc()
                : subjectRepository.findByBranchIdAndActiveTrueAndBranchActiveTrueOrderByNameAsc(branchId);
        return subjects.stream()
                .map(SubjectResponseDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BranchResponseDto> listAllBranches() {
        return branchRepository.findAllByOrderByNameAsc().stream()
                .map(BranchResponseDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubjectResponseDto> listAllSubjects(UUID branchId) {
        List<SubjectEntity> subjects = (branchId == null)
                ? subjectRepository.findAllByOrderByNameAsc()
                : subjectRepository.findByBranchIdOrderByNameAsc(branchId);
        return subjects.stream()
                .map(SubjectResponseDto::fromEntity)
                .toList();
    }

    @Transactional
    public BranchResponseDto createBranch(UUID adminUserId, BranchRequestDto request) {
        String normalizedName = request.name().trim();
        if (branchRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ConflictException("Ya existe una rama con el nombre " + normalizedName);
        }

        SubjectBranchEntity entity = SubjectBranchEntity.builder()
                .id(UUID.randomUUID())
                .name(normalizedName)
                .description(request.description() == null ? null : request.description().trim())
                .active(request.active() == null || request.active())
                .createdAt(Instant.now())
                .build();

        SubjectBranchEntity saved = branchRepository.save(entity);
        auditLogService.recordAction(
                adminUserId,
                "ROLE_ADMIN",
                "CREATE_BRANCH",
                "SUBJECT_BRANCH",
                saved.getId(),
                "Creación de rama académica: " + saved.getName()
        );
        return BranchResponseDto.fromEntity(saved);
    }

    @Transactional
    public BranchResponseDto updateBranch(UUID adminUserId, UUID branchId, BranchRequestDto request) {
        SubjectBranchEntity entity = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Rama académica no encontrada"));

        String normalizedName = request.name().trim();
        if (branchRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, branchId)) {
            throw new ConflictException("Ya existe otra rama con el nombre " + normalizedName);
        }

        entity.setName(normalizedName);
        entity.setDescription(request.description() == null ? null : request.description().trim());
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        SubjectBranchEntity saved = branchRepository.save(entity);
        auditLogService.recordAction(
                adminUserId,
                "ROLE_ADMIN",
                "UPDATE_BRANCH",
                "SUBJECT_BRANCH",
                saved.getId(),
                "Actualización de rama académica: " + saved.getName() + " (active=" + saved.isActive() + ")"
        );
        return BranchResponseDto.fromEntity(saved);
    }

    @Transactional
    public SubjectResponseDto createSubject(UUID adminUserId, SubjectRequestDto request) {
        String normalizedCode = request.code().trim().toUpperCase();
        if (subjectRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new ConflictException("Ya existe una asignatura con el código " + normalizedCode);
        }

        SubjectBranchEntity branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new ResourceNotFoundException("La rama académica seleccionada no existe"));

        SubjectEntity entity = SubjectEntity.builder()
                .id(UUID.randomUUID())
                .branch(branch)
                .code(normalizedCode)
                .name(request.name().trim())
                .active(request.active() == null || request.active())
                .createdAt(Instant.now())
                .build();

        SubjectEntity saved = subjectRepository.save(entity);
        auditLogService.recordAction(
                adminUserId,
                "ROLE_ADMIN",
                "CREATE_SUBJECT",
                "SUBJECT",
                saved.getId(),
                "Creación de asignatura " + saved.getCode() + " - " + saved.getName()
        );
        return SubjectResponseDto.fromEntity(saved);
    }

    @Transactional
    public SubjectResponseDto updateSubject(UUID adminUserId, UUID subjectId, SubjectRequestDto request) {
        SubjectEntity entity = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignatura no encontrada"));

        String normalizedCode = request.code().trim().toUpperCase();
        if (subjectRepository.existsByCodeIgnoreCaseAndIdNot(normalizedCode, subjectId)) {
            throw new ConflictException("Ya existe otra asignatura con el código " + normalizedCode);
        }

        SubjectBranchEntity branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new ResourceNotFoundException("La rama académica seleccionada no existe"));

        entity.setBranch(branch);
        entity.setCode(normalizedCode);
        entity.setName(request.name().trim());
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        SubjectEntity saved = subjectRepository.save(entity);
        auditLogService.recordAction(
                adminUserId,
                "ROLE_ADMIN",
                "UPDATE_SUBJECT",
                "SUBJECT",
                saved.getId(),
                "Actualización de asignatura " + saved.getCode() + " - " + saved.getName() + " (active=" + saved.isActive() + ")"
        );
        return SubjectResponseDto.fromEntity(saved);
    }
}
