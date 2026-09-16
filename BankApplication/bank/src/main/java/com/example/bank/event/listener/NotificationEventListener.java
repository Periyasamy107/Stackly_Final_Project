package com.example.bank.event.listener;

import com.example.bank.common.enums.NotificationType;
import com.example.bank.event.CustomerCreatedEvent;
import com.example.bank.event.InvestmentMaturityEvent;
import com.example.bank.event.LoanApprovedEvent;
import com.example.bank.event.TransactionCompletedEvent;
import com.example.bank.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Async("bankTaskExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleCustomerCreated(
            CustomerCreatedEvent event) {

        notificationService.createNotification(
                event.eventId(),
                event.userId(),
                NotificationType.CUSTOMER_CREATED,
                "Customer profile created",
                "Your banking customer profile has been created successfully.",
                "CUSTOMER",
                event.customerId()
        );
    }

    @Async("bankTaskExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleLoanApproved(
            LoanApprovedEvent event) {

        notificationService.createNotification(
                event.eventId(),
                event.userId(),
                NotificationType.LOAN_APPROVED,
                "Loan approved",
                "Your "
                        + event.loanType()
                        + " loan (ID "
                        + event.loanId()
                        + ") for "
                        + event.principalAmount()
                        + " has been approved.",
                "LOAN",
                event.loanId()
        );
    }

    @Async("bankTaskExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleInvestmentMaturity(
            InvestmentMaturityEvent event) {

        notificationService.createNotification(
                event.eventId(),
                event.userId(),
                NotificationType.INVESTMENT_MATURED,
                "Investment matured",
                "Your investment (ID "
                        + event.investmentId()
                        + ") has reached maturity and is ready for settlement.",
                "INVESTMENT",
                event.investmentId()
        );
    }

    @Async("bankTaskExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleTransactionCompleted(
            TransactionCompletedEvent event) {

        notificationService.createNotification(
                event.eventId(),
                event.userId(),
                NotificationType.TRANSACTION_COMPLETED,
                "Transaction completed",
                buildTransactionMessage(event),
                "TRANSACTION",
                event.transactionId()
        );
    }

    private String buildTransactionMessage(
            TransactionCompletedEvent event) {

        String description =
                event.description() == null
                        || event.description().isBlank()
                        ? ""
                        : " " + event.description();

        return "A "
                + event.transactionType()
                .name()
                .toLowerCase()
                + " transaction of "
                + event.amount()
                + " was completed."
                + description;
    }
}