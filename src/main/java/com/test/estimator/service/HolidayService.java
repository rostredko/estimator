package com.test.estimator.service;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

@Service
@Transactional
public class HolidayService {

    private final static String API_KEY = "ba9d310a393ac3dcaefebb0fa74334c435db0c63";
    private final static String REMOTE_API_URL = "https://calendarific.com/api/v2/holidays";

    private final RestTemplate restTemplate;

    @Autowired
    public HolidayService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Calendar> pullHolidaysForCurrentYear(String year, String countryCode) {
        List<Calendar> holidays = new ArrayList<>();

        URIBuilder serviceURI = null;
        try {
            serviceURI = new URIBuilder(REMOTE_API_URL);
            serviceURI.addParameter("api_key", API_KEY);
            serviceURI.addParameter("country", countryCode);
            serviceURI.addParameter("year", year);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if(serviceURI == null) {
            return holidays;
        }

        ResponseEntity<String> response = restTemplate.getForEntity(serviceURI.toString(), String.class);

        JSONArray holidaysJsonArray = new JSONObject(response.getBody()).getJSONObject("response").getJSONArray("holidays");
        for (Object holidayObj : holidaysJsonArray) {
            JSONObject holidayJson = (JSONObject) holidayObj;
            JSONObject holidayDateJson = holidayJson.getJSONObject("date").getJSONObject("datetime");
            Calendar holidayDate = new GregorianCalendar(holidayDateJson.optInt("year"),
                    holidayDateJson.optInt("month") - 1, holidayDateJson.optInt("day"));
            holidays.add(holidayDate);
        }

        return holidays;
    }
}
