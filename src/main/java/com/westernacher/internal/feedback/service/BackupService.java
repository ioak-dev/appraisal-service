package com.westernacher.internal.feedback.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.westernacher.internal.feedback.controller.PersonController;
import com.westernacher.internal.feedback.domain.Appraisal;
import com.westernacher.internal.feedback.domain.AppraisalCycle;
import com.westernacher.internal.feedback.domain.AppraisalStatusType;
import com.westernacher.internal.feedback.domain.Person;
import com.westernacher.internal.feedback.repository.AppraisalCycleRepository;
import com.westernacher.internal.feedback.repository.AppraisalRepository;
import com.westernacher.internal.feedback.repository.PersonRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class BackupService {

    @Autowired
    private AppraisalRepository appraisalRepository;

    @Autowired
    private AppraisalCycleRepository cycleRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private JavaMailSender sender;

    @Value("${spring.mail.username}")
    String from;

    @Value("${backup.mail.to}")
    String to;

    @Scheduled(cron = "${backup.cron.expression}")
    public void sendAppraisalDatabase() {
        List<Person> personList = personRepository.findAll();
        List<Appraisal> appraisalList = appraisalRepository.findAll();
        List<AppraisalCycle> cycleList = cycleRepository.findAll();

        try {
            File personFile = File.createTempFile("person", ".json");
            File appraisalFile = File.createTempFile("appraisal", ".json");
            File cycleFile = File.createTempFile("cycle", ".json");

            ObjectMapper mapper = new ObjectMapper();

            mapper.writeValue(personFile, personList);
            mapper.writeValue(appraisalFile, appraisalList);
            mapper.writeValue(cycleFile, cycleList);

            send(to, personFile, appraisalFile, cycleFile);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async
    public void send( String to,
                      File personFile, File appraisalFile, File cycleFile) {
        try {
            MimeMessage mimeMessage = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("Appraisal backup : "+new Date().toString());
            helper.setText("Please find the attachment back up for Appraisal Application");
            helper.addAttachment("person.json",personFile);
            helper.addAttachment("appraisal.json",appraisalFile);
            helper.addAttachment("cycle.json",cycleFile);
            sender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public static void writeDataToCsvUsingStringArray(PrintWriter writer, List<PersonController.PersonResource> persons) {
        String[] CSV_HEADER = { "Name", "Id", "Email", "Status" };
        try (
                CSVWriter csvWriter = new CSVWriter(writer,
                        CSVWriter.DEFAULT_SEPARATOR,
                        CSVWriter.NO_QUOTE_CHARACTER,
                        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END);
        ){
            csvWriter.writeNext(CSV_HEADER);

            for (PersonController.PersonResource personResource : persons) {
                String[] data = {
                        personResource.getEmployeeName(),
                        personResource.getEmployeeId(),
                        personResource.getEmployeeEmail(),
                        personResource.getEmployeeStatus()
                };

                csvWriter.writeNext(data);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
