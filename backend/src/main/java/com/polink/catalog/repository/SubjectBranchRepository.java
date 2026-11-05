package com.polink.catalog.repository;

import com.polink.catalog.entity.SubjectBranchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubjectBranchRepository extends JpaRepository<SubjectBranchEntity, UUID> {

    List<SubjectBranchEntity> findByActiveTrueOrderByNameAsc();

    List<SubjectBranchEntity> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}
