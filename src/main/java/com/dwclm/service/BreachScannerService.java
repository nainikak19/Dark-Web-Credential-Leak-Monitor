package com.dwclm.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dwclm.dto.DashboardMetricsDTO;
import com.dwclm.model.AlertLog;
import com.dwclm.model.BreachResult;
import com.dwclm.model.User;
import com.dwclm.repository.AlertLogRepository;
import com.dwclm.repository.BreachResultRepository;
import com.dwclm.repository.BreachedEmailRepository;
import com.dwclm.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BreachScannerService {

	private final UserRepository userRepository;
	private final BreachResultRepository breachResultRepository;
	private final BreachedEmailRepository breachedEmailRepository;
	private final AlertLogRepository alertLogRepository;
	private final JavaMailSender mailSender;

	// Scans all users to check for breaches
	public void scanAllUsers() {
		List<User> users = userRepository.findAll();
		for (User user : users) {
			boolean breached = checkEmailBreach(user);

			// Only save if breach status has changed
			if (user.isBreached() != breached) {
				user.setBreached(breached);
				userRepository.save(user);
			}
		}
	}

	// Scans a single user email
	public boolean scanSingleUser(User user) {
		boolean breached = checkEmailBreach(user);

		if (user.isBreached() != breached) {
			user.setBreached(breached);
			userRepository.save(user);
		}
		return breached;
	}

	// Checks if user's email is in fake breach DB
	private boolean checkEmailBreach(User user) {
		return breachedEmailRepository.findByEmailIgnoreCase(user.getEmail()).map(fakeBreach -> {

			// OPTIONAL: Avoid duplicate breach entries
			boolean alreadyLogged = breachResultRepository.existsByUserIdAndBreachName(user.getId(),
					fakeBreach.getBreachName());

			if (alreadyLogged) {
				return true;
			}

			// Save in BreachResult (history)
			BreachResult breachResult = BreachResult.builder().breachName(fakeBreach.getBreachName())
					.domain(fakeBreach.getDomain()).breachDate(fakeBreach.getBreachDate())
					.description(fakeBreach.getDescription()).user(user).detectedAt(LocalDateTime.now()).build();

			breachResultRepository.save(breachResult);

			// Send alert email
			sendAlert(user.getEmail(), "Breach detected: " + fakeBreach.getBreachName(), fakeBreach.getDescription());
			return true;
		}).orElse(false);
	}

	// Saves alert in AlertLog and sends email
	private void sendAlert(String email, String subject, String message) {
		// Save in alert log
		alertLogRepository.save(
				AlertLog.builder().recipientEmail(email).alertMessage(message).sentAt(LocalDateTime.now()).build());

		try {
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(email);
			mailMessage.setSubject(subject);
			mailMessage.setText("Hello,\n\n" + message + "\n\nStay safe,\nDWCLM System");

			mailSender.send(mailMessage);
			System.out.println("EMAIL SENT TO: " + email);
		} catch (Exception e) {
			System.out.println("Failed to send email to " + email + ": " + e.getMessage());
		}
	}

	// Daily scheduled scan at 10 AM
	@Scheduled(cron = "0 0 10 * * ?")
	public void scheduledScan() {
		System.out.println("Scheduled scan started at " + LocalDateTime.now());
		scanAllUsers();
		System.out.println("Scheduled scan completed at " + LocalDateTime.now());
	}

	// Returns dashboard stats
	public DashboardMetricsDTO getDashboardMetrics() {
		long totalUsers = userRepository.count();

		// Optimize breached users count via distinct user ids
		long breachedUsers = breachResultRepository.countDistinctUserId();

		long totalBreaches = breachResultRepository.count();

		long uniqueBreachNames = breachResultRepository.findAll().stream().map(BreachResult::getBreachName).distinct()
				.count();

		return new DashboardMetricsDTO(totalUsers, breachedUsers, totalBreaches, uniqueBreachNames);
	}
}
