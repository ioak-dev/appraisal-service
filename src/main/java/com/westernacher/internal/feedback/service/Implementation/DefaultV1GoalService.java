package com.westernacher.internal.feedback.service.Implementation;


import com.westernacher.internal.feedback.domain.v1Goal;
import com.westernacher.internal.feedback.repository.v1GoalRepository;
import com.westernacher.internal.feedback.service.v1GoalService;
import com.westernacher.internal.feedback.util.CSVService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class DefaultV1GoalService implements v1GoalService {

    @Autowired
    private v1GoalRepository repository;

    @Autowired
    private CSVService csvService;


    @Override
    public List<v1Goal> uploadGoalCsvFile(MultipartFile file) {

        try {
            List<String[]> csvRows = csvService.readCSVRows(file);

            Set<String> cuList = new HashSet<>();
            Set<String> jobList = new HashSet<>();

            for (String[] columns : csvRows) {
                cuList.add(columns[4]);
                jobList.add(columns[5]);
            }

            repository.deleteAllByCuIn(cuList);
            repository.deleteAllByJobIn(jobList);

            List<v1Goal> v1Goals = new ArrayList<>();
            int rowNumber = 0;
            for (String[] columns : csvRows) {
                rowNumber = rowNumber + 1;
                try{
                    v1Goal v1Goal = new v1Goal();
                    v1Goal.setGroup(new String(columns[0].getBytes(), "UTF-8"));
                    v1Goal.setCriteria(new String(columns[1].getBytes(), "UTF-8"));
                    v1Goal.setDescription(new String(columns[2].getBytes(), "UTF-8"));
                    v1Goal.setWeightage(Float.parseFloat(columns[3]));
                    v1Goal.setCu(columns[4]);
                    v1Goal.setJob(columns[5]);
                    v1Goal.setOrder(Integer.parseInt(columns[6]));
                    v1Goals.add(v1Goal);
                }catch(Exception e) {
                    log.info("Please correct the row number: "+rowNumber);
                    e.printStackTrace();
                }
            }
            return repository.saveAll(v1Goals);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
