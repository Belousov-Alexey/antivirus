package ru.mtuci.bvt_demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignatureHistoryRepository extends JpaRepository<SignatureHistoryEntity, Long> {
}