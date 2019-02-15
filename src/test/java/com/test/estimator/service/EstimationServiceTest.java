package com.test.estimator.service;

import com.test.estimator.domain.Company;
import com.test.estimator.domain.Developer;
import com.test.estimator.domain.DeveloperType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.text.ParseException;
import java.util.*;

import static com.test.estimator.service.EstimationService.DATE_FORMAT;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EstimationServiceTest {

    @InjectMocks
    private EstimationService estimationService;

    @Mock
    private CompanyService companyService;

    @Mock
    private HolidayService holidayService;

    @Test
    public void shouldThrowAnErrorInCaseOfLackDevelopers() {
        //given
        Integer backendEstimationHours = 1;
        Integer frontendEstimationHours = 0;
        when(companyService.getAllCompanies()).thenReturn(new ArrayList<>());

        //when
        String result = estimationService.predictTheEndDateOfTheProject(backendEstimationHours, frontendEstimationHours);

        //then
        assertEquals(result, "Sorry, but there is no company with enough developers to complete all the tasks");
    }

    @Test
    public void shouldPredictDateOnlyForBackendTasks() throws ParseException {
        //given
        Integer backendEstimationHours = 50;
        Integer frontendEstimationHours = 0;
        Developer developer = new Developer();
        developer.setDeveloperName("Rost");
        developer.setDeveloperType(DeveloperType.BACKEND);
        Company company = new Company();
        company.setCompanyName("Philips");
        company.setDevelopers(Collections.singletonList(developer));
        developer.setCompany(company);
        List<Company> companies = Collections.singletonList(company);
        List<Calendar> holidays = new ArrayList<>();
        when(companyService.getAllCompanies()).thenReturn(companies);
        when(holidayService.pullHolidaysForCurrentYear("2019", "UA")).thenReturn(holidays);

        //when
        String result = estimationService.predictTheEndDateOfTheProject(backendEstimationHours, frontendEstimationHours);

        //then
        String date = "2019-02-15";
        Date currentDate = DATE_FORMAT.parse(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DATE, 6); //50 hours for 1 developer = 50/8=6 days
        currentDate = calendar.getTime();
        assertEquals(result, DATE_FORMAT.format(currentDate));
    }

    @Test
    public void shouldPredictDateOnlyForFrontendTasks() throws ParseException {
        //given
        Integer backendEstimationHours = 0;
        Integer frontendEstimationHours = 100;
        Developer developer = new Developer();
        developer.setDeveloperName("Rost");
        developer.setDeveloperType(DeveloperType.FRONTEND);
        Company company = new Company();
        company.setCompanyName("Philips");
        company.setDevelopers(Collections.singletonList(developer));
        developer.setCompany(company);
        List<Company> companies = Collections.singletonList(company);
        List<Calendar> holidays = new ArrayList<>();
        when(companyService.getAllCompanies()).thenReturn(companies);
        when(holidayService.pullHolidaysForCurrentYear("2019", "UA")).thenReturn(holidays);

        //when
        String result = estimationService.predictTheEndDateOfTheProject(backendEstimationHours, frontendEstimationHours);

        //then
        String date = "2019-02-15";
        Date currentDate = DATE_FORMAT.parse(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DATE, 12); //12 days with holidays and weekends
        currentDate = calendar.getTime();
        assertEquals(result, DATE_FORMAT.format(currentDate));
    }

    @Test
    public void shouldPredictDateForBothFrontendAndBackendTasks() throws ParseException {
        //given
        Integer backendEstimationHours = 50;
        Integer frontendEstimationHours = 100;
        Developer developerOne = new Developer();
        developerOne.setDeveloperName("Rost");
        developerOne.setDeveloperType(DeveloperType.FRONTEND);
        Developer developerTwo = new Developer();
        developerTwo.setDeveloperName("Phil");
        developerTwo.setDeveloperType(DeveloperType.BACKEND);
        Company company = new Company();
        company.setCompanyName("Philips");
        company.setDevelopers(Arrays.asList(developerOne, developerTwo));
        developerOne.setCompany(company);
        List<Company> companies = Collections.singletonList(company);
        List<Calendar> holidays = new ArrayList<>();
        when(companyService.getAllCompanies()).thenReturn(companies);
        when(holidayService.pullHolidaysForCurrentYear("2019", "UA")).thenReturn(holidays);

        //when
        String result = estimationService.predictTheEndDateOfTheProject(backendEstimationHours, frontendEstimationHours);

        //then
        String date = "2019-02-15";
        Date currentDate = DATE_FORMAT.parse(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DATE, 18); //18 days
        currentDate = calendar.getTime();
        assertEquals(result, DATE_FORMAT.format(currentDate));
    }

    @Test
    public void shouldPredictDateForBothFrontendAndBackendTasksWithBookedCompany() throws ParseException {
        //given
        Integer backendEstimationHours = 50;
        Integer frontendEstimationHours = 150;
        Developer developerOne = new Developer();
        developerOne.setDeveloperName("Rost");
        developerOne.setDeveloperType(DeveloperType.FRONTEND);
        Developer developerTwo = new Developer();
        developerTwo.setDeveloperName("Phil");
        developerTwo.setDeveloperType(DeveloperType.BACKEND);
        Company company = new Company();
        company.setCompanyName("Philips");
        company.setDevelopers(Arrays.asList(developerOne, developerTwo));
        developerOne.setCompany(company);
        List<Company> companies = Collections.singletonList(company);
        List<Calendar> holidays = new ArrayList<>();
        String date = "2019-02-15";
        Date holidayDate = DATE_FORMAT.parse(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(holidayDate);
        holidays.add(calendar);
        when(companyService.getAllCompanies()).thenReturn(companies);
        when(companyService.findByName(anyString())).thenReturn(Optional.of(company));
        when(companyService.saveCompany(eq(company))).thenReturn(company);
        when(holidayService.pullHolidaysForCurrentYear("2019", "UA")).thenReturn(holidays);

        //when
        estimationService.predictTheEndDateOfTheProject(backendEstimationHours, frontendEstimationHours);
        String result = estimationService.predictTheEndDateOfTheProject(backendEstimationHours, frontendEstimationHours);

        //then
        Date currentDate = DATE_FORMAT.parse(date);
        calendar.setTime(currentDate);
        calendar.add(Calendar.DATE, 44); // 44 days with holidays and weekends
        currentDate = calendar.getTime();
        assertEquals(result, DATE_FORMAT.format(currentDate));
    }
}