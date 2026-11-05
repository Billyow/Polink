package com.polink.catalog.repository;

import com.polink.catalog.entity.SubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface SubjectRepository extends JpaRepository<SubjectEntity, UUID> {

    List<SubjectEntity> findByActiveTrueAndBranchActiveTrueOrderByNameAsc();

    List<SubjectEntity> findByBranchIdAndActiveTrueAndBranchActiveTrueOrderByNameAsc(UUID branchId);

    List<SubjectEntity> findAllByOrderByNameAsc();

    List<SubjectEntity> findByBranchIdOrderByNameAsc(UUID branchId);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);

    List<SubjectEntity> findByIdInAndActiveTrue(Collection<UUID> ids);
}
