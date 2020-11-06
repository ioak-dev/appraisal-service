package com.westernacher.internal.feedback.util;

import com.opencsv.CSVReaderBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

@Component
public class CSVService {

    public List<String[]> readCSVRows(MultipartFile file) {

        try {
            Reader reader = new InputStreamReader(file.getInputStream(), "UTF-8");
            com.opencsv.CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build();
            return csvReader.readAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
