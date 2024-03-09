package it.letscode.simappdevice;

public class MessageInQueue {
    private int id;
    private String text;
    private String phoneNumber;

    // Konstruktor
    public MessageInQueue(int id, String text, String phoneNumber) {
        this.id = id;
        this.text = text;
        this.phoneNumber = phoneNumber;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
