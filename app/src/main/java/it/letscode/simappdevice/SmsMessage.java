package it.letscode.simappdevice;

public class SmsMessage {
    private String senderNumber;
    private String messageBody;
    private String formattedMessage;

    private Boolean isIncoming;
    private String type;

    public SmsMessage(String senderNumber, String messageBody, String formattedMessage, Boolean isIncoming) {
        this.senderNumber = senderNumber;
        this.messageBody = messageBody;
        this.formattedMessage = formattedMessage;
        this.type = isIncoming ? "Incoming" : "Outgoing";
    }

    public String getSenderNumber() {
        return senderNumber;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public String getFormattedMessage() {
        return formattedMessage;
    }

    public String getType() {
        return type;
    }
}
