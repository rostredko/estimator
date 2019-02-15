package com.test.estimator.service;

import com.test.estimator.domain.Developer;
import com.test.estimator.repository.DeveloperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class DeveloperService {

    private DeveloperRepository developerRepository;

    @Autowired
    DeveloperService(DeveloperRepository developerRepository) {
        this.developerRepository = developerRepository;
    }

    public void deleteAllDevelopers() {
        developerRepository.deleteAll();
    }

    public Developer save(Developer developer) {
        return developerRepository.save(developer);
    }
}
