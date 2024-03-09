package it.letscode.simappdevice;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MessagesQueue {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static volatile boolean isTaskScheduled = false;

    private static final SmsSender smsSender = new SmsSender();
    private static final ControllerHttpGateway controllerHttpGateway = new ControllerHttpGateway();

    private static List<MessageInQueue> messageInQueueList = new ArrayList<>();

    private static int queueLength = 0;

    public static void check() {
        if(queueLength > 0) {
            System.out.println("Pomijam sprwadzanie nowych wiadomosci w kolejce - aktualna kolejka nie jest pusta");
            return;
        };

        controllerHttpGateway.getSingleMessages();
    }

    public static void addMessagesToQueue(JSONArray messages, JSONObject meta) {
        System.out.println("Messages");
        System.out.println(messages);

        for (int i = 0; i < messages.length(); i++) {
            try {
                JSONObject messageJson = messages.getJSONObject(i);

                int messageId = messageJson.getInt("id");

                if(messageInQueueList.stream().anyMatch((message) -> message.getId() == messageId)) continue;

                MessageInQueue message = new MessageInQueue(
                        messageId,
                        messageJson.getString("text"),
                        messageJson.getString("phone_number")
                );
                messageInQueueList.add(message);

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("messageInQueueList length: " + messageInQueueList.size());

        if(queueLength == 0) {
            doWork();
        }
    }

    private static void doWork() {
        for(int i = 0; messageInQueueList.size() > 0 && i < 5; i++) {
            queueLength++;
            smsSender.sendSms(messageInQueueList.get(0));
            messageInQueueList.remove(0);
        }
    }

    public static void messageActionDone(int messageId) {

        //TODO: to nie bedzie wchodzic na nowszym androidzie > 9 wiec kolejka sie nigdy nie zwoilni..
        //trzeba to przemyslec. ;x

        queueLength--;
        if(queueLength <= 0) {
            try {
                Thread.sleep(20 * 1000);
            } catch (InterruptedException ignore) {
            } finally {
                check();
            }
        }
    }

    public static void checkMessagesQueueCrontabStart() {

        final Runnable task = MessagesQueue::check;

        checkMessagesQueueCrontabStop();

        scheduler.scheduleAtFixedRate(task, 0, 15, TimeUnit.MINUTES);
        isTaskScheduled = true;
    }


    public static void checkMessagesQueueCrontabStop() {
        if (isTaskScheduled) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
            isTaskScheduled = false;
        }
    }
}
