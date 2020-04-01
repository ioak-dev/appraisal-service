package com.westernacher.internal.feedback.controller;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;

import com.westernacher.internal.feedback.domain.Appraisal;
import com.westernacher.internal.feedback.domain.AppraisalStatusType;
import com.westernacher.internal.feedback.domain.Person;
import com.westernacher.internal.feedback.repository.AppraisalRepository;
import com.westernacher.internal.feedback.repository.PersonRepository;
import com.westernacher.internal.feedback.service.BackupService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@RestController
@RequestMapping("/notification")
@Slf4j
public class NotificationController {

    @Autowired
    private JavaMailSender sender;

    @Autowired
    private AppraisalRepository appraisalRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private BackupService backupService;

    @Value("${spring.mail.username}")
    String from;

    @Value("${spring.mail.host}")
    String host;

    @Value("${spring.mail.port}")
    String port;

    @Value("${spring.mail.password}")
    String password;

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public String send(@Valid @RequestBody MailResource resource) {
        try {
            sendMail(resource.getTo(), resource.getSubject(), resource.getBody());
            return "Email Sent!";
        }catch(Exception ex) {
            return "Error in sending email: "+ex;
        }
    }

    @RequestMapping(value = "/{cycleId}/send", method = RequestMethod.POST)
    public String sendByCycleId(@PathVariable("cycleId") String cycleId,
                                @Valid @RequestBody MailResource resource) {
        try {
            List<Appraisal> appraisals = appraisalRepository.findAllByCycleIdAndStatus(cycleId, AppraisalStatusType.SELF_REVIEW.name());

            List<String> emailIdList = new ArrayList<>();
            appraisals.stream().forEach(appraisal -> {
                Person person = personRepository.findById(appraisal.getUserId()).orElse(null);
                emailIdList.add(person.getEmail());
            });

            send(emailIdList, resource.getSubject(), resource.getBody());

            return "Email Sent!";
        }catch(Exception ex) {
            ex.printStackTrace();
            return "Error in sending email : "+ex;
        }
    }

    private void send(List<String> toList, String subject, String body){
        try{
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setFrom(from);
            helper.setBcc(InternetAddress.parse(StringUtils.join(toList, ',')));
            helper.setSubject(subject);
            helper.setText(body);
            sender.send(message);
        }catch(Exception e){
            log.info("Error in sending email");
        }
    }

    private void send(String to, String subject, String body){
        try{
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);

            sender.send(message);
        }catch(Exception e){
            e.printStackTrace();
            log.info("Error in sending email");
        }
    }

    private void sendMail(String to, String subject, String body) {
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
            message.setSubject(subject, "UTF-8");
            message.setText(body, "UTF-8");

            Transport.send(message);
            log.info("Mail send successfully to :"+to);
        }catch(MessagingException e){
            log.info("Sending From: " + this.from + " Sending To: " + to);
            log.error("Error occured during sending mail"+e);
        }
    }

    @RequestMapping(value = "/backup", method = RequestMethod.GET)
    public void backup () {
        backupService.sendAppraisalDatabase();
    }

    @Data
    public static class MailResource {

        private String to;
        private String subject;
        private String body;

    }

}


