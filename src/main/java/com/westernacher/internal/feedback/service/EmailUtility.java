package com.westernacher.internal.feedback.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.text.MessageFormat;
import java.util.Arrays;

@Service
@Slf4j
@PropertySource("classpath:/message.properties")
public class EmailUtility {

    @Autowired
    private JavaMailSender sender;

    @Value("${spring.mail.username}")
    String from;

    @Autowired
    private ConfigUtility configUtil;

    public void send( String to,
                           String subjectKey,
                           String bodyKey,
                           String[] subjectParameters,
                           String[] bodyParameters) {
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        String subject = configUtil.getProperty(subjectKey);
        String body = configUtil.getProperty(bodyKey);

        if (subjectParameters != null) {
            subject = MessageFormat.format(configUtil.getProperty(subjectKey),
                    Arrays.copyOf(subjectParameters,subjectParameters.length, Object[].class));
        }

        if (bodyParameters != null){
            body = MessageFormat.format(configUtil.getProperty(bodyKey),
                    Arrays.copyOf(bodyParameters,bodyParameters.length, Object[].class));
        }
        log.info("from:"+from+"    To:"+to+"   subject:"+subject+"     Body:"+body);

        try {
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            sender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}

@Configuration
class ConfigUtility {

    @Autowired
    private Environment environment;

    public String getProperty(String pPropertyKey) {
        return environment.getProperty(pPropertyKey);
    }
}
