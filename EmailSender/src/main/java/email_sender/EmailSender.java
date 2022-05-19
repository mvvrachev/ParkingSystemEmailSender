package email_sender;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class EmailSender {

    public static void main(String[] args) {

        FileInputStream serviceAccount = null;
        try {
            serviceAccount = new FileInputStream("credentials.json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        FirebaseOptions options = null;
        try {
            options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FirebaseApp.initializeApp(options);

        Firestore db = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> future = db.collection("reservations").whereEqualTo("date", getTodayDate()).get();

        List<QueryDocumentSnapshot> documents = null;
        try {
            documents = future.get().getDocuments();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        StringBuilder reservations = new StringBuilder("Reservations for today: \n\n");
        for (QueryDocumentSnapshot document : documents) {
            reservations.append("Space: ").append(document.getData().get("space")).append("\n")
                    .append("Floor: ").append(document.getData().get("floor")).append("\n")
                    .append("Car Number: ").append(document.getData().get("carNumber")).append("\n")
                    .append("\n");
        }

        String to = "parkingsystem4@gmail.com";
        String from = "parkingsystem4@gmail.com";

        String host = "smtp.gmail.com";

        Properties properties = System.getProperties();

        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("parkingsystem4@gmail.com", "parkingsystem123");
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(from));

            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            message.setSubject("This is the Subject Line!");

            message.setText(String.valueOf(reservations));

            System.out.println("sending...");
            Transport.send(message);
            System.out.println("Sent email successfully!");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

    private static String getTodayDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        return formatter.format(calendar.getTime());
    }
}
