package com.test.estimator.service;

import com.test.estimator.domain.Company;
import com.test.estimator.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Optional<Company> findByName(String companyName) {
        return companyRepository.findCompanyByCompanyName(companyName);
    }

    public Company saveCompany(Company company) {
        return companyRepository.save(company);
    }

    public void deleteAllCompanies() {
        companyRepository.deleteAll();
    }
}
