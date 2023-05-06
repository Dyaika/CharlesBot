package me.dyaika.bot.commands;

import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.dyaika.bot.Bot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WatchlistCommands extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        SlashCommandData command = Commands.slash("watchlist", "Выводит список просмотра");
        List<SubcommandData> subcommands = new ArrayList<>();
        subcommands.add(new SubcommandData("edit", "Изменяет видео в списке " +
                "просмотра, добавляет, если таковое отсутствует")
                .addOption(OptionType.STRING, "title", "Название видео", true)
                .addOption(OptionType.INTEGER, "episode", "Номер серии", false)
                .addOption(OptionType.STRING, "source", "Ссылка, где можно посмотреть", false));
        subcommands.add(new SubcommandData("remove", "Удаляет видео из списка просмотра")
                .addOption(OptionType.STRING, "title", "Название видео", true));
        subcommands.add(new SubcommandData("show", "Показывет список просмотра")
                .addOption(OptionType.STRING, "title", "Название конкретного видео", false));
        subcommands.add(new SubcommandData("clear", "Удаляет весь список просмотра текущего сервера"));
        command.addSubcommands(subcommands);

        Bot.addSlashCommand(command);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Gson gson = new Gson();
        switch (event.getName()){
            case "watchlist":
                event.deferReply().queue();
                StringBuilder answer;
                JsonObject guilds = Bot.getGuildsJson();
                String guild_id = event.getGuild().getId();
                String title;
                boolean isFound = false;
                // Безопасное чтение из json
                if (!guilds.has(guild_id)) {
                    guilds.add(guild_id, new JsonObject());
                }
                DatabaseReference watchlist_ref = Bot.getRef().child("guilds")
                        .child(guild_id)
                        .child("watchlist");

                // Создание списка просмотра в виде List<WatchlistItem>
                List<WatchlistItem> items = new ArrayList<>();
                JsonArray jsonElements = null;
                if (guilds.getAsJsonObject(guild_id).has("watchlist")){
                    jsonElements = guilds.getAsJsonObject(guild_id).get("watchlist").getAsJsonArray();
                } else {
                    guilds.getAsJsonObject(guild_id).add("watchlist", new JsonArray());
                }
                if (jsonElements != null && !jsonElements.isEmpty()){
                    for (JsonElement jsonElement :
                            jsonElements) {
                        WatchlistItem item = gson.fromJson(jsonElement, WatchlistItem.class);
                        items.add(item);
                    }
                }

                //обработка подкоманд
                switch (event.getSubcommandName()){
                    case "edit":
                        WatchlistItem video = new WatchlistItem(
                                event.getOption("title"),
                                event.getOption("episode"),
                                event.getOption("source"));
                        isFound = false;
                        answer = new StringBuilder();
                        for (int i = 0; i < items.size(); i++){
                            WatchlistItem item = items.get(i);

                            // Если нужно изменить элемент
                            if (item.getTitle().equalsIgnoreCase(video.getTitle())){
                                isFound = true;
                                if (Objects.equals(video.getSource(), WatchlistItem.EMPTY_SOURCE)){
                                    video.setSource(item.getSource());
                                }
                                if (video.getEpisode() == WatchlistItem.EMPTY_EPISODE){
                                    video.setEpisode(item.getEpisode());
                                }
                                watchlist_ref.child(i+"").setValueAsync(video);
                                guilds.getAsJsonObject(guild_id).getAsJsonArray("watchlist")
                                        .set(i, gson.toJsonTree(video));
                                answer.append("Обновленная информация для ").append(video.getTitle()).append(":\n");
                                answer.append(video.episodeRow()).append("\n");
                                answer.append(video.sourceRow());
                                break;
                            }

                        }
                        if (!isFound){

                            // Если нужно добавить новый элемент
                            watchlist_ref.child(items.size()+"").setValueAsync(video);
                            guilds.getAsJsonObject(guild_id).getAsJsonArray("watchlist")
                                    .add(gson.toJsonTree(video));
                            answer.append("Был добавлен новый элемент ").append(video.getTitle()).append(":\n");
                            answer.append(video.episodeRow()).append("\n");
                            answer.append(video.sourceRow());
                        }
                        event.getHook().sendMessage(answer.toString()).queue();
                        break;
                    case "remove":
                        title = event.getOption("title").getAsString();
                        isFound = false;
                        answer = new StringBuilder();
                        for (int i = 0; i < items.size(); i++){
                            WatchlistItem item = items.get(i);

                            // Если нашли
                            if (item.getTitle().equalsIgnoreCase(title)){
                                isFound = true;
                                watchlist_ref.child(i+"").removeValue(null);
                                guilds.getAsJsonObject(guild_id).getAsJsonArray("watchlist")
                                        .remove(i);
                                answer.append("Удалено ").append(title).append(" c позиции ").append(i).append("\n");
                                break;
                            }

                        }
                        if (!isFound){

                            // Если не нашли
                            answer.append(title).append(" и так нет в списке просмотра");
                        }
                        event.getHook().sendMessage(answer.toString()).queue();
                        break;
                    case "show":
                        if (items.isEmpty()){
                            event.getHook().sendMessage("Список этого сервера пуст").queue();
                        } else {
                            if (event.getOption("title") == null){

                                // Если хотят просто увидеть список
                                answer = new StringBuilder("Список этого сервера:\n");
                                for (WatchlistItem item:
                                     items) {
                                    answer.append(item.getTitle()).append("\n");
                                }
                                event.getHook().sendMessage(answer.toString()).queue();
                            } else {

                                // Если хотят увидеть информацию по конкретному названию
                                title = event.getOption("title").getAsString();
                                answer = new StringBuilder("Информация о ").append(title).append(":\n");
                                isFound = false;
                                for (WatchlistItem item:
                                     items) {
                                    if (item.getTitle().equalsIgnoreCase(title)){
                                        answer.append(item.episodeRow()).append("\n");
                                        answer.append(item.sourceRow());
                                        isFound = true;
                                        break;
                                    }
                                }
                                if (!isFound){
                                    answer.append("не найдено");
                                }
                                event.getHook().sendMessage(answer.toString()).queue();
                            }
                        }
                        break;
                    case "clear":
                        watchlist_ref.removeValue(null);
                        guilds.getAsJsonObject(guild_id).remove("watchlist");
                        event.getHook().sendMessage("Весь список просмотра был очищен").queue();
                        break;
                    default:
                        event.getHook().sendMessage("Что-то пошло не так, свяжитесь с разработчиком").queue();
                        break;
                }
                break;
            default:
                break;
        }
    }

    /**
     * Класс элемента списка просмотра.
     * Хранит информацию о видео
     */
    private static class WatchlistItem{
        public static final String EMPTY_TITLE = null;
        public static final String EMPTY_SOURCE = null;
        public static final int EMPTY_EPISODE = 0;
        private String title;
        private int episode;
        private String source;

        @Override
        public String toString() {
            return "WatchlistItem{" +
                    "title='" + title + '\'' +
                    ", episode=" + episode +
                    ", source='" + source + '\'' +
                    '}';
        }

        /**
         * Конструктор с безопасными параметрами.
         * @param title Название
         * @param episode Номер серии
         * @param source Ссылка для просмотра
         */
        public WatchlistItem(String title, Integer episode, String source) {
            setTitle(title);
            setEpisode(episode);
            setSource(source);
        }

        /**
         * Конструктор с безопасными параметрами-опциями
         * @param title Название
         * @param episode Номер серии
         * @param source Ссылка для просмотра
         */
        public WatchlistItem(OptionMapping title, OptionMapping episode, OptionMapping source) {
            if (title == null){
                this.title = EMPTY_TITLE;
            } else {
                this.title = title.getAsString();
            }
            if (episode == null){
                this.episode = EMPTY_EPISODE;
            } else {
                this.episode = episode.getAsInt();
            }
            if (source == null){
                this.source = EMPTY_SOURCE;
            } else {
                this.source = source.getAsString();
            }
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            if (title == null){
                this.title = EMPTY_SOURCE;
            } else {
                this.title = title;
            }
        }

        public int getEpisode() {
            return episode;
        }

        public void setEpisode(Integer episode) {
            if (episode == null){
                this.episode = EMPTY_EPISODE;
            } else {
                this.episode = episode;
            }
        }


        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            if (source == null){
                this.source = EMPTY_SOURCE;
            } else {
                this.source = source;
            }
        }

        /**
         * Для получения красивой информации об эпизоде
         * @return Возвращает красиво оформленную информацию об эпизоде
         */
        public String episodeRow(){
            StringBuilder res = new StringBuilder("эпизод: ");
            if (episode == EMPTY_EPISODE){
                res.append("не указан");
            } else {
                res.append(episode);
            }
            return res.toString();
        }

        /**
         * Для получения красивой информации об источнике
         * @return Возвращает красиво оформленную информацию об источнике
         */
        public String sourceRow(){
            StringBuilder res = new StringBuilder("где посмотреть: ");
            if (Objects.equals(source, EMPTY_SOURCE)){
                res.append("не указано");
            } else {
                res.append(source);
            }
            return res.toString();
        }
    }
}
