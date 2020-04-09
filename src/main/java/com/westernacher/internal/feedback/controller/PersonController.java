
package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.*;
import com.westernacher.internal.feedback.repository.AppraisalRepository;
import com.westernacher.internal.feedback.repository.PersonRepository;
import com.westernacher.internal.feedback.service.BackupService;
import com.westernacher.internal.feedback.service.PersonService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/person")
@Slf4j
public class PersonController {

    @Autowired
    private PersonRepository repository;

    @Autowired
    private PersonService service;

    @Autowired
    private AppraisalRepository appraisalRepository;

    @Autowired
    private BackupService backupService;

    @RequestMapping(method = RequestMethod.GET)
    public List<Person> getAll () {
        return repository.findAll();
    }

    @RequestMapping(value = "/unit/{unit}", method = RequestMethod.GET)
    public List<Person> getByUnit (@PathVariable("unit") String unit) {
        return repository.findAllByUnit(unit);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Person get (@PathVariable("id") String id) {
        return repository.findById(id).orElse(null);
    }

    @RequestMapping(value = "/email/{email}", method = RequestMethod.GET)
    public Person getPersonByEmail (@PathVariable("email") String email) {
        return repository.findPersonByEmail(email.toLowerCase());
    }

    @RequestMapping(method = RequestMethod.PUT)
    public Person update (@Valid @RequestBody Person person) {
        return repository.save(person);
    }

    @RequestMapping(method = RequestMethod.POST)
    public void create (@Valid @RequestBody List<Person> persons) {
        persons.forEach(person -> {
            Person existingPerson = repository.findPersonByEmail(person.getEmail());
            if (existingPerson != null) {
                person.setId(existingPerson.getId());
                person.setRoles(existingPerson.getRoles());
            }
            person.setEmail(person.getEmail().toLowerCase());
            repository.save(person);
        });
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete (@PathVariable("id") String id) {
        repository.deleteById(id);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public void delete () {
        repository.deleteAll();
    }

    @RequestMapping(value = "/{id}/updateRoles", method = RequestMethod.PUT)
    public void updateRoles(@PathVariable("id") String id,
                                @Valid @RequestBody List<Role> roleList) {
        service.updateRoles(id, roleList);
    }

    @PostMapping(value = "/upload/role", consumes = "multipart/form-data")
    public void uploadMultipart(@RequestParam("file") MultipartFile file) {
        Map<String, Map<RoleType, List<String>>> csvContent = getCSVMap(file);


        List<Person> persons = repository.findAll();
        persons.stream().forEach(person -> {
            service.removeRoles(person.getId());
        });
        csvContent.forEach((k,v) ->{
            Map<RoleType, List<String>> roleMap = v;
            List<Role> roles = new ArrayList<>();
            roleMap.forEach((kk,vv) -> {
                Role role = new Role();
                role.setType(kk);
                role.setOptions(vv);
                roles.add(role);
            });
            Person person = repository.findPersonByEmail(k.trim().toLowerCase());

            if (person==null) {
                log.info(k);
            }else{
                service.updateRoles(person.getId(), roles);
            }

        });


    }


    private Map<String, Map<RoleType, List<String>>> getCSVMap(MultipartFile file) {

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

        Map<String, Map<RoleType, List<String>>> csvMap = new HashMap<>();

        for(String line:csvline) {
            String[] values = line.split(",");

            if (csvMap.containsKey(values[0].trim().toLowerCase())) {
                Map<RoleType, List<String>> secondValue = csvMap.get(values[0].trim().toLowerCase());
                if (secondValue.containsKey(RoleType.valueOf(values[1].trim()))) {
                    List<String> innerList = secondValue.get(RoleType.valueOf(values[1].trim()));
                    innerList.add(values[2].trim().toLowerCase());
                    secondValue.put(RoleType.valueOf(values[1].trim()), innerList);
                } else {
                    List<String> innerList2 = new ArrayList<>();
                    if (values.length==3) {
                        innerList2.add(values[2].trim().toLowerCase());
                    }
                    secondValue.put(RoleType.valueOf(values[1].trim()), innerList2);
                }
                csvMap.put(values[0].trim().toLowerCase(), secondValue);
            }else{
                Map<RoleType, List<String>> secondMap2 = new HashMap<>();
                List<String> innerList2 = new ArrayList<>();
                if (values.length==3) {
                    innerList2.add(values[2].trim().toLowerCase());
                }
                secondMap2.put(RoleType.valueOf(values[1].trim()), innerList2);

                csvMap.put(values[0].trim().toLowerCase(), secondMap2);
            }
        }
        return csvMap;

    }

    @PostMapping(value = "/upload/person", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadPersonFile(@ModelAttribute("file") MultipartFile file) {

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
    return null;
    }

    @GetMapping("/download/employeestatus/{cycleId}")
    public void downloadCSV(HttpServletResponse response, @PathVariable String cycleId) throws IOException{

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=PersonStatus.csv");

        List<Person> personList = repository.findAll();
        Map<String, Person> personMap = new HashMap<>();
        personList.stream().forEach(person -> {
            personMap.put(person.getId(), person);
        });

        List<Appraisal> appraisalList = appraisalRepository.findAllByCycleId(cycleId);
        List<PersonResource> personResources = new ArrayList<>();
        appraisalList.stream().forEach(appraisal -> {
            PersonResource resource = new PersonResource();
            resource.setEmployeeId(personMap.get(appraisal.getUserId()).getEmpId());
            resource.setEmployeeName(personMap.get(appraisal.getUserId()).getName());
            resource.setEmployeeEmail(personMap.get(appraisal.getUserId()).getEmail());
            resource.setEmployeeStatus(appraisal.getStatus().name());
            personResources.add(resource);
        } );

        backupService.writeDataToCsvUsingStringArray(response.getWriter(), personResources);
    }

    @Data
    public static class PersonResource {

        private String employeeId;
        private String employeeName;
        private String employeeEmail;
        private String employeeStatus;
    }

}
