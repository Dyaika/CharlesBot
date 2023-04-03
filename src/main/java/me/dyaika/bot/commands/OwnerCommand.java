package me.dyaika.bot.commands;

import com.google.gson.JsonObject;
import me.dyaika.bot.Bot;
import me.dyaika.bot.Config;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class OwnerCommand extends ListenerAdapter {

    /**
     * Срабатывает при получении любого сообщения.
     * Название Invite не отображает суть, хотел одно - сделал другое, класс будет удален позднее.
     * @param event событие получения сообщения с важной информацией
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        // Проверка на бота
        if (event.getMember().getUser().isBot()){
            return;
        }

        // Выключение бота
        String prefix = Config.get("prefix");
        String raw = event.getMessage().getContentRaw();
        if (raw.equalsIgnoreCase(prefix + "shutdown") &&
                Bot.isOwner(event.getAuthor().getId())){
            System.out.println("Bot shutting down");
            event.getJDA().shutdown();
        }
        JsonObject guilds = Bot.getGuildsJson();

        // Подсчет количества сообщений на сервере
        // id для guild (сервера) где было получено сообщение
        String guild_id = event.getGuild().getId();

        // Безопасное чтение из json
        if (!guilds.has(guild_id)) {
            guilds.add(guild_id, new JsonObject());
        }
        long count = 0L;
        if (guilds.getAsJsonObject(guild_id).has("messages_count")){
            count = guilds.getAsJsonObject(guild_id).get("messages_count").getAsLong();
        }
        count++;

        // Запись значения в json и базу данных
        guilds.getAsJsonObject(guild_id).addProperty("messages_count", count);
        Bot.getRef().child("guilds")
                .child(guild_id)
                .child("messages_count")
                .setValueAsync(count);
        String answer = event.getMember().getEffectiveName() + ", сообщение #" + count + " " + raw;
        System.out.println(guild_id + ": " + count);
        //event.getChannel().sendMessage(answer).queue();
    }


}
