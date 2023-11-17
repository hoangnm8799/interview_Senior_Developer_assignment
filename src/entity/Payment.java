package entity;

import java.math.BigDecimal;
import java.util.Date;

public class Payment {
    private static int count = 0;

    private int id;
    private BigDecimal amount;
    private Date paymentDate;
    private String state;
    private int billId;

    public Payment(int billId, BigDecimal amount, Date paymentDate, String state) {
        this.id = generatePaymentId();
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.state = state;
        this.billId = billId;
    }

    public int getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public String getState() {
        return state;
    }

    public int getBillId() {
        return billId;
    }

    private static int generatePaymentId() {
        count++;
        return count;
    }
}
