package com.app.service;

import com.app.model.Customer;
import com.app.model.Loan;
import com.app.model.LoanInstallment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.app.repository.CustomerRepository;
import com.app.repository.LoanInstallmentRepository;
import com.app.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class LoanService {

    private static final Logger log = LoggerFactory.getLogger(LoanService.class);
    private final CustomerRepository customerRepository;
    private final LoanRepository loanRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;

    @Autowired
    public LoanService(CustomerRepository customerRepository,
                       LoanRepository loanRepository,
                       LoanInstallmentRepository loanInstallmentRepository) {
        this.customerRepository = customerRepository;
        this.loanRepository = loanRepository;
        this.loanInstallmentRepository = loanInstallmentRepository;
    }

    @Transactional
    public Loan createLoan(Long customerId, Double amount, Double interestRate, Integer installments) {
        log.info("Attempting to create loan for the given customer with the ID: {}", customerId);
        log.info("Loan has amount: {}, has interest rate: {} and has the number of installments: {} ",amount,interestRate,installments);

        // Check if the user exists in our DB
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new RuntimeException("Customer not found"));
        log.info("Customer {} {} has valid credentials existing in our database, loan creation may resume. ",customer.getName(),customer.getSurname());

        // Check if the user has enough credit to achieve this loan
        Double availableCredit = customer.getCreditLimit() - customer.getUsedCreditLimit();
        log.info("Checking available credit for the given application. Found the available credit as: {} ", availableCredit);
        Double loanTotalAmount = amount * (1 + interestRate);
        if (availableCredit < loanTotalAmount) {
            throw new RuntimeException("Customer does not have enough credit for this loan.");
        }

        // Allow only 6-9-12-24 months of installments.
        List<Integer> validInstallments = Arrays.asList(6, 9, 12, 24);
        if (!validInstallments.contains(installments)) {
            throw new RuntimeException("Invalid installment number. Allowed values are only 6, 9, 12 or 24.");
        }

        // only 0.1 to 0.5 interest rate is allowed
        if (interestRate < 0.1 || interestRate > 0.5) {
            throw new RuntimeException("Invalid interest rate as it must be between 0.1-0.5.");
        }

        // Update the amount of used credit limit
        customer.setUsedCreditLimit(customer.getUsedCreditLimit() + loanTotalAmount);
        customerRepository.save(customer);
        // Here is the gimmick, we presume that the given credit limit is fixed and used credit limit is always increased
        // there might be cases where the used credit shall be deducted from another credit limit variable and the process
        // might work backwards, so that agent users can define, reduce or increase available limits of the users. This approach
        // guarantees a bulletproof mathematical induction but isn't close to real-life scenarios.

        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setLoanAmount(amount);
        loan.setNumberOfInstallments(installments);
        loan.setCreateDate(LocalDate.now());
        loan.setIsPaid(false);
        Loan savedLoan = loanRepository.save(loan);
        List<LoanInstallment> installmentsList = new ArrayList<>();
        for (int i = 1; i <= installments; i++) {
            LoanInstallment installment = new LoanInstallment();
            installment.setLoan(savedLoan);
            installment.setAmount(loanTotalAmount / installments);  // Equal installment amount
            installment.setPaidAmount(0.0);
            installment.setPaid(false);
            LocalDate dueDate = LocalDate.now().plusMonths(i).withDayOfMonth(1);
            installment.setDueDate(dueDate);
            installmentsList.add(installment);
        }

        loanInstallmentRepository.saveAll(installmentsList);
        return savedLoan;
    }

    public List<Loan> listLoans(Long customerId) {
        log.info("Listing loans for customer with ID: {}", customerId);
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new RuntimeException("Customer not found"));
        log.info("Found customer: {} {}", customer.getName(), customer.getSurname());
        List<Loan> loans = loanRepository.findByCustomer(customer);
        if (loans.isEmpty()) {
            log.info("No loans found for customer: {}", customerId);
        } else {
            log.info("Found {} loan(s) for customer: {}", loans.size(), customerId);
        }
        return loans;
    }

    public List<LoanInstallment> listInstallments(Long loanId) {
        log.info("Fetching installments for loan with ID: {}", loanId);

        Loan loan = loanRepository.findById(loanId).orElseThrow(() -> new RuntimeException("Loan not found"));
        log.info("Found loan with ID: {} for customer: {}", loanId, loan.getCustomer().getName());

        // Fetch installments associated with the loan
        List<LoanInstallment> installments = loanInstallmentRepository.findByLoan(loan);

        if (installments.isEmpty()) {
            log.info("No installments found for loan: {}", loanId);
        } else {
            log.info("Found {} installment(s) for loan: {}", installments.size(), loanId);
        }

        return installments;
    }


    public String payLoan(Long loanId, Double amount) {
        log.info("Processing payment for loan ID: {} with amount: {}", loanId, amount);

        Loan loan = loanRepository.findById(loanId).orElseThrow(() -> new RuntimeException("Loan not found"));
        log.info("Found loan with ID: {} for customer: {}", loanId, loan.getCustomer().getName());

        List<LoanInstallment> installments = loanInstallmentRepository.findByLoan(loan);
        List<LoanInstallment> paidInstallments = new ArrayList<>();
        Double totalPaid = 0.0;
        int installmentsPaid = 0;

        for (LoanInstallment installment : installments) {
            if (installment.getPaid()) {
                continue;
            }

            if (amount >= installment.getAmount()) {
                penaltyOrRewardCalculationForInstallment(installment);
                installment.setPaidAmount(installment.getAmount());
                installment.setPaid(true);
                installment.setPaymentDate(LocalDate.now());
                paidInstallments.add(installment);
                amount -= installment.getAmount();
                installmentsPaid++;

                totalPaid += installment.getAmount();

                log.info("Paid installment with due date: {} and amount: {}", installment.getDueDate(), installment.getAmount());
            } else {
                break;
            }
        }

        loanInstallmentRepository.saveAll(paidInstallments);

        if (installmentsPaid == 0) {
            log.info("No installments were paid because the provided amount was insufficient.");
            return "Insufficient funds to pay any installment.";
        }

        // Check if the loan is fully paid
        boolean isLoanPaid = installments.stream().allMatch(LoanInstallment::getPaid);
        if (isLoanPaid) {
            loan.setIsPaid(true);
            loanRepository.save(loan);
            log.info("Loan ID: {} has been fully paid.", loanId);
        }

        log.info("Successfully paid {} installments. Total paid: {}", installmentsPaid, totalPaid);
        return "Successfully paid " + installmentsPaid + " installments. Total amount spent: " + totalPaid;
    }

    // Helper method to apply rewards or penalties based on payment date
    private void penaltyOrRewardCalculationForInstallment(LoanInstallment installment) {
        LocalDate currentDate = LocalDate.now();
        long daysDifference = currentDate.toEpochDay() - installment.getDueDate().toEpochDay();

        // If the payment is before the due date, apply a discount
        if (daysDifference < 0) {
            double discount = installment.getAmount() * 0.001 * Math.abs(daysDifference);
            installment.setPaidAmount(installment.getAmount() - discount);
            log.info("Applied reward (discount) of {} for early payment. Paid amount is now: {}",
                    discount, installment.getPaidAmount());
        }
        // If the payment is after the due date, apply a penalty
        else if (daysDifference > 0) {
            double penalty = installment.getAmount() * 0.001 * daysDifference;
            installment.setPaidAmount(installment.getAmount() + penalty);
            log.info("Applied penalty of {} for late payment. Paid amount is now: {}",
                    penalty, installment.getPaidAmount());
        }
        // If the payment is on the due date, no reward or penalty
        else {
            installment.setPaidAmount(installment.getAmount());
            log.info("No reward or penalty applied. Paid amount is: {}", installment.getPaidAmount());
        }
    }

}
