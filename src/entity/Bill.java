package entity;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Bill {
    private int billNo;
    private String type;
    private BigDecimal amount;
    private Date dueDate;
    private String state;
    private String provider;
    private Date scheduledPaymentDate;

    private Date paymentDate;

    public Bill(int billNo, String type, BigDecimal amount, Date dueDate, String state, String provider) {
        this.billNo = billNo;
        this.type = type;
        this.amount = amount;
        this.dueDate = dueDate;
        this.state = state;
        this.provider = provider;
        this.scheduledPaymentDate = null;
    }

    public int getBillNo() {
        return billNo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Date getScheduledPaymentDate() {
        return scheduledPaymentDate;
    }

    public void setScheduledPaymentDate(Date scheduledPaymentDate) {
        this.scheduledPaymentDate = scheduledPaymentDate;
    }

    public void setBillNo(int billNo) {
        this.billNo = billNo;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }
}