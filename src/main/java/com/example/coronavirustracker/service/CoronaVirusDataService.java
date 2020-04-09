package com.example.coronavirustracker.service;

import com.example.coronavirustracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoronaVirusDataService {

    private static final String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    private List<LocationStats> allStats = new ArrayList<>();

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    @PostConstruct
//  (cron = second,minut,hours,day,month,year)
    @Scheduled(cron = "* * 1 * * *")    // pobezi kazdou hodinu ... v main se musi dat @EnableScheduling
//    1)
//    @PostConstruct - v momente co se spusti trida CoronaVirusDataService
//                     se vykona tato metoda
    public void fetchVirusData() throws IOException, InterruptedException {
        List<LocationStats> newStats = new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DATA_URL))
                .build();
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
//        System.out.println(httpResponse.body());

//        2)
//         Parse CSV file from data service
//         using CSV library maven from http://commons.apache.org/proper/commons-csv/
//         using Header auto detection from http://commons.apache.org/proper/commons-csv/user-guide.html
        StringReader csvBodyReader = new StringReader(httpResponse.body());
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(csvBodyReader);
        for (CSVRecord record : records) {
//            String state = record.get("Province/State");
//            System.out.println(state);
//            dale si ulozim tyto informace do data modelu
            LocationStats locationStats = new LocationStats();
            locationStats.setState(record.get("Province/State"));
            locationStats.setCountry(record.get("Country/Region"));
            locationStats.setLatestTotalCases(Integer.parseInt(record.get(record.size() - 1)));
//            System.out.println(locationStats);

//            7) create delta
            int latestCases = Integer.parseInt(record.get(record.size() - 1));
            int prevDayCases = Integer.parseInt(record.get(record.size() - 2));
            locationStats.setLatestTotalCases(latestCases);
            locationStats.setDiffFromPreviousDay(latestCases-prevDayCases);
            newStats.add(locationStats);
        }
        this.allStats = newStats;
//        next need to create Controller and render in to html UI (user interface)
//        that point to resource/template/home.html
    }
}