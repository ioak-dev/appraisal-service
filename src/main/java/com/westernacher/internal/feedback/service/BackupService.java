package com.westernacher.internal.feedback.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.westernacher.internal.feedback.controller.PersonController;
import com.westernacher.internal.feedback.domain.*;
import com.westernacher.internal.feedback.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.opencsv.CSVWriter;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.util.*;

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
    private GoalDefinitionRepository goalDefinitionRepository;

    @Autowired
    private RatingScaleRepository ratingScaleRepository;



    @Autowired
    private JavaMailSender sender;

    @Value("${spring.mail.username}")
    String from;

    @Value("${backup.mail.to}")
    String to;

    @Value("${spring.mail.host}")
    String host;

    @Value("${spring.mail.port}")
    String port;

    @Value("${spring.mail.password}")
    String password;

    @Scheduled(cron = "${backup.cron.expression}")
    public void sendAppraisalDatabase() {
        List<Person> personList = personRepository.findAll();
        List<Appraisal> appraisalList = appraisalRepository.findAll();
        List<AppraisalCycle> cycleList = cycleRepository.findAll();
        List<GoalDefinition> goalDefinitionList = goalDefinitionRepository.findAll();
        List<RatingScale> ratingScaleList = ratingScaleRepository.findAll();

        try {
            File personFile = File.createTempFile("person", ".json");
            File appraisalFile = File.createTempFile("appraisal", ".json");
            File cycleFile = File.createTempFile("cycle", ".json");
            File goalFile = File.createTempFile("goaldefination", ".json");
            File ratingScaleFile = File.createTempFile("ratingscale", ".josn");

            ObjectMapper mapper = new ObjectMapper();

            mapper.writeValue(personFile, personList);
            mapper.writeValue(appraisalFile, appraisalList);
            mapper.writeValue(cycleFile, cycleList);
            mapper.writeValue(goalFile, goalDefinitionList);
            mapper.writeValue(ratingScaleFile, ratingScaleList);

            send(to, personFile, appraisalFile, cycleFile, goalFile, ratingScaleFile);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

   /* @Async
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
    }*/

    @Async
    public void send( String to,
                      File personFile, File appraisalFile, File cycleFile, File goalFile, File ratingScaleFile) {
        Properties props = new Properties();
        props.put("mail.smtp.host", this.host);
        props.put("mail.smtp.port", this.port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable",true);
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.socketFactory.port", this.port);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(from, password);
                    }
                });

        try{
            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(this.from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("Appraisal backup : "+new Date().toString(), "UTF-8");
            message.setText("Please find the attachment back up for Appraisal Application", "UTF-8");

            MimeBodyPart attachment = new MimeBodyPart();
            attachment.attachFile(personFile);

            MimeBodyPart attachment2 = new MimeBodyPart();
            attachment2.attachFile(appraisalFile);

            MimeBodyPart attachment3 = new MimeBodyPart();
            attachment3.attachFile(cycleFile);

            MimeBodyPart attachment4 = new MimeBodyPart();
            attachment4.attachFile(goalFile);

            MimeBodyPart attachment5 = new MimeBodyPart();
            attachment5.attachFile(ratingScaleFile);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(attachment);
            multipart.addBodyPart(attachment2);
            multipart.addBodyPart(attachment3);
            multipart.addBodyPart(attachment4);
            multipart.addBodyPart(attachment5);

            message.setContent(multipart);

            Transport.send(message);
            log.info("Mail send successfully to :"+to);
        }catch(MessagingException e){
            log.info("Sending From: " + this.from + " Sending To: " + to);
            log.error("Error occured during sending mail"+e);
            e.printStackTrace();
        }catch (IOException i) {
            log.info("Sending From: " + this.from + " Sending To: " + to);
            log.error("Error occured during sending mail"+i);
            i.printStackTrace();
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
