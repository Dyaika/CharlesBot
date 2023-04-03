package me.dyaika.bot.commands;

import me.dyaika.bot.Bot;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ModeratorCommands extends ListenerAdapter {

    /**
     * Здесь добавляются конкретные комманды с описанием и опциями
     * @param event Событие
     */
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        Bot.addSlashCommand(Commands.slash("ping", "Ping someone")
                .addOption(OptionType.USER, "user", "User you want to ping", false));
    }

    /**
     * Здесь прописывается логика самих комманд
     * @param event Событие - где произошло, кто вызвал и т.д.
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        switch (event.getName()){
            case "ping":
                event.deferReply().queue();
                OptionMapping pingOption = event.getOption("user");
                User user = event.getUser();
                User target = user;
                if (pingOption != null){
                    target = pingOption.getAsUser();
                }
                event.getHook().sendMessage(target.getAsMention() + ", тебя пингует " + user.getName()).queue();
                break;
            default:
                break;
        }
    }
}
