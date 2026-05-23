INSERT INTO clients (id, full_name, inn) VALUES
    ('CLT-00123', 'Иванов Иван Иванович',   '1234567890'),
    ('CLT-00124', 'Петров Петр Петрович',    '098765432109'),
    ('CLT-00125', 'Сидорова Анна Сергеевна', '1122334455');

INSERT INTO risk_profile (inn, credit_history) VALUES
    ('1234567890',   'Хорошая'),
    ('098765432109', 'Плохая'),
    ('1122334455',   'Хорошая')
ON CONFLICT (inn) DO NOTHING;

INSERT INTO deals (deal_number, loan_amount_rub, interest_rate, issue_date, loan_term_months, repayment_method) VALUES
    ('CRD-2025-00123', 500000.00, 12.5, '2024-01-15', 36, 'Аннуитетный'),
    ('CRD-2025-00124', 200000.00,  9.9, '2023-06-01', 24, 'Дифференцированный'),
    ('CRD-2025-00125', 750000.00, 14.0, '2024-03-10', 60, 'Аннуитетный'),
    ('CRD-2025-00126', 150000.00, 11.0, '2025-01-20', 12, 'Дифференцированный');

INSERT INTO client_deals (deal_id, client_id) VALUES
    ('CRD-2025-00123', (SELECT id FROM clients WHERE inn = '1234567890')),
    ('CRD-2025-00124', (SELECT id FROM clients WHERE inn = '1234567890')),
    ('CRD-2025-00125', (SELECT id FROM clients WHERE inn = '098765432109')),
    ('CRD-2025-00126', (SELECT id FROM clients WHERE inn = '1122334455'));
