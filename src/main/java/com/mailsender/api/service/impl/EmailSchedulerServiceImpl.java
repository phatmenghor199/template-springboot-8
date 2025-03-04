package com.mailsender.api.service.impl;

import com.mailsender.api.dto.*;
import com.mailsender.api.enumation.AccountType;
import com.mailsender.api.enumation.Status;
import com.mailsender.api.exceptions.NotFoundException;
import com.mailsender.api.mapper.EmailSendHistoryMapper;
import com.mailsender.api.models.*;
import com.mailsender.api.repository.CustomerRepository;
import com.mailsender.api.repository.EmailSendHistoryRepository;
import com.mailsender.api.repository.ExchangeDashboardRepository;
import com.mailsender.api.repository.ExchangeRateRepository;
import com.mailsender.api.service.EmailSchedulerService;
import com.mailsender.api.utils.Constants;
import com.mailsender.api.utils.SoapApiService;
import com.mailsender.api.utils.filter.EmailSchedulerSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSchedulerServiceImpl implements EmailSchedulerService {
    private final CustomerRepository customerRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeDashboardRepository exchangeDashboardRepository;
    private final EmailSendHistoryRepository emailSendHistoryRepository;
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final SoapApiService soapApiService;
    private final EmailSendHistoryMapper emailSendHistoryMapper;

    @Scheduled(cron = "0 0 5-17 * * *", zone = "Asia/Phnom_Penh")
    @Transactional
    public void checkSchedulesAndSendEmails() {

        ExchangeRate exchangeRate = fetchExchangeRateFromExternalService();

        if (exchangeRate == null) {
            log.error("Skipping email processing due to invalid exchange rate.");

            // Save history with null exchange rate if fetching failed
            List<Customer> customers = customerRepository.findAll();
            for (Customer customer : customers) {
                if (Constants.ACTIVE.equals(customer.getStatus().name())) {
                    saveEmailHistory(customer, null, Constants.FAILURE, "Exchange rate fetch failed.");
                }
            }
            return;
        }

        ExchangeRate savedExchangeRate = exchangeRateRepository.save(exchangeRate);
        log.info("Saved exchange rate: {}", savedExchangeRate);

        List<Customer> customers = customerRepository.findAll();
        for (Customer customer : customers) {
            if (Constants.ACTIVE.equals(customer.getStatus().name())) {
                customer.getSchedules().forEach(schedule -> {
                    if (shouldSendEmail(schedule)) {
                        try {
                            sendEmail(customer, savedExchangeRate);
                            // 2️⃣ Update history AFTER success
                            saveEmailHistory(customer, savedExchangeRate, Constants.SUCCESS, null);
                        } catch (Exception e) {
                            log.error("Email sending failed for customer {}: {}", customer.getUsername(), e.getMessage());
                            saveEmailHistory(customer, savedExchangeRate, Constants.FAILURE, e.getMessage());
                        }
                    }
                });
            }
        }

    }

    @Transactional
    @Scheduled(cron = "0 0 11 * * *", zone = "Asia/Phnom_Penh") // Runs every day at 11 AM Phnom Penh time
    public void checkSchedulesLogExchange() {
        ExchangeDashboard exchangeRate = fetchExchangeRateFromService();
        if (exchangeRate == null) {
            log.warn("Exchange rate data is null, inserting default values (0).");

            exchangeRate = new ExchangeDashboard();
            exchangeRate.setBuyRateUsd(BigDecimal.ZERO);
            exchangeRate.setSellRateUsd(BigDecimal.ZERO);
            exchangeRate.setBuyRateThb(BigDecimal.ZERO);
            exchangeRate.setSellRateThb(BigDecimal.ZERO);
            exchangeRate.setFetchedAt(LocalDate.now());

        }
        ExchangeDashboard savedExchangeRate = exchangeDashboardRepository.save(exchangeRate);
        log.info("Saved exchange rate to dashboard: {}", savedExchangeRate);
    }

    @Override
    public AllEmailSchedulerResponseDto getAllHistory(Status status, LocalDate date, String search, int pageNo, int pageSize) {
        Specification<EmailSendHistory> specification = Specification.where(EmailSchedulerSpecifications.hasStatus(status)).and(EmailSchedulerSpecifications.createdOn(date)).and(EmailSchedulerSpecifications.customerUsernameOrEmailContains(search));

        // Create a Pageable that sorts by createdAt in descending order (newest first)
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by("createdAt").descending());
        Page<EmailSendHistory> emailSendHistories = emailSendHistoryRepository.findAll(specification, pageable);
        List<EmailSchedulerResponseDto> historyMapperDtoList = emailSendHistoryMapper.toDtoList(emailSendHistories.getContent());

        log.info("All Email Scheduler ResponseDto founds: {}", historyMapperDtoList);
        return emailSendHistoryMapper.mapToListDto(historyMapperDtoList, emailSendHistories);
    }

    @Override
    public EmailSchedulerResponseDto getEmailSchedulerById(UUID id) {
        log.info("Fetching Email scheduler with ID: {}", id);

        EmailSendHistory emailSendHistory = emailSendHistoryRepository.findById(id).orElseThrow(() -> {
            log.error("Email History not found with ID: {}", id);
            return new NotFoundException("Email History not found with ID:" + id);
        });

        EmailSchedulerResponseDto schedulerResponseDto = emailSendHistoryMapper.toDto(emailSendHistory);
        log.info("Email History found : {}", schedulerResponseDto.toString());
        return schedulerResponseDto;
    }

    @Transactional
    @Override
    public boolean resendEmailByHistoryId(UUID historyId) {
        log.info("Resending email for history id: {}", historyId);

        // Retrieve the email history record by its ID
        EmailSendHistory history = emailSendHistoryRepository.findById(historyId)
                .orElseThrow(() -> {
                    log.error("Email History resend not found with ID: {}", historyId);
                    return new NotFoundException("Email History not found with ID: " + historyId);
                });

        // Get the customer from the history
        Customer customer = history.getCustomer();
        if (customer == null || customer.getEmail() == null) {
            log.error("Customer email is missing for history id: {}", historyId);
            return false;
        }

        try {

            // Fetch a new exchange rate from the external SOAP service
            ExchangeRate newExchangeRate = fetchExchangeRateFromExternalService();
            if (newExchangeRate == null) {
                throw new Exception("New exchange rate fetch failed");
            }
            // Save the new exchange rate
            ExchangeRate savedNewExchangeRate = exchangeRateRepository.save(newExchangeRate);
            log.info("New exchange rate retrieved and saved: {}", savedNewExchangeRate);

            // Attempt to resend the email using the stored exchange rate
            sendEmail(customer, history.getExchangeRate());
            history.setStatus(Status.SUCCESS);
            history.setErrorMessage(null);
            history.setSentAt(LocalDateTime.now());
            emailSendHistoryRepository.save(history);
            log.info("Resend successful for history id: {}", historyId);
            return true;

        } catch (Exception e) {
            log.error("Resend failed for history id {}: {}", historyId, e.getMessage());
            // Update history to FAILURE if resend fails
            history.setStatus(Status.FAILURE);
            history.setErrorMessage(e.getMessage());
            history.setSentAt(LocalDateTime.now());
            emailSendHistoryRepository.save(history);
            return false;
        }
    }

    @Override
    public boolean sendEmailByCustomerId(Long customerId) {
        log.info("sending email for customer id: {}", customerId);

        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> {
            log.error("Customer not found with ID: {}", customerId);
            return new NotFoundException("Customer not found with ID:" + customerId);
        });

        try {
            // Fetch a new exchange rate from the external SOAP service
            ExchangeRate newExchangeRate = fetchExchangeRateFromExternalService();
            if (newExchangeRate == null) {
                throw new Exception("New exchange rate fetch failed");
            }

            // Save the new exchange rate
            ExchangeRate savedExchangeRate = exchangeRateRepository.save(newExchangeRate);
            log.info("New exchange rate retrieved and saved to database: {}", savedExchangeRate);

            // Send the email
            sendEmail(customer, savedExchangeRate);

            // Save email history as SUCCESS
            saveEmailHistory(customer, savedExchangeRate, Constants.SUCCESS, null);
            log.info("Email successfully sent to customer id: {}", customerId);

            return true;
        } catch (Exception e) {
            log.error("Email sending failed for customer id {}: {}", customerId, e.getMessage());
            // Save email history as FAILURE
            saveEmailHistory(customer, null, Constants.FAILURE, e.getMessage());
            return false;
        }
    }

    @Override
    public ExchangeRateDto getLatestExchangeRate() {
        log.info("Fetching latest exchange rate via getLatestExchangeRate()");
        ExchangeRate exchangeRate = fetchExchangeRateFromExternalService();
        if (exchangeRate == null) {
            log.error("Failed to fetch exchange rate from SOAP service.");
            throw new NotFoundException("Exchange rate not found");
        }
        ExchangeRateDto dtoExchange = new ExchangeRateDto();
        dtoExchange.setBuyRateUsd(exchangeRate.getBuyRateUsd());
        dtoExchange.setSellRateUsd(exchangeRate.getSellRateUsd());
        dtoExchange.setBuyRateThb(exchangeRate.getBuyRateThb());
        dtoExchange.setSellRateThb(exchangeRate.getSellRateThb());
        dtoExchange.setFetchedAt(exchangeRate.getFetchedAt());
        return dtoExchange;
    }

    // Fetch exchange rate from external service (simulate for now)
    private ExchangeRate fetchExchangeRateFromExternalService() {
        log.info("Fetching exchange rate from external SOAP service...");

        ExchangeRate exchangeRate = new ExchangeRate();
        try {
            ExchangeRateFromSoap usdRate = soapApiService.getAccountInfo(AccountType.KHR);
            ExchangeRateFromSoap thbRate = soapApiService.getAccountInfo(AccountType.THB);

            boolean isUsdValid = usdRate != null && usdRate.getBuyRate().compareTo(BigDecimal.ZERO) > 0 && usdRate.getSellRate().compareTo(BigDecimal.ZERO) > 0;
            boolean isThbValid = thbRate != null && thbRate.getBuyRate().compareTo(BigDecimal.ZERO) > 0 && thbRate.getSellRate().compareTo(BigDecimal.ZERO) > 0;

            if (!isUsdValid && !isThbValid) {
                log.error("Failed to fetch valid exchange rates for both USD and THB.");
                return null;
            }

            if (isUsdValid) {
                exchangeRate.setBuyRateUsd(usdRate.getBuyRate());
                exchangeRate.setSellRateUsd(usdRate.getSellRate());
            } else {
                log.warn("Invalid or missing USD exchange rate from SOAP service.");
            }

            if (isThbValid) {
                exchangeRate.setBuyRateThb(thbRate.getBuyRate());
                exchangeRate.setSellRateThb(thbRate.getSellRate());
            } else {
                log.warn("Invalid or missing THB exchange rate from SOAP service.");
            }

            exchangeRate.setFetchedAt(LocalDateTime.now());
            log.info("Fetched exchange rate successfully: {}", exchangeRate);
            return exchangeRate;
        } catch (Exception e) {
            log.error("Error fetching exchange rate from SOAP service: {}", e.getMessage(), e);
            return null;
        }
    }

    private ExchangeDashboard fetchExchangeRateFromService() {
        log.info("Fetchings exchange rate from external SOAP service...");

        ExchangeDashboard exchangeRate = new ExchangeDashboard();
        try {
            ExchangeRateFromSoap usdRate = soapApiService.getAccountInfo(AccountType.KHR);
            ExchangeRateFromSoap thbRate = soapApiService.getAccountInfo(AccountType.THB);

            boolean isUsdValid = usdRate != null && usdRate.getBuyRate().compareTo(BigDecimal.ZERO) > 0 && usdRate.getSellRate().compareTo(BigDecimal.ZERO) > 0;
            boolean isThbValid = thbRate != null && thbRate.getBuyRate().compareTo(BigDecimal.ZERO) > 0 && thbRate.getSellRate().compareTo(BigDecimal.ZERO) > 0;

            if (!isUsdValid && !isThbValid) {
                log.error("Failed to fetch valid exchanges rates for both USD and THB.");
                return null;
            }

            if (isUsdValid) {
                exchangeRate.setBuyRateUsd(usdRate.getBuyRate());
                exchangeRate.setSellRateUsd(usdRate.getSellRate());
            } else {
                log.warn("Invalid or missing USD exchanges rate from SOAP service.");
            }

            if (isThbValid) {
                exchangeRate.setBuyRateThb(thbRate.getBuyRate());
                exchangeRate.setSellRateThb(thbRate.getSellRate());
            } else {
                log.warn("Invalid or missing THB exchanges rate from SOAP service.");
            }

            exchangeRate.setFetchedAt(LocalDate.now());
            log.info("Fetched exchanges rate successfully: {}", exchangeRate);
            return exchangeRate;
        } catch (Exception e) {
            log.error("Error fetching exchange rate from SOAP service: {}", e.getMessage(), e);
            return null;
        }
    }


    private boolean shouldSendEmail(CustomerSchedule schedule) {
        DayOfWeek today = LocalDateTime.now().getDayOfWeek();

        // Format current time and send time in "hh:mm a" format (AM/PM)
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
        String scheduledTime = schedule.getSendTime();

        // Log for debugging
        log.info("Today's Day: {}", today);
        log.info("Current time (formatted): {}", currentTime);
        log.info("Schedule send time: {}", scheduledTime);

        // Parse the times to compare (convert both to LocalTime)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime sendDateTime = LocalDateTime.of(currentDateTime.toLocalDate(),
                LocalTime.parse(scheduledTime, formatter));

        // Allow up to 5 minutes of delay after the scheduled time
        LocalDateTime sendWindowEndTime = sendDateTime.plusMinutes(55);  // Add a 5-minute delay window

        // Check if current time is within the send window
        boolean shouldSend = schedule.getSendDays().contains(today.name()) &&
                !currentDateTime.isBefore(sendDateTime) && currentDateTime.isBefore(sendWindowEndTime);

        log.info("Should send email for schedule {}: {}", schedule, shouldSend);
        return shouldSend;
    }

    private void sendEmail(Customer customer, ExchangeRate exchangeRate) {
        try {
            if (customer.getEmail() == null) {
                log.error("Email is missing for customer: {}", customer.getUsername());
                return;
            }

            if (exchangeRate == null) {
                log.error("Exchange rate is null. Cannot send email.");
                return;
            }

            log.info("Sending email to customer: {}", customer.getEmail());
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(customer.getEmail());
            helper.setFrom(Constants.SEND_FROM);

            ZoneId cambodiaZone = ZoneId.of("Asia/Phnom_Penh");
            LocalDate currentDate = LocalDate.now(cambodiaZone);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

            // Format the subject with Cambodia's date
            String subject = String.format("CPBank Exchange Rates on %s", currentDate.format(formatter));
            helper.setSubject(subject);

            Context context = new Context();
            context.setVariable("greeting", getGreeting() + ", ");
            context.setVariable("usd_buy", exchangeRate.getBuyRateUsd());
            context.setVariable("usd_sell", exchangeRate.getSellRateUsd());
            context.setVariable("thb_buy", exchangeRate.getBuyRateThb());
            context.setVariable("thb_sell", exchangeRate.getSellRateThb());

            if (templateEngine != null) {
                log.info("Template engine ======== is initialized.");
                String htmlContent = templateEngine.process("email-template.html", context);
                helper.setText(htmlContent, true);
                log.info("Template engine ======== is end.");
            } else {
                log.error("Template engine is not initialized.");
                return;
            }

            javaMailSender.send(message);
            log.info("Email sent successfully to {}", customer.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", customer.getEmail(), e.getMessage());
            saveEmailHistory(customer, exchangeRate, "FAILURE", "Fail to send email to customer");
        }
    }

    private String getGreeting() {
        int hour = LocalDateTime.now().getHour();
        if (hour >= 5 && hour < 12) {
            return Constants.GOOD_MORNING;
        } else if (hour >= 12 && hour < 18) {
            return Constants.GOOD_AFTERNOON;
        } else {
            return Constants.GOOD_EVENING;
        }
    }


    private void saveEmailHistory(Customer customer, ExchangeRate exchangeRate, String status, String errorMessage) {

        log.info("Saved customer history: {}", customer);
        EmailSendHistory history = new EmailSendHistory();
        history.setCustomer(customer);
        history.setExchangeRate(exchangeRate);
        history.setStatus(Status.valueOf(status));
        history.setErrorMessage(errorMessage);
        history.setSentAt(LocalDateTime.now());

        emailSendHistoryRepository.save(history);
        log.info("Saved email history: {}", history);
    }


}
