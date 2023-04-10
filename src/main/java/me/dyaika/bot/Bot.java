package me.dyaika.bot;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.dyaika.bot.commands.ModeratorCommands;
import me.dyaika.bot.commands.OwnerCommand;
import me.dyaika.bot.commands.RandomCommands;
import me.dyaika.bot.commands.WatchlistCommands;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
     * Основные команды бота
     */
    private static List<SlashCommandData> slashCommands = new ArrayList<>();

    /**
     * Главная функция, запускает бота.
     * Здесь приложение получает токены доступа и выбирает, какие команды будут доступны
     * @param args Не используются
     * @throws IOException Ошибки при чтении файлов и тд
     */
    public static void main(String[] args) throws IOException {
        // Подключение Discord
        JDABuilder builder = JDABuilder.createDefault(Config.get("token"));
        builder.enableIntents(GatewayIntent.MESSAGE_CONTENT); // enables explicit access to message.getContentDisplay()

        // Задает деятельность (например "играет в ЧтоТо-2)
        builder.setActivity(Activity.watching("Indian guy on YouTube"));

        JDA jda = builder.build();

        // Добавлять новые классы команд сюда
        jda.addEventListener(
                new OwnerCommand(),
                new ModeratorCommands(),
                new RandomCommands(),
                new WatchlistCommands());

        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Добавляем все команды, описанные в классах выше
        jda.updateCommands().addCommands(slashCommands).queue();

        // Подключение Firebase
        FileInputStream serviceAccount =
                new FileInputStream(Config.get("firebase_key"));

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl(Config.get("firebase_url"))
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

    /**
     * Проверка пользователя по id на владельца
     * @param id Идентификатор проверяемого пользователя
     * @return является ли пользователь владельцем
     */
    public static boolean isOwner(String id){
        List<String> owners_id = List.of(Config.get("owners_id").split(" "));
        for (String owner_id:
             owners_id) {
            if (id.equals(owner_id)){
                return true;
            }
        }
        return false;
    }

    /**
     * Slash-команды списком добавляются в onReady() через этот метод
     * @param commands Список данных о командах
     */
    public static void addSlashCommands(List<SlashCommandData> commands){
        slashCommands.addAll(commands);
    }

    /**
     * Slash-команда (одна) добавляется в onReady() через этот метод
     * @param command Данные о команде
     */
    public static void addSlashCommand(SlashCommandData command){
        slashCommands.add(command);
    }
}
