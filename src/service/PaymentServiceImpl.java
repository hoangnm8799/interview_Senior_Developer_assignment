package service;

import constants.PaymentState;
import entity.Bill;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PaymentServiceImpl implements PaymentService {

    private static BigDecimal availableBalance;
    private static List<Bill> bills;

    public static BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public static void setAvailableBalance(BigDecimal availableBalance) {
        PaymentServiceImpl.availableBalance = availableBalance;
    }

    public static List<Bill> getBills() {
        return bills;
    }

    public static void setBills(List<Bill> bills) {
        PaymentServiceImpl.bills = bills;
    }

    public PaymentServiceImpl() {
        this.availableBalance = BigDecimal.ZERO;
        bills = new ArrayList<>();
    }

    @Override
    public void cashIn(BigDecimal amount) {
        availableBalance = availableBalance.add(amount);
        System.out.println("Your available balance: " + availableBalance);

        // Check if current date is greater than the scheduled payment date for each bill
        Date currentDate = new Date();
        for (Bill bill : bills) {
            if (bill.getScheduledPaymentDate() != null && currentDate.after(bill.getScheduledPaymentDate())) {
                System.out.println("Automatic payment for scheduled bills");
                payBills(new String[]{String.valueOf(bill.getBillNo())});
            }
        }
    }

    @Override
    public void listBills() {
        System.out.printf("%-15s %-15s %-15s %-15s %-15s %-15s\n", "Bill No.", "Type", "Amount", "Due Date", "State", "PROVIDER");
        for (Bill bill : bills) {
            System.out.printf("%-15d %-15s %-15s %-15s %-15s %-15s%n",
                    bill.getBillNo(), bill.getType(), bill.getAmount(),
                    formatDate(bill.getDueDate()), bill.getState(), bill.getProvider());
        }
    }

    public String formatDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(date);
    }

    @Override
    public void createBill(int billNo, String type, BigDecimal amount, Date dueDate, String state, String provider) {
        Bill bill = new Bill(billNo, type, amount, dueDate, state, provider);
        bills.add(bill);
    }

    @Override
    public void payBills(String[] billIds) {
        boolean paymentSuccessful = true;
        List<Bill> billSorted = new ArrayList<Bill>();
        if(billIds.length > 1) {
            for (String billId : billIds) {
                billSorted.add(findBillById(billId));
                Collections.sort(billSorted, Comparator.comparing(Bill::getDueDate));
            }
        } else {
            billSorted.add(findBillById(billIds[0]));
        }
        for (Bill bill : billSorted) {
            if (bill != null) {
                if (bill.getAmount().compareTo(availableBalance) <= 0) {
                    availableBalance = availableBalance.subtract(bill.getAmount());
                    bill.setState(PaymentState.PROCESSED.getValue());
                    bill.setPaymentDate(new Date());
                    System.out.println("Payment has been completed for Bill with id " + bill.getBillNo());
                } else {
                    paymentSuccessful = false;
                    System.out.println("Sorry! Not enough fund to proceed with payment.");
                }
            } else {
                paymentSuccessful = false;
                System.out.println("Sorry! Not found a bill with such id.");
            }
        }
        if (paymentSuccessful) {
            System.out.println("Your current balance is: " + availableBalance);
        }
    }

    @Override
    public void listBillsByDueDate() {
        List<Bill> sortedBills = new ArrayList<>(bills);
        Collections.sort(sortedBills, Comparator.comparing(Bill::getDueDate));
        System.out.printf("%-15s %-15s %-15s %-15s %-15s %-15s\n", "Bill No.", "Type", "Amount", "Due Date", "State", "PROVIDER");
        for (Bill bill : bills) {
            if(bill.getState().equals(PaymentState.NOT_PAID.getValue())) {
                System.out.printf("%-15d %-15s %-15s %-15s %-15s %-15s%n",
                        bill.getBillNo(), bill.getType(), bill.getAmount(),
                        formatDate(bill.getDueDate()), bill.getState(), bill.getProvider());
            }
        }
    }

    @Override
    public void schedulePayment(String billId, String scheduledDateStr) {
        Bill bill = findBillById(billId);
        if (bill != null) {
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            dateFormat.setLenient(false);
            Date scheduledDate;
            try {
                scheduledDate = dateFormat.parse(scheduledDateStr);
                bill.setScheduledPaymentDate(scheduledDate);
                bill.setPaymentDate(scheduledDate);
                bill.setState(PaymentState.PENDING.getValue());
                System.out.println("Payment for bill id " + bill.getBillNo() + " is scheduled on " + scheduledDateStr);
            } catch (ParseException e) {
                System.out.println("Invalid scheduled date format. Please use dd/MM/yyyy format.");
            }

            // Check if current date is greater than the scheduled payment date
            Date currentDate = new Date();
            if (bill.getScheduledPaymentDate() != null && currentDate.after(bill.getScheduledPaymentDate())) {
                System.out.println("Automatic payment for scheduled bills");
                payBills(new String[]{billId});
            }
        } else {
            System.out.println("Sorry! Not found a bill with id " + billId);
        }
    }

    @Override
    public void listPayments() {
        int index = 0;
        System.out.printf("%-15s %-15s %-15s %-15s %-15s\n", "No.", "Amount", "Payment Date", "State", "Bill Id");
        for (Bill bill : bills) {
            if(!bill.getState().equals(PaymentState.NOT_PAID) && bill.getPaymentDate() != null) {
                index++;
                System.out.printf("%-15d %-15s %-15s %-15s %-15s",
                    index, bill.getAmount(), formatDate(bill.getPaymentDate()), bill.getState(), bill.getBillNo());
            }
        }
    }

    @Override
    public void searchBillsByProvider(String provider) {
        System.out.printf("%-15s %-15s %-15s %-15s %-15s %-15s\n", "Bill No.", "Type", "Amount", "Due Date", "State", "PROVIDER");
        for (Bill bill : bills) {
            if (bill.getProvider().equalsIgnoreCase(provider)) {
                System.out.printf("%-15d %-15s %-15s %-15s %-15s %-15s%n",
                        bill.getBillNo(), bill.getType(), bill.getAmount(),
                        formatDate(bill.getDueDate()), bill.getState(), bill.getProvider());
            }
        }
    }

    public static Bill findBillById(String billId) {
        for (Bill bill : bills) {
            if (bill.getBillNo() == Integer.parseInt(billId)) {
                return bill;
            }
        }
        return null;
    }

}
