package com.westernacher.internal.feedback.service.Implementation;


import com.westernacher.internal.feedback.domain.Person;
import com.westernacher.internal.feedback.domain.Role;
import com.westernacher.internal.feedback.domain.RoleType;
import com.westernacher.internal.feedback.repository.PersonRepository;
import com.westernacher.internal.feedback.repository.RoleRepository;
import com.westernacher.internal.feedback.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DefaultRoleService implements RoleService {

    @Autowired
    private RoleRepository repository;

    @Autowired
    private PersonRepository personRepository;

    public List<Role> updateAll(List<Role> roles) {
        return repository.saveAll(roles);
    }

    public Role createAndUpdate (Role role) {
        Role existingRole = null;
        if (role.getId() != null) {
            existingRole = repository.findById(role.getId()).orElse(null);
        }

        if (existingRole !=null) {
            existingRole.setReviewerId(role.getReviewerId());
            existingRole.setRoleType(role.getRoleType());
            existingRole.setEmployeeId(role.getEmployeeId());
            return repository.save(existingRole);
        }
        return repository.save(role);
    }

    public void uploadCsvFile(MultipartFile file) {
        updateAll(getRoles(getRows(file)));
    }


    private List<String> getRows(MultipartFile file) {
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
        return csvline;
    }

    private List<Role> getRoles(List<String> rows) {

        Map<String, String> personMap = new HashMap<>();

        List<Person> personList = personRepository.findAll();

        personList.stream().forEach(person -> {
            personMap.put(person.getEmail().toLowerCase(), person.getId());
        });

        List<Role> roles =  new ArrayList<>();

        for(String line : rows) {
            String[] values = line.split(",");
            Role role = new Role();
            role.setReviewerId(personMap.get(values[0].trim().toLowerCase()));
            role.setRoleType(RoleType.valueOf(values[1].trim()));
            role.setEmployeeId(personMap.get(values[2].trim().toLowerCase()));
            roles.add(role);
        }
        return roles;

    }

}
