package com.westernacher.internal.feedback.service;

import com.westernacher.internal.feedback.domain.Role;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RoleService {
    Role createAndUpdate (Role role);

    List<Role> updateAll(List<Role> roles);

    void uploadCsvFile(MultipartFile file);

}
