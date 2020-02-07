
package com.westernacher.internal.feedback.controller;

import com.westernacher.internal.feedback.domain.Person;
import com.westernacher.internal.feedback.domain.Role;
import com.westernacher.internal.feedback.domain.RoleType;
import com.westernacher.internal.feedback.repository.PersonRepository;
import com.westernacher.internal.feedback.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/person")
public class PersonController {

    @Autowired
    private PersonRepository repository;

    @Autowired
    private PersonService service;

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

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
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

            Person person = repository.findPersonByEmail(k);
            service.updateRoles(person.getId(), roles);
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

            if (csvMap.containsKey(values[0])) {
                Map<RoleType, List<String>> secondValue = csvMap.get(values[0]);
                if (secondValue.containsKey(RoleType.valueOf(values[1]))) {
                    List<String> innerList = secondValue.get(RoleType.valueOf(values[1]));
                    innerList.add(values[2]);
                    secondValue.put(RoleType.valueOf(values[1]), innerList);
                } else {
                    List<String> innerList2 = new ArrayList<>();
                    if (values.length==3) {
                        innerList2.add(values[2]);
                    }
                    secondValue.put(RoleType.valueOf(values[1]), innerList2);
                }
                csvMap.put(values[0], secondValue);
            }else{
                Map<RoleType, List<String>> secondMap2 = new HashMap<>();
                List<String> innerList2 = new ArrayList<>();
                if (values.length==3) {
                    innerList2.add(values[2]);
                }
                secondMap2.put(RoleType.valueOf(values[1]), innerList2);

                csvMap.put(values[0], secondMap2);
            }
        }
        return csvMap;

    }

}
