package com.test.estimator.repository;

import com.test.estimator.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findCompanyByCompanyName(@Param("name") String name);

}
