package com.cloud.vaccinewallet.repositories;

import com.cloud.vaccinewallet.beans.VaccineInformation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VaccineInformationRepository extends JpaRepository<VaccineInformation, Long> {
}

