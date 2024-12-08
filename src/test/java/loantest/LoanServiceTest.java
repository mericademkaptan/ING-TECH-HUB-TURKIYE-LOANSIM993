package loantest;

import com.app.model.Customer;
import com.app.model.Loan;
import com.app.model.LoanInstallment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.app.repository.CustomerRepository;
import com.app.repository.LoanInstallmentRepository;
import com.app.repository.LoanRepository;
import com.app.service.LoanService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class LoanServiceTest {

    private static final Logger log = LoggerFactory.getLogger(LoanServiceTest.class);
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanInstallmentRepository loanInstallmentRepository;

    @InjectMocks
    private LoanService loanService;

    private Customer customer;
    private Loan loan;
    private LoanInstallment installment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Set up a dummy user with predefined attributes for testing
        customer = new Customer();
        customer.setId(1L);
        customer.setName("Fatih");
        customer.setSurname("Terim");
        customer.setCreditLimit(10000.0);
        customer.setUsedCreditLimit(2000.0);

        loan = new Loan();
        loan.setId(1L);
        loan.setCustomer(customer);
        loan.setLoanAmount(1000.0);
        loan.setIsPaid(false);
        loan.setNumberOfInstallments(6);
        loan.setCreateDate(LocalDate.now());

        installment = new LoanInstallment();
        installment.setLoan(loan);
        installment.setAmount(200.0);
        installment.setPaid(false);
        installment.setPaidAmount(0.0);
        installment.setDueDate(LocalDate.now().plusMonths(1));
    }

    @Test
    void testCreateLoanSuccess() {
        log.info("Testing loan creation scenario. ");
        // Mocking repository responses
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(loanRepository.save(Mockito.any(Loan.class))).thenReturn(loan);
        when(loanInstallmentRepository.saveAll(Mockito.anyList())).thenReturn(Arrays.asList(installment));

        // Calling the method under test
        Loan createdLoan = loanService.createLoan(1L, 1000.0, 0.2, 6);

        // Verifying the results
        assertNotNull(createdLoan);
        assertEquals(1000.0, createdLoan.getLoanAmount());
        assertEquals(6, createdLoan.getNumberOfInstallments());
        assertFalse(createdLoan.getIsPaid());
    }

    @Test
    void testCreateLoanCustomerNotFound() {
        log.info("Testing customer not found scenario. ");
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // Calling the method and expecting an exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loanService.createLoan(1L, 1000.0, 0.2, 6);
        });

        assertEquals("Customer not found", exception.getMessage());
    }

    @Test
    void testCreateLoanInsufficientCredit() {
        log.info("Testing insufficient credit during loan creation scenario. ");
        customer.setUsedCreditLimit(9500.0);  // Set used credit limit to more than available credit

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loanService.createLoan(1L, 1000.0, 0.2, 6);
        });

        assertEquals("Customer does not have enough credit for this loan.", exception.getMessage());
    }

    @Test
    void testCreateLoanInvalidInstallments() {
        log.info("Testing invalid installment amount scenario. ");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        // Invalid installment number (e.g., 5 is not allowed)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loanService.createLoan(1L, 1000.0, 0.2, 5);
        });

        assertEquals("Invalid installment number. Allowed values are only 6, 9, 12 or 24.", exception.getMessage());
    }

    @Test
    void testCreateLoanInvalidInterestRate() {
        log.info("Testing invalid interest rate scenario. ");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        // Invalid interest rate (e.g., less than 0.1)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loanService.createLoan(1L, 1000.0, 0.05, 6);
        });

        assertEquals("Invalid interest rate as it must be between 0.1-0.5.", exception.getMessage());
    }

    @Test
    void testListLoansSuccess() {
        log.info("Testing listing loans scenario. ");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(loanRepository.findByCustomer(customer)).thenReturn(Arrays.asList(loan));

        List<Loan> loans = loanService.listLoans(1L);

        assertNotNull(loans);
        assertEquals(1, loans.size());
        assertEquals(loan, loans.get(0));
    }

    @Test
    void testPayLoanSuccess() {
        log.info("Testing loan payment scenario. ");
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanInstallmentRepository.findByLoan(loan)).thenReturn(Arrays.asList(installment));

        String result = loanService.payLoan(1L, 200.0);

        assertTrue(installment.getPaid());
        assertEquals(200.0, installment.getPaidAmount());
        assertEquals("Successfully paid 1 installments. Total amount spent: 200.0", result);
    }

    @Test
    void testPayLoanInsufficientFunds() {
        log.info("Testing insufficient funds scenario. ");
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanInstallmentRepository.findByLoan(loan)).thenReturn(Arrays.asList(installment));

        String result = loanService.payLoan(1L, 100.0);

        assertFalse(installment.getPaid());
        assertEquals("Insufficient funds to pay any installment.", result);
    }

}
