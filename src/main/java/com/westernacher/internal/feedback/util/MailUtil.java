package com.westernacher.internal.feedback.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

@Service
@Slf4j
public class MailUtil {

    @Value("${spring.mail.username}")
    String  from;

    @Value("${spring.mail.host}")
    String host;

    @Value("${spring.mail.port}")
    String port;

    @Value("${spring.mail.password}")
    String password;

    public boolean send( String to, String bodyTemplate, Map<String, String> bodyValues,
                      String subjectTemplate, Map<String, String> subjectValues) {
        Session session = getPropertyAndSessionDetails();
        try{
            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(this.from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(getHtmlByTemplateAndContext(subjectTemplate, subjectValues), "UTF-8");
            message.setText(getHtmlByTemplateAndContext(bodyTemplate, bodyValues), "UTF-8");

            Transport.send(message);
            log.info("Mail send successfully to :"+to);
            return true;
        }catch(MessagingException e){
            log.info("Sending From: " + this.from + " Sending To: " + to);
            log.error("Error occured during sending mail"+e);
            return false;
        }
    }

    public boolean send(String to, String bodyTemplate, Map<String, String> bodyValues,
                        String subjectTemplate, Map<String, String> subjectValues, DataSource dataSource) {
        Session session = getPropertyAndSessionDetails();
        try{
            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(this.from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(getHtmlByTemplateAndContext(subjectTemplate, subjectValues), "UTF-8");
            message.setText(getHtmlByTemplateAndContext(bodyTemplate, bodyValues), "UTF-8");

            MimeBodyPart attachment = new MimeBodyPart();
            attachment.setDataHandler(new DataHandler(dataSource));
            attachment.setFileName("Appraisal-Report.zip");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(attachment);
            message.setContent(multipart);
            Transport.send(message);
            log.info("Mail send successfully to :"+to);
            return true;
        }catch(MessagingException e){
            log.info("Sending From: " + this.from + " Sending To: " + to);
            log.error("Error occured during sending mail"+e);
            return false;
        }
    }

    public static VelocityEngine getVelocityEngine(){

        VelocityEngine ve = new VelocityEngine();
        Properties props = new Properties();

        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init(props);
        return ve;
    }

    public static String getHtmlByTemplateAndContext(String templateName, Map<String, String> values){

        VelocityEngine ve = getVelocityEngine();

        Template template = ve.getTemplate("template-email/" + templateName, "UTF-8");

        VelocityContext context = new VelocityContext();

        for (Map.Entry<String,String> entry : values.entrySet()) {
            context.put(entry.getKey(), entry.getValue());
        }
        StringWriter writer = new StringWriter();
        template.merge( context, writer );
        return  writer.toString();
    }

    private Session getPropertyAndSessionDetails(){
        Properties props = new Properties();
        props.put("mail.smtp.host", this.host);
        props.put("mail.smtp.port", this.port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable",true);
        Session session = Session.getDefaultInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(MailUtil.this.from, MailUtil.this.password);
                    }
                });
        return session;
    }
}
