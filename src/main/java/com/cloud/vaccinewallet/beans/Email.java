package com.cloud.vaccinewallet.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class Email {
    @Autowired
    private JavaMailSender javaMailSender;

    public void sendEmail(String to, String subject, String text) {
        System.out.println("Sending Email...");
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        try {
            System.out.println(msg);
            javaMailSender.send(msg);
            System.out.println("Mail ID Correct");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Done");
    }

}
