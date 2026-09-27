package com.SpringBoot.Authentication.repo;

import com.SpringBoot.Authentication.entity.Patient;
import com.SpringBoot.Authentication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient,Long> {
}
