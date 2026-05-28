CREATE TABLE clients (
    id          VARCHAR(50)  PRIMARY KEY,
    full_name   VARCHAR(255) NOT NULL,
    inn         VARCHAR(12)  NOT NULL UNIQUE,
    CONSTRAINT inn_format_check CHECK (inn ~ '^\d{10}$|^\d{12}$')
);

CREATE TABLE client_deals (
    deal_id   VARCHAR(50) PRIMARY KEY,
    client_id VARCHAR(50) NOT NULL,
    CONSTRAINT fk_client_deals_client
        FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);

CREATE TABLE risk_profile (
    inn            VARCHAR(12) PRIMARY KEY,
    credit_history VARCHAR(10) NOT NULL,
    CONSTRAINT rp_inn_format_check CHECK (inn ~ '^\d{10}$|^\d{12}$'),
    CONSTRAINT credit_history_values CHECK (credit_history IN ('Хорошая', 'Плохая'))
);

CREATE TABLE deals (
    id               BIGSERIAL PRIMARY KEY,
    deal_number      VARCHAR(50)    UNIQUE NOT NULL,
    loan_amount_rub  DECIMAL(15, 2) NOT NULL,
    interest_rate    DECIMAL(5, 2)  NOT NULL,
    issue_date       DATE           NOT NULL,
    loan_term_months INT            NOT NULL,
    repayment_method VARCHAR(50)    NOT NULL
);
