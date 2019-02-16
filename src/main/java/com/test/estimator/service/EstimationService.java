package com.test.estimator.service;

import com.test.estimator.domain.Company;
import com.test.estimator.domain.DeveloperType;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.test.estimator.domain.DeveloperType.BACKEND;
import static com.test.estimator.domain.DeveloperType.FRONTEND;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;


@Service
@Transactional
public class EstimationService {

    private final CompanyService companyService;
    private final HolidayService holidayService;

    public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final static SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("EEEE");
    private final static int WORKING_HOURS_PER_DAY = 8;
    private final static int WORKING_HOURS_PER_WEEK = 40;
    private String bestCompanyName;

    @Autowired
    public EstimationService(CompanyService companyService, HolidayService holidayService) {
        this.companyService = companyService;
        this.holidayService = holidayService;
    }

    public String predictTheEndDateOfTheProject(Integer backendEstimationInHours, Integer frontendEstimationInHours) {
        List<Company> companies = companyService.getAllCompanies();

        int bestResultInHours = getBestResultInHours(companies, backendEstimationInHours, frontendEstimationInHours);

        if (bestResultInHours == 0) {
            return "Sorry, but there is no company with enough developers to complete all the tasks";
        }

        double days = Math.ceil((double) bestResultInHours / WORKING_HOURS_PER_DAY);
        Date currentDate = new Date();
        Calendar theEndDateOfTheProject = createCalendarFromDate(currentDate);

        while (days > 0) {
            if (!isDayWeekend(theEndDateOfTheProject) && !isHoliday(theEndDateOfTheProject)) {
                days--;
            }
            theEndDateOfTheProject.add(Calendar.DATE, 1);
        }

        theEndDateOfTheProject.add(Calendar.SECOND, -1); //Calendar adds the whole day and set time to 00.00
                                                                 //which means the next day, so we need to go back for
                                                                 //1 second in a previous day
        bookCompany(theEndDateOfTheProject.getTime(), this.bestCompanyName);

        return DATE_FORMAT.format(theEndDateOfTheProject.getTime());
    }

    private boolean isHoliday(Calendar currentDate) {
        int year = currentDate.get(Calendar.YEAR);
        List<Calendar> holidays = holidayService.pullHolidaysForCurrentYear(String.valueOf(year), "UA");
        Optional<Date> holiday = holidays.stream()
                .map(Calendar::getTime)
                .filter(date -> date.compareTo(currentDate.getTime()) == 0)
                .findAny();
        return holiday.isPresent();
    }

    private boolean isDayWeekend(Calendar theEndDateOfTheProject) {
        return DAY_FORMAT.format(theEndDateOfTheProject.getTime()).equalsIgnoreCase(SATURDAY.name()) ||
                DAY_FORMAT.format(theEndDateOfTheProject.getTime()).equalsIgnoreCase(SUNDAY.name());
    }

    private int getBestResultInHours(List<Company> companies, Integer backendEstimationInHours,
                                     Integer frontendEstimationInHours) {
        int bestResultInHours = 0;
        String bestCompanyName = "";

        for (Company company : companies) {
            int numberOfFrontendDevelopers = getNumberOfDevelopersByType(company, FRONTEND);
            int numberOfBackendDevelopers = getNumberOfDevelopersByType(company, BACKEND);

            int backendInHoursForCompany = 0;
            int frontendInHoursForCompany = 0;
            if (numberOfBackendDevelopers != 0) {
                backendInHoursForCompany = backendEstimationInHours / numberOfBackendDevelopers;
            }
            if (numberOfFrontendDevelopers != 0) {
                frontendInHoursForCompany = frontendEstimationInHours / numberOfFrontendDevelopers;
            }

            int resultForCompanyInHours = 0;
            if (bestResultInHours == 0 || resultForCompanyInHours < bestResultInHours) {
                if (company.getBookedTillDate() != null) {
                    resultForCompanyInHours += addRemainingBusyHoursToResult(company, resultForCompanyInHours);
                }

                if (frontendEstimationInHours == 0 && backendInHoursForCompany != 0) {
                    bestResultInHours = backendInHoursForCompany + resultForCompanyInHours;
                    bestCompanyName = company.getCompanyName();
                }

                if (backendEstimationInHours == 0 && frontendInHoursForCompany != 0) {
                    bestResultInHours = frontendInHoursForCompany + resultForCompanyInHours;
                    bestCompanyName = company.getCompanyName();
                }

                if (frontendInHoursForCompany != 0 && backendInHoursForCompany != 0) {
                    resultForCompanyInHours += frontendInHoursForCompany + backendInHoursForCompany;
                    bestResultInHours = resultForCompanyInHours;
                    bestCompanyName = company.getCompanyName();
                }
            }
        }

        if (bestResultInHours != 0) {
            rememberBestCompanyName(bestCompanyName);
        }

        return bestResultInHours;
    }

    private void rememberBestCompanyName(String companyName) {
        this.bestCompanyName = companyName;
    }

    private void bookCompany(Date bookedTillDate, String bestCompanyName) {
        Optional<Company> bestCompany = companyService.findByName(bestCompanyName);
        bestCompany.ifPresent(c -> {
            c.setBookedTillDate(bookedTillDate);
            companyService.saveCompany(c);
        });
    }

    private int addRemainingBusyHoursToResult(Company company, int resultForCompanyInHours) {
        Calendar theEndDateOfTheProject = createCalendarFromDate(company.getBookedTillDate());
        DateTime startTime = DateTime.now();
        DateTime endTime = new DateTime(company.getBookedTillDate());
        LocalDate startDate = LocalDate.now();
        LocalDate beginningOfMonth = startDate.withDayOfMonth(1);
        LocalDate endOfMonth = startDate.plusMonths(1).withDayOfMonth(1).minusDays(1);

        Period periodBetweenTodayAndTheEndOfTheProjectDay = new Period(startTime, endTime);
        int numOfDaysInCurrentYear = theEndDateOfTheProject.getActualMaximum(Calendar.DAY_OF_YEAR);

        long numOfDaysInCurrentMonthWithoutWeekends = countDaysInCurrentMonthWithoutWeekends(beginningOfMonth, endOfMonth);
        int hoursInCurrentYear = periodBetweenTodayAndTheEndOfTheProjectDay.getYears() * numOfDaysInCurrentYear;
        int hoursInCurrentMonth = Math.toIntExact(periodBetweenTodayAndTheEndOfTheProjectDay.getMonths() *
                numOfDaysInCurrentMonthWithoutWeekends * WORKING_HOURS_PER_DAY);
        int hoursInWeek = periodBetweenTodayAndTheEndOfTheProjectDay.getWeeks() * WORKING_HOURS_PER_WEEK;
        int hoursInDay = periodBetweenTodayAndTheEndOfTheProjectDay.getDays() * WORKING_HOURS_PER_DAY;
        int hoursToFinishPreviousProject = Math.toIntExact(hoursInCurrentYear + hoursInCurrentMonth + hoursInWeek +
                 + hoursInDay + periodBetweenTodayAndTheEndOfTheProjectDay.getHours());
        resultForCompanyInHours += hoursToFinishPreviousProject;

        return resultForCompanyInHours;
    }

    private long countDaysInCurrentMonthWithoutWeekends(LocalDate beginningOfMonth, LocalDate endOfMonth) {
        return Stream.iterate(beginningOfMonth, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(beginningOfMonth, endOfMonth))
                .filter(date -> date.getDayOfWeek() != SATURDAY && date.getDayOfWeek() != SUNDAY)
                .count();
    }

    private Calendar createCalendarFromDate(Date bookedTillDate) {
        Calendar theEndDateOfTheProject = Calendar.getInstance();
        try {
            theEndDateOfTheProject.setTime(DATE_FORMAT.parse(DATE_FORMAT.format(bookedTillDate)));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return theEndDateOfTheProject;
    }

    private int getNumberOfDevelopersByType(Company company, DeveloperType developerType) {
        return Math.toIntExact(company.getDevelopers().stream()
                .filter(developer -> developerType.equals(developer.getDeveloperType()))
                .count());
    }
}
