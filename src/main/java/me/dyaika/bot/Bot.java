package me.dyaika.bot;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.dyaika.bot.commands.InviteCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Bot {

    /**
     * Ссылка на базу данных Firebase
     */
    private static DatabaseReference ref;

    /**
     * База данных из Firebase как локальный JSON
     */
    private static JsonObject guilds_json;

    /**
     * Главная функция, запускает бота.
     * Здесь приложение получает токены доступа и выбирает, какие команды будут доступны
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // Подключение Discord
        String token = null;
        try {
            File tokenFile = Paths.get("token.txt").toFile();
            if (!tokenFile.exists()) {
                System.out.println("[ERROR] Could not find token.txt file");
                System.out.print("Please paste in your bot token: ");
                Scanner s = new Scanner(System.in);
                token = s.nextLine();
                System.out.println();
                System.out.println("[INFO] Creating token.txt - please wait");
                if (!tokenFile.createNewFile()) {
                    System.out.println(
                            "[ERROR] Could not create token.txt - please create this file and paste in your token"
                                    + ".");
                    s.close();
                    return;
                }
                Files.write(tokenFile.toPath(), token.getBytes());
                s.close();
            }
            token = new String(Files.readAllBytes(tokenFile.toPath()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (token == null) return;
        JDABuilder builder = JDABuilder.createDefault(token);
        builder.enableIntents(GatewayIntent.MESSAGE_CONTENT); // enables explicit access to message.getContentDisplay()

        // Задает деятельность (например "играет в ЧтоТо-2)
        builder.setActivity(Activity.watching("Indian guy on YouTube"));

        JDA jda = builder.build();

        // Добавлять новые классы команд сюда
        jda.addEventListener(new InviteCommand());

        // Подключение Firebase
        FileInputStream serviceAccount =
                new FileInputStream("charlesbot-acd4c-firebase-adminsdk-j1kjh-61e7ced5ee.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://charlesbot-acd4c-default-rtdb.europe-west1.firebasedatabase.app")
                .build();

        FirebaseApp.initializeApp(options);
        ref = FirebaseDatabase.getInstance().getReference();
        ref.child("guilds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Преобразование строки в JsonObject
                Gson gson = new Gson();
                String json = gson.toJson(dataSnapshot.getValue());
                guilds_json = JsonParser.parseString(json).getAsJsonObject();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обработка ошибок
            }
        });
    }

    /**
     * Использовать в других классах для получения ссылки на базу данных.
     * Корнем является корень базы данных
     * @return Ссылка на базу данных
     */
    public static DatabaseReference getRef() {
        return ref;
    }

    /**
     * Использовать в других классах для получения базы данных.
     * Корнем является ключ guilds (серверы)
     * @return база данных guilds (серверы) как json
     */
    public static JsonObject getGuildsJson() {
        return guilds_json;
    }
}
