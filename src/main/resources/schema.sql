-- Customer table
CREATE TABLE IF NOT EXISTS customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    surname VARCHAR(255),
    creditLimit DOUBLE,
    usedCreditLimit DOUBLE
);

-- Loan table
CREATE TABLE IF NOT EXISTS loan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loanAmount DOUBLE,
    numberOfInstallments INT,
    customer_id BIGINT,
    paid BOOLEAN,
    FOREIGN KEY (customer_id) REFERENCES customer(id)
);

-- Installment table
CREATE TABLE IF NOT EXISTS loan_installment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id BIGINT,
    amount DOUBLE,
    paidAmount DOUBLE,
    paid BOOLEAN,
    dueDate DATE,
    paymentDate DATE,
    FOREIGN KEY (loan_id) REFERENCES loan(id)
);
