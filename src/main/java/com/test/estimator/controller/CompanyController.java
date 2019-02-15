package com.test.estimator.controller;

import com.test.estimator.controller.dto.TasksEstimationDto;
import com.test.estimator.domain.Company;
import com.test.estimator.service.CompanyService;
import com.test.estimator.service.DeveloperService;
import com.test.estimator.service.EstimationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/companies")
public class CompanyController {

    private final EstimationService estimationService;
    private final CompanyService companyService;
    private final DeveloperService developerService;

    @Autowired
    public CompanyController(EstimationService estimationService,
                             CompanyService companyService,
                             DeveloperService developerService) {
        this.estimationService = estimationService;
        this.companyService = companyService;
        this.developerService = developerService;
    }

    @PostMapping(path = "/prediction")
    public ResponseEntity<String> getPrediction(@RequestBody TasksEstimationDto estimationDto) {
        String date = estimationService.predictTheEndDateOfTheProject(estimationDto.getBackendTasksEstimation(),
                estimationDto.getFrontendTasksEstimation());
        return new ResponseEntity<>(date, HttpStatus.OK);
    }

    @DeleteMapping
    public void deleteAllEntries() {
        developerService.deleteAllDevelopers();
        companyService.deleteAllCompanies();
    }
}
