package com.westernacher.internal.feedback.service.Implementation;


import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.westernacher.internal.feedback.domain.Goal;
import com.westernacher.internal.feedback.repository.GoalRepository;
import com.westernacher.internal.feedback.service.GoalService;
import com.westernacher.internal.feedback.util.CSVService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DefaultGoalService implements GoalService {

    @Autowired
    private GoalRepository repository;

    @Autowired
    private CSVService csvService;


    @Override
    public List<Goal> uploadGoalCsvFile(MultipartFile file) {

        try {
            List<String[]> csvRows = csvService.readCSVRows(file);
            List<Goal> goals = new ArrayList<>();
            int rowNumber = 0;
            for (String[] columns : csvRows) {
                rowNumber = rowNumber + 1;
                try{
                    Goal goal = new Goal();
                    goal.setGroup(new String(columns[0].getBytes(), "UTF-8"));
                    goal.setCriteria(new String(columns[1].getBytes(), "UTF-8"));
                    goal.setDescription(new String(columns[2].getBytes(), "UTF-8"));
                    goal.setWeightage(Float.parseFloat(columns[3]));
                    goal.setJobName(columns[4]);
                    goal.setOrder(Integer.parseInt(columns[5]));
                    goals.add(goal);
                }catch(Exception e) {
                    log.info("Please correct the row number: "+rowNumber);
                    e.printStackTrace();
                }
            }
            return repository.saveAll(goals);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
