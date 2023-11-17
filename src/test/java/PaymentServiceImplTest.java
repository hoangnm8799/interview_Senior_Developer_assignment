package test.java;

import constants.PaymentState;
import entity.Bill;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.PaymentServiceImpl;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PaymentServiceImplTest {

    private PaymentServiceImpl paymentService;

    @BeforeEach
    public void setup() {
        paymentService = new PaymentServiceImpl();
    }

    @Test
    public void testCashIn() {
        BigDecimal initialBalance = paymentService.getAvailableBalance();
        BigDecimal amount = new BigDecimal("100.00");
        paymentService.cashIn(amount);
        BigDecimal expectedBalance = initialBalance.add(amount);
        BigDecimal actualBalance = paymentService.getAvailableBalance();
        Assertions.assertEquals(expectedBalance, actualBalance);
    }

    @Test
    public void testCashIn2() {
        paymentService.createBill(1, "ELECTRIC", new BigDecimal(200000), new Date(120, 9, 25), "NOT_PAID", "EVN HCMC");
        paymentService.createBill(2, "WATER", new BigDecimal(175000), new Date(120, 9, 30), "NOT_PAID", "SAVACO HCMC");
        paymentService.createBill(3, "INTERNET", new BigDecimal(800000), new Date(120, 10, 30), "NOT_PAID", "VNPT");

        BigDecimal initialBalance = paymentService.getAvailableBalance();
        BigDecimal amount = new BigDecimal("1000000.00");
        paymentService.getBills().get(0).setScheduledPaymentDate( new Date("2023/01/01"));
        paymentService.cashIn(amount);
        BigDecimal expectedBalance = initialBalance.add(amount).subtract(paymentService.getBills().get(0).getAmount());
        BigDecimal actualBalance = paymentService.getAvailableBalance();
        String actualState = paymentService.getBills().get(0).getState();
        Assertions.assertEquals(expectedBalance, actualBalance);
        Assertions.assertEquals(PaymentState.PROCESSED.getValue(), actualState);
    }

    @Test
    public void testCreateBill() {
        int billNo = 1;
        String type = "Electricity";
        BigDecimal amount = new BigDecimal("50.00");
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date dueDate = null;
        try {
            dueDate = dateFormat.parse("01/01/2022");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String state = "Not Paid";
        String provider = "ABC Power";
        paymentService.createBill(billNo, type, amount, dueDate, state, provider);

        // Verify that the bill has been created
        Bill bill = paymentService.findBillById(String.valueOf(billNo));
        Assertions.assertNotNull(bill);
        Assertions.assertEquals(billNo, bill.getBillNo());
        Assertions.assertEquals(type, bill.getType());
        Assertions.assertEquals(amount, bill.getAmount());
        Assertions.assertEquals(dueDate, bill.getDueDate());
        Assertions.assertEquals(state, bill.getState());
        Assertions.assertEquals(provider, bill.getProvider());
    }

    @Test
    public void testPayBillsSufficientFunds() {
        // Create a bill with an amount less than the available balance
        int billNo = 1;
        String type = "Electricity";
        BigDecimal amount = new BigDecimal("50.00");
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date dueDate = null;
        try {
            dueDate = dateFormat.parse("01/01/2022");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String state = "Not Paid";
        String provider = "ABC Power";
        paymentService.createBill(billNo, type, amount, dueDate, state, provider);

        // Set the available balance to a value greater than the bill amount
        BigDecimal availableBalance = new BigDecimal("100.00");
        paymentService.setAvailableBalance(availableBalance);

        // Perform the payment
        paymentService.payBills(new String[]{String.valueOf(billNo)});

        // Verify that the bill has been paid and the balance has been updated
        Bill bill = paymentService.findBillById(String.valueOf(billNo));
        Assertions.assertNotNull(bill);
        Assertions.assertEquals(PaymentState.PROCESSED.getValue(), bill.getState());
        Assertions.assertNotNull(bill.getPaymentDate());
        Assertions.assertEquals(availableBalance.subtract(amount), paymentService.getAvailableBalance());
    }

    @Test
    public void testPayBillsInsufficientFunds() {
        // Create a bill with an amount greater than the available balance
        int billNo = 1;
        String type = "Electricity";
        BigDecimal amount = new BigDecimal("150.00");
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date dueDate = null;
        try {
            dueDate = dateFormat.parse("01/01/2022");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String state = "NOT_PAID";
        String provider = "ABC Power";
        paymentService.createBill(billNo, type, amount, dueDate, state, provider);

        // Set the available balance to a value less than the bill amount
        BigDecimal availableBalance = new BigDecimal("100.00");
        paymentService.setAvailableBalance(availableBalance);

        // Perform the payment
        paymentService.payBills(new String[]{String.valueOf(billNo)});

        // Verify that the bill has not been paid and the balance remains the same
        Bill bill = paymentService.findBillById(String.valueOf(billNo));
        Assertions.assertNotNull(bill);
        Assertions.assertEquals(PaymentState.NOT_PAID.getValue(), bill.getState());
        Assertions.assertNull(bill.getPaymentDate());
        Assertions.assertEquals(availableBalance, paymentService.getAvailableBalance());
    }

    @Test
    public void testPayMultiBills() {
        // Create a bills
        paymentService.createBill(1, "ELECTRIC", new BigDecimal(200000), new Date(120, 9, 25), "NOT_PAID", "EVN HCMC");
        paymentService.createBill(2, "WATER", new BigDecimal(175000), new Date(120, 9, 30), "NOT_PAID", "SAVACO HCMC");
        paymentService.createBill(3, "INTERNET", new BigDecimal(800000), new Date(120, 10, 30), "NOT_PAID", "VNPT");
        // Set the available balance to a value less than the bill amount
        BigDecimal availableBalance = new BigDecimal("12345600.00");
        paymentService.setAvailableBalance(availableBalance);
        // Perform the payment
        String []billIds = {"1", "2"};
        paymentService.payBills(billIds);
        // Verify that the bill has not been paid and the balance remains the same
        BigDecimal expectedBalance = availableBalance;
        for (String billNo : billIds) {
            Bill bill = paymentService.findBillById(billNo);
            expectedBalance = expectedBalance.subtract(bill.getAmount());
            Assertions.assertNotNull(bill);
            Assertions.assertEquals(PaymentState.PROCESSED.getValue(), bill.getState());
            Assertions.assertNotNull(bill.getPaymentDate());
        }
        Assertions.assertEquals(expectedBalance, paymentService.getAvailableBalance());
    }

    @Test
    public void testPayBills_BillNotFound() {
        // Create a bill with id "123"
        paymentService.createBill(123, "Electricity", new BigDecimal("50.00"),null, PaymentState.NOT_PAID.getValue(), null);

        // Perform payBills
        String output = captureOutput(() -> paymentService.payBills(new String[]{"456"}));
        Assertions.assertTrue(output.contains("Sorry! Not found a bill with such id."));
    }

    @Test
    public void testListBillsByDueDate_NoUnpaidBills() {
        // Create some bills with paid state
        paymentService.createBill(1, "Electricity", new BigDecimal("50.00"), new Date("01/01/2022"), PaymentState.PROCESSED.getValue(), "ABC Power");
        paymentService.createBill(2, "Water", new BigDecimal("30.00"), new Date("01/02/2022"), PaymentState.PROCESSED.getValue(), "XYZ Water");

        // Perform listBillsByDueDate
        String output = captureOutput(() -> paymentService.listBillsByDueDate());

        // Verify output does not contain any bill information
        Assertions.assertFalse(output.contains(PaymentState.NOT_PAID.getValue()));
    }

    @Test
    public void testListBillsByDueDate_WithUnpaidBills() {
        // Create some bills with unpaid state
        paymentService.createBill(1, "Electricity", new BigDecimal("50.00"), new Date("01/01/2022"), PaymentState.NOT_PAID.getValue(), "ABC Power");
        paymentService.createBill(2, "Water", new BigDecimal("30.00"), new Date("01/02/2022"), PaymentState.NOT_PAID.getValue(), "XYZ Water");

        // Perform listBillsByDueDate
        String output = captureOutput(() -> paymentService.listBillsByDueDate());

        // Verify output contains bill information for unpaid bills
        Assertions.assertTrue(output.contains("Bill No."));
        Assertions.assertTrue(output.contains("Electricity"));
        Assertions.assertTrue(output.contains("50.00"));
        Assertions.assertTrue(output.contains(PaymentState.NOT_PAID.getValue()));
        Assertions.assertTrue(output.contains("ABC Power"));
        Assertions.assertTrue(output.contains("Water"));
        Assertions.assertTrue(output.contains("30.00"));
        Assertions.assertTrue(output.contains(PaymentState.NOT_PAID.getValue()));
        Assertions.assertTrue(output.contains("XYZ Water"));
    }

    private String captureOutput(Runnable code) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        PrintStream originalOut = System.out;
        System.setOut(printStream);
        code.run();
        System.out.flush();
        System.setOut(originalOut);
        return outputStream.toString();
    }

    @Test
    public void testSchedulePayment_ValidScheduledDate() {
        // Create a bill with id "123"
        paymentService.createBill(123, "Electricity", new BigDecimal("50.00"), null, PaymentState.NOT_PAID.getValue(), null);

        // Mock System.out
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        // Perform schedulePayment with a valid scheduled date
        paymentService.schedulePayment("123", "01/12/2022");

        // Verify printed output
        String output = outputStream.toString();
        Assertions.assertTrue(output.contains("Payment for bill id 123 is scheduled on 01/12/2022"));

        // Verify bill state and payment date
        Bill bill = paymentService.getBills().get(0);
        Assertions.assertEquals(PaymentState.PENDING.getValue(), bill.getState());
        Assertions.assertEquals("01/12/2022", paymentService.formatDate(bill.getScheduledPaymentDate()));
        Assertions.assertEquals("01/12/2022", paymentService.formatDate(bill.getPaymentDate()));

        // Reset System.out
        System.setOut(originalOut);
    }

    @Test
    public void testSchedulePayment_InvalidScheduledDateFormat() {
        // Create a bill with id "123"
        paymentService.createBill(123, "Electricity", new BigDecimal("50.00"), null, PaymentState.NOT_PAID.getValue(), null);
        // Mock System.out
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        // Perform schedulePayment with an invalid scheduled date format
        paymentService.schedulePayment("123", "12-01-2022");

        // Verify printed output
        String output = outputStream.toString();
        Assertions.assertTrue(output.contains("Invalid scheduled date format. Please use dd/MM/yyyy format."));

        // Verify bill state and payment date remain unchanged
        Bill bill = paymentService.getBills().get(0);
        Assertions.assertNull(bill.getScheduledPaymentDate());
        Assertions.assertNull(bill.getPaymentDate());

        // Reset System.out
        System.setOut(originalOut);
    }

    @Test
    public void testSchedulePayment_BillNotFound() {
        paymentService.createBill(123, "Electricity", new BigDecimal("50.00"), null, PaymentState.NOT_PAID.getValue(), null);

        // Mock System.out
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        // Perform schedulePayment with a non-existent bill id
        paymentService.schedulePayment("456", "01/12/2022");

        // Verify printed output
        String output = outputStream.toString();
        Assertions.assertTrue(output.contains("Sorry! Not found a bill with id 456"));

        // Reset System.out
        System.setOut(originalOut);
    }

    @Test
    public void testSchedulePayment_AutomaticPaymentTriggered() {
        // Create a bill with id "123"
        paymentService.createBill(123, "Electricity", new BigDecimal("50.00"), null, PaymentState.NOT_PAID.getValue(), null);
        Bill bill = paymentService.getBills().get(0);

        // Set the scheduled payment date to a past date
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date pastDate = dateFormat.parse("01/01/2020");
            bill.setScheduledPaymentDate(pastDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Mock System.out
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        // Perform schedulePayment
        paymentService.schedulePayment("123", "01/12/2022");

        // Verify printed output
        String output = outputStream.toString();
        Assertions.assertTrue(output.contains("Automatic payment for scheduled bills"));

        // Reset System.out
        System.setOut(originalOut);
    }


    @Test
    public void testListBills_NoBills() {
        // Mock System.out
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        // Perform listBills with no bills
        paymentService.listBills();

        // Verify printed output
        String output = outputStream.toString();
        Assertions.assertFalse(output.contains("Bill No.       Type            Amount          Due Date        State           PROVIDER        "));

        // Reset System.out
        System.setOut(originalOut);
    }

    @Test
    public void testListBills_WithBills() {
        // Create two bills
        paymentService.createBill(1, "Electricity", new BigDecimal("50.00"), new Date(), PaymentState.NOT_PAID.getValue(), "Provider A");
        paymentService.createBill(2, "Water", new BigDecimal("30.00"), new Date(), PaymentState.PROCESSED.getValue(), "Provider B");

        // Mock System.out
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        // Perform listBills
        paymentService.listBills();

        // Verify printed output
        String output = outputStream.toString();
        Assertions.assertTrue(output.contains("1               Electricity     50.00           "));
        Assertions.assertTrue(output.contains("2               Water           30.00           "));

        // Reset System.out
        System.setOut(originalOut);
    }

    @Test
    public void testListPayments_NoPayments() {
        // Mock System.out
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        // Perform listPayments with no payments
        paymentService.listPayments();

        // Verify printed output
        String output = outputStream.toString();
        Assertions.assertFalse(output.contains("No.            Amount          Payment Date    State           Bill Id         "));

        // Reset System.out
        System.setOut(originalOut);
    }

    @Test
    public void testListPayments_WithPayments() {
        // Create two bills with payments
        paymentService.createBill(1, "Electricity", new BigDecimal("50.00"), new Date(), PaymentState.PROCESSED.getValue(), "Provider A");
        paymentService.createBill(2, "Water", new BigDecimal("30.00"), new Date(), PaymentState.PROCESSED.getValue(), "Provider B");

        // Mock System.out
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        // Perform listPayments
        paymentService.listPayments();
    }


    @Test
    public void testSearchBillsByProvider_NoMatchingBills() {
        // Mock System.out
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        // Perform searchBillsByProvider with no matching bills
        paymentService.searchBillsByProvider("Provider A");

        // Verify printed output
        String output = outputStream.toString();
        Assertions.assertFalse(output.contains("Bill No.       Type            Amount          Due Date        State           PROVIDER        "));

        // Reset System.out
        System.setOut(originalOut);
    }

    @Test
    public void testSearchBillsByProvider_WithMatchingBills() {
        // Create two bills with matching provider
        paymentService.createBill(1, "Electricity", new BigDecimal("50.00"), new Date(), PaymentState.NOT_PAID.getValue(), "Provider A");
        paymentService.createBill(2, "Water", new BigDecimal("30.00"), new Date(), PaymentState.PROCESSED.getValue(), "Provider A");

        // Mock System.out
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        // Perform searchBillsByProvider
        paymentService.searchBillsByProvider("Provider A");

        // Verify printed output
        String output = outputStream.toString();
        Assertions.assertTrue(output.contains("1               Electricity     50.00           "));
        Assertions.assertTrue(output.contains("2               Water           30.00           "));

        // Reset System.out
        System.setOut(originalOut);
    }

}