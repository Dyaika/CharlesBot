package me.dyaika.bot.commands;

import com.google.gson.JsonArray;
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
        command.addSubcommands(subcommands);

        Bot.addSlashCommand(command);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        switch (event.getName()){
            case "watchlist":
                event.deferReply().queue();
                switch (event.getSubcommandName()){
                    case "edit":
                        WatchlistItem video = new WatchlistItem(
                                event.getOption("title"),
                                event.getOption("episode"),
                                event.getOption("source")
                        );
                        event.getHook().sendMessage("Попытка создания/изменения " + video).queue();
                        break;
                    case "remove":
                        event.getHook().sendMessage("Попытка удаления "
                                + event.getOption("title").getAsString()).queue();
                        break;
                    case "show":
                        JsonObject guilds = Bot.getGuildsJson();
                        String guild_id = event.getGuild().getId();

                        // Безопасное чтение из json
                        if (!guilds.has(guild_id)) {
                            guilds.add(guild_id, new JsonObject());
                        }
                        JsonArray jsonElements = null;
                        if (guilds.getAsJsonObject(guild_id).has("watchlist")){
                            jsonElements = guilds.getAsJsonObject(guild_id).get("watchlist").getAsJsonArray();
                        }
                        if (jsonElements == null || jsonElements.isEmpty()){
                            event.getHook().sendMessage("Список этого сервера пуст").queue();
                        } else {
                            StringBuilder answer = new StringBuilder("Список этого сервера:\n");
                            for (int i = 0; i < jsonElements.size(); i++){
                                answer.append(i).append(": ")
                                        .append(jsonElements.get(i).getAsJsonObject().get("name").getAsString())
                                        .append("\n");
                            }
                            event.getHook().sendMessage(answer.toString()).queue();
                        }
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
        private String title;
        private int episode;
        private String source;

        @Override
        public String toString() {
            return "WatchlistItem{" +
                    "title='" + title + '\'' +
                    ", episode=" + episode +
                    ", link='" + source + '\'' +
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
         * Конструктор с безопасными параметрами.
         * @param title Название
         * @param episode Номер серии
         * @param source Ссылка для просмотра
         */
        public WatchlistItem(OptionMapping title, OptionMapping episode, OptionMapping source) {
            setTitle(title);
            setEpisode(episode);
            setLink(source);
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = Objects.requireNonNullElse(title, "empty");
        }
        public void setTitle(OptionMapping title) {
            if (title != null){
                this.title = title.getAsString();
            } else {
                this.title = "empty";
            }
        }

        public int getEpisode() {
            return episode;
        }

        public void setEpisode(Integer episode) {
            this.episode = Objects.requireNonNullElse(episode, 1);
        }

        public void setEpisode(OptionMapping episode) {
            if (episode != null){
                this.episode = episode.getAsInt();
            } else {
                this.episode = 1;
            }
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {

            this.source = Objects.requireNonNullElse(source, "empty");
        }

        public void setLink(OptionMapping link) {
            if (link != null){
                this.source = link.getAsString();
            } else {
                this.source = "empty";
            }
        }


    }
}
