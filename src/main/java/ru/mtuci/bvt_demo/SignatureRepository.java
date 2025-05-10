package ru.mtuci.bvt_demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SignatureRepository extends JpaRepository<SignatureEntity, Long> {
    List<SignatureEntity> findByStatus(String status);
}
