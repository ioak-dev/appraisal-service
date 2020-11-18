package com.westernacher.internal.feedback.service.Implementation;


import com.westernacher.internal.feedback.domain.AppraisalStatusType;
import com.westernacher.internal.feedback.domain.Person;
import com.westernacher.internal.feedback.domain.Role;
import com.westernacher.internal.feedback.domain.RoleType;
import com.westernacher.internal.feedback.repository.PersonRepository;
import com.westernacher.internal.feedback.repository.RoleRepository;
import com.westernacher.internal.feedback.service.RoleService;
import com.westernacher.internal.feedback.util.CSVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private CSVService csvService;

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
            existingRole.setReviewerType(role.getReviewerType());
            existingRole.setEmployeeId(role.getEmployeeId());
            return repository.save(existingRole);
        }
        return repository.save(role);
    }

    public void uploadCsvFile(MultipartFile file) {
        updateAll(getRoles(csvService.readCSVRows(file)));
    }

    private List<Role> getRoles(List<String[]> rows) {

        Map<String, String> personMap = new HashMap<>();

        List<Person> personList = personRepository.findAll();

        personList.stream().forEach(person -> {
            personMap.put(person.getEmail().toLowerCase(), person.getId());
        });

        List<Role> roles =  new ArrayList<>();

        for(String[] line : rows) {
            if (line[3].trim().toLowerCase().equals("add")) {
                Role db = repository.findByEmployeeIdAndReviewerIdAndReviewerType(personMap.get(line[2].trim().toLowerCase()),
                        personMap.get(line[0].trim().toLowerCase()), AppraisalStatusType.valueOf(line[1].trim()));
                if (db == null) {
                    Role role = new Role();
                    role.setReviewerId(personMap.get(line[0].trim().toLowerCase()));
                    role.setReviewerType(line[1].trim());
                    role.setEmployeeId(personMap.get(line[2].trim().toLowerCase()));
                    roles.add(repository.save(role));
                }
            }else if (line[3].trim().toLowerCase().equals("remove")) {
                repository.deleteByEmployeeIdAndReviewerIdAndReviewerType(personMap.get(line[2].trim().toLowerCase()),
                        personMap.get(line[0].trim().toLowerCase()), AppraisalStatusType.valueOf(line[1].trim()));
            }

        }
        return roles;

    }

}
