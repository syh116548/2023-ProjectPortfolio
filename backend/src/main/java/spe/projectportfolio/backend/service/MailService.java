package spe.projectportfolio.backend.service;


import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import java.util.Date;

@Service
public class MailService {

    @Resource
    private JavaMailSenderImpl javaMailSender;

    @Value("${spring.mail.username}")
    private String sendMailer;


    private void checkMail(String receiveEmail, String subject, String emailMsg){

        if(StringUtils.isEmpty(receiveEmail)) {
            throw new RuntimeException("The mail recipient cannot be empty.");
        }
        if(StringUtils.isEmpty(subject)) {
            throw new RuntimeException("The mail subject cannot be empty.");
        }
        if(StringUtils.isEmpty(emailMsg)) {
            throw new RuntimeException("The mail content cannot be empty.");
        }
    }


    public Boolean sendTextMail(String receiveEmail, String subject, String emailMsg) {

        checkMail(receiveEmail, subject, emailMsg);
        try {

            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(javaMailSender.createMimeMessage(), true);

            mimeMessageHelper.setFrom(sendMailer);

            mimeMessageHelper.setTo(receiveEmail.split(","));

            mimeMessageHelper.setSubject(subject);

            mimeMessageHelper.setText(emailMsg);

            mimeMessageHelper.setSentDate(new Date());


            javaMailSender.send(mimeMessageHelper.getMimeMessage());
            return true;
        } catch (MessagingException e) {
            System.out.println("Failed to send mail: " + e.getMessage());
            return false;
        }
    }
}
