package com.westernacher.internal.feedback.service.Implementation;

import com.westernacher.internal.feedback.domain.*;
import com.westernacher.internal.feedback.domain.v2.AppraisalHeader;
import com.westernacher.internal.feedback.domain.v2.AppraisalRole;
import com.westernacher.internal.feedback.domain.v2.Person;
import com.westernacher.internal.feedback.repository.*;
import com.westernacher.internal.feedback.repository.v2.AppraisalHeaderRepository;
import com.westernacher.internal.feedback.repository.v2.PersonRepository;
import com.westernacher.internal.feedback.service.AppraisalCycleService;
import com.westernacher.internal.feedback.service.v2.AppraisalHeaderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class DefaultAppraisalHeaderService implements AppraisalHeaderService {

    @Autowired
    private AppraisalHeaderRepository repository;

    @Override
    public List<AppraisalHeader> getHeaderByEmployeeId(String employeeId, String from, String to) {
        List<AppraisalHeader> response = new ArrayList<>();
        for (AppraisalHeader appraisalHeader:repository.findAllByEmployeeId(employeeId)) {
            if ((appraisalHeader.getFrom()< Integer.parseInt(from) && appraisalHeader.getTo()> Integer.parseInt(from)) ||
                    (appraisalHeader.getFrom()> Integer.parseInt(from) && appraisalHeader.getTo()<Integer.parseInt(to)) ||
                    (appraisalHeader.getFrom() < Integer.parseInt(to) && appraisalHeader.getTo() >Integer.parseInt(to))) {
                response.add(appraisalHeader);
            }
        }
        return response;
    }
}
