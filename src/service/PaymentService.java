package service;

import java.math.BigDecimal;
import java.util.Date;

public interface PaymentService {
    void cashIn(BigDecimal amount);

    void listBills();

    void createBill(int billNo, String type, BigDecimal amount, Date dueDate, String state, String provider);

    void payBills(String[] billIds);

    void listBillsByDueDate();

    void schedulePayment(String billId, String scheduledDateStr);

    void listPayments();

    void searchBillsByProvider(String provider);
}