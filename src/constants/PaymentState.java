package constants;

public enum PaymentState {
    NOT_PAID("NOT_PAID"),
    PROCESSED("PROCESSED"),
    PENDING("PENDING"),;

    String value;

    PaymentState(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}