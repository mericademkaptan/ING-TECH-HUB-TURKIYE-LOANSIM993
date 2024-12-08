package com.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import com.app.model.Loan;
import com.app.model.LoanInstallment;
import com.app.model.LoanRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.app.service.LoanService;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    @Autowired
    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    /**
     * Creates a new loan based on the provided loan request details.
     *
     * @param loanRequest The loan request details (customerId, amount, interest rate, number of installments)
     * @return The created loan
     */
    @Operation(
            summary = "Create a new loan",
            description = "Creates a loan based on the provided loan request details.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Loan created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid loan request")
            }
    )
    @PostMapping("/create")
    public ResponseEntity<Loan> createLoan(
            @RequestBody(
                    description = "Loan request body containing customerId, amount, interest rate, and number of installments"
            ) LoanRequest loanRequest) {

        Loan createdLoan = loanService.createLoan(
                loanRequest.getCustomerId(),
                loanRequest.getAmount(),
                loanRequest.getInterestRate(),
                loanRequest.getInstallments()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLoan);
    }

    /**
     * Retrieves a list of loans for a specific customer.
     *
     * @param customerId The customer ID
     * @return A list of loans for the given customer
     */
    @Operation(
            summary = "List loans for a customer",
            description = "Retrieve all loans associated with a specific customer.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of loans retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "No loans found for the customer")
            }
    )
    @GetMapping("/list")
    public List<Loan> listLoans(
            @RequestParam Long customerId) {
        return loanService.listLoans(customerId);
    }

    /**
     * Retrieves a list of installments for a specific loan.
     *
     * @param loanId The loan ID
     * @return A list of installments for the given loan ID
     */
    @Operation(
            summary = "List installments for a loan",
            description = "Retrieve all installments for a specific loan.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Installments for the loan retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "No installments found for the given loan ID")
            }
    )
    @GetMapping("/installments")
    public List<LoanInstallment> listInstallments(
            @RequestParam Long loanId) {
        return loanService.listInstallments(loanId);
    }

    /**
     * Processes a payment towards a specific loan.
     *
     * @param loanId The loan ID to make a payment towards
     * @param amount The payment amount
     * @return Response message indicating the result of the payment
     */
    @Operation(
            summary = "Make a payment on a loan",
            description = "Make a payment towards a specific loan.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Payment successfully processed"),
                    @ApiResponse(responseCode = "400", description = "Invalid payment details"),
                    @ApiResponse(responseCode = "404", description = "Loan not found")
            }
    )
    @PostMapping("/pay")
    public ResponseEntity<String> payLoan(
            @RequestParam Long loanId,
            @RequestParam Double amount) {

        String response = loanService.payLoan(loanId, amount);
        return ResponseEntity.ok(response);
    }
}
