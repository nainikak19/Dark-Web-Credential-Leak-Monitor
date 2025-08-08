package com.dwclm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Sends an email alert
    public void sendBreachAlert(String toEmail, String breachedSites) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail); // Recipient email
        message.setSubject("Alert: Your Email Was Found in a Data Breach");
        message.setText("Dear user,\n\nYour email has been found in the following breaches: \n" 
                        + breachedSites 
                        + "\n\nPlease reset your passwords immediately!\n\nRegards,\nDWCLM Security Team");

        mailSender.send(message);
        System.out.println("Alert email sent to: " + toEmail);
    }
}
