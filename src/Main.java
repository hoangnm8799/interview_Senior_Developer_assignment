import service.PaymentService;
import service.PaymentServiceImpl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("You can interact with solution via a simple set of commands :\nCASH_IN + Amount\nLIST_BILL\nPAY + Bill Ids (BillNo)\nDUE_DATE\nSCHEDULE + Bill Id + Date dd/MM/yyyy\nLIST_PAYMENT\nSEARCH_BILL_BY_PROVIDER + Provider name\nEXIT");
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        PaymentService paymentService = new PaymentServiceImpl();
        paymentService.createBill(1, "ELECTRIC", new BigDecimal(200000), new Date(120, 9, 25), "NOT_PAID", "EVN HCMC");
        paymentService.createBill(2, "WATER", new BigDecimal(175000), new Date(120, 9, 30), "NOT_PAID", "SAVACO HCMC");
        paymentService.createBill(3, "INTERNET", new BigDecimal(800000), new Date(120, 10, 30), "NOT_PAID", "VNPT");
        while (!exit) {
            System.out.print("$ ");
            String command = scanner.nextLine().trim();
            String[] tokens = command.split(" ");

            switch (tokens[0].toUpperCase()) {
                case "CASH_IN":
                    if (tokens.length < 2) {
                        System.out.println("Invalid command. Please specify the amount.");
                    } else {
                        BigDecimal amount = new BigDecimal(tokens[1]);
                        paymentService.cashIn(amount);
                    }
                    break;
                case "LIST_BILL":
                    paymentService.listBills();
                    break;
                case "PAY":
                    if (tokens.length < 2) {
                        System.out.println("Invalid command. Please specify the bill ID(s) to pay.");
                    } else {
                        String[] billIds = Arrays.copyOfRange(tokens, 1, tokens.length);
                        paymentService.payBills(billIds);
                    }
                    break;
                case "DUE_DATE":
                    paymentService.listBillsByDueDate();
                    break;
                case "SCHEDULE":
                    if (tokens.length < 3) {
                        System.out.println("Invalid command. Please specify the bill ID and scheduled date.");
                    } else {
                        String billId = tokens[1];
                        String scheduledDateStr = tokens[2];
                        paymentService.schedulePayment(billId, scheduledDateStr);
                    }
                    break;
                case "LIST_PAYMENT":
                    paymentService.listPayments();
                    break;
                case "SEARCH_BILL_BY_PROVIDER":
                    if (tokens.length < 2) {
                        System.out.println("Invalid command. Please specify the provider name.");
                    } else {
                        String provider = tokens[1];
                        paymentService.searchBillsByProvider(provider);
                    }
                    break;
                case "EXIT":
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid command. Please try again.");
                    break;
            }
        }
    }
}