package com.westernacher.internal.feedback.controller;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;

import com.westernacher.internal.feedback.domain.Appraisal;
import com.westernacher.internal.feedback.domain.Person;
import com.westernacher.internal.feedback.repository.AppraisalRepository;
import com.westernacher.internal.feedback.repository.PersonRepository;
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

    @Value("${spring.mail.username}")
    String from;

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public String send(@Valid @RequestBody MailResource resource) {
        try {
          send(resource.getTo(), resource.getSubject(), resource.getBody());
            return "Email Sent!";
        }catch(Exception ex) {
            return "Error in sending email: "+ex;
        }
    }

    @RequestMapping(value = "/{cycleId}/send", method = RequestMethod.POST)
    public String sendByCycleId(@PathVariable("cycleId") String cycleId,
                                @Valid @RequestBody MailResource resource) {
        try {
            List<Appraisal> appraisals = appraisalRepository.findAllByCycleId(cycleId);

            List<String> emailIdList = new ArrayList<>();
            appraisals.stream().forEach(appraisal -> {
                Person person = personRepository.findById(appraisal.getUserId()).orElse(null);
                emailIdList.add(person.getEmail());
            });

            send(emailIdList, resource.getSubject(), resource.getBody());

            return "Email Sent!";
        }catch(Exception ex) {
            return "Error in sending email : "+ex;
        }
    }

    private void send(List<String> toList, String subject, String body){
        try{
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setFrom(from);
            helper.setBcc(InternetAddress.parse(StringUtils.join(toList, ',')));
            helper.setTo(InternetAddress.parse(StringUtils.join(toList, ',')));
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
            log.info("Error in sending email");
        }
    }

    @Data
    public static class MailResource {

        private String to;
        private String subject;
        private String body;

    }

}


