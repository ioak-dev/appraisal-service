package com.westernacher.internal.feedback.service.Implementation;

import com.westernacher.internal.feedback.domain.Person;
import com.westernacher.internal.feedback.domain.PersonStatus;
import com.westernacher.internal.feedback.repository.PersonRepository;
import com.westernacher.internal.feedback.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

@Service
public class DefaultPersonService implements PersonService {

    @Autowired
    private PersonRepository repository;

    @Override
    public Person createAndUpdate (Person person) {
        Person existingPerson = repository.findPersonByEmail(person.getEmail());
        if(existingPerson != null) {
            existingPerson.setEmpId(person.getEmpId());
            existingPerson.setName(person.getName());
            existingPerson.setJobName(person.getJobName() != null ? person.getJobName():null);
            existingPerson.setUnit(person.getUnit() != null?person.getUnit():null);
            existingPerson.setJoiningDate(person.getJoiningDate());
            existingPerson.setLevel(person.getLevel() != null ? person.getLevel():null);
            existingPerson.setSpecialization(person.getSpecialization() != null ? person.getSpecialization():null);
            existingPerson.setLastAppraisalDate(person.getLastAppraisalDate());
            existingPerson.setDuration(person.getDuration());
            existingPerson.setStatus(person.getStatus());
            return repository.save(existingPerson);
        }
        return repository.save(person);
    }

    @Override
    public void uploadPersonFile(MultipartFile file) {
        repository.deleteAll();
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (fileName.endsWith(".csv")) {
            BufferedReader br;
            List<String> csvline = new ArrayList<>();
            try {
                String line;
                InputStream is = file.getInputStream();
                br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    csvline.add(line);
                }
                csvline.remove(0);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
            csvline.stream().forEach(line -> {
                String[] values = line.split(",");
                Person person = new Person();
                person.setEmpId(values[0].trim());
                person.setName(values[1].trim());
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                try{
                    person.setJoiningDate(format.parse(values[2].trim()));
                }catch (ParseException e) {

                }

                person.setJobName(values[3].trim());
                person.setUnit(values[4].trim());
                person.setLevel(values[5].trim());
                person.setSpecialization(values[6].trim());
                person.setStatus(PersonStatus.valueOf(values[7].trim()));
                person.setEmail(values[8].trim().toLowerCase());
                try{
                    person.setLastAppraisalDate(format.parse(values[9].trim()));
                }catch(ParseException e){}

                person.setDuration(Integer.parseInt(values[10].trim()));

                repository.save(person);
            });
        }
    }
}
