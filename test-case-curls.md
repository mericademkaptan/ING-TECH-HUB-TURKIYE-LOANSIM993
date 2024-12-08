# API Test Cases for LOAN-SIM-993

### 1. **Create Loan (`POST /api/loans/create`)**

#### Test Case 1: Valid Loan Request

# Test case to create a new loan with valid parameters
curl -X POST "http://localhost:8080/api/loans/create" \
     -H "Content-Type: application/json" \
     -d '{
           "customerId": 12345,
           "amount": 5000.0,
           "interestRate": 0.5,
           "installments": 12
         }'

# Test case where required fields like "installments" are missing in the loan request
curl -X POST "http://localhost:8080/api/loans/create" \
     -H "Content-Type: application/json" \
     -d '{
           "customerId": 12345,
           "amount": 5000.0
         }'

# Test case to list loans for an existing customer with ID 12345
curl -X GET "http://localhost:8080/api/loans/list?customerId=12345" \
     -H "Content-Type: application/json"

# Test case for a customer (ID: 67890) who has no loans
curl -X GET "http://localhost:8080/api/loans/list?customerId=67890" \
     -H "Content-Type: application/json"

# Test case to list installments for an existing loan with ID 1
curl -X GET "http://localhost:8080/api/loans/installments?loanId=1" \
     -H "Content-Type: application/json"

# Test case for a loan (ID: 999) with no installments available
curl -X GET "http://localhost:8080/api/loans/installments?loanId=999" \
     -H "Content-Type: application/json"

# Test case to make a valid payment of 500.0 towards loan ID 1
curl -X POST "http://localhost:8080/api/loans/pay" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "loanId=1&amount=500.0"

# Test case where the loan ID (999) does not exist in the system
curl -X POST "http://localhost:8080/api/loans/pay" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "loanId=999&amount=500.0"
