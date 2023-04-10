package me.dyaika.bot.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.dyaika.bot.Bot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

public class WatchlistCommands extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        Bot.addSlashCommand(Commands.slash("watchlist", "Выводит список просмотра"));
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        switch (event.getName()){
            case "watchlist":
                event.deferReply().queue();
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
                break;
        }
    }
}
