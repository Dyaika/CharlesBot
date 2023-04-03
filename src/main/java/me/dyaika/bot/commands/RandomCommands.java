package me.dyaika.bot.commands;

import me.dyaika.bot.Bot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomCommands extends ListenerAdapter {
    /**
     * Здесь добавляются конкретные комманды рандома с описанием и опциями
     * @param event Событие
     */
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        Bot.addSlashCommand(Commands.slash("dice", "Кинуть кубик (случайное целое)")
                .addOption(OptionType.INTEGER, "max", "Максимальное целое", false)
                .addOption(OptionType.INTEGER, "min", "Минимальное целое", false));

    }

    /**
     * Здесь прописывается логика самих комманд рандома
     * @param event Событие - где произошло, кто вызвал и т.д.
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        switch (event.getName()){
            case "dice":
                event.deferReply().queue();
                OptionMapping maxOption = event.getOption("max");
                int max = 6;
                OptionMapping minOption = event.getOption("min");
                int min = 1;
                if (maxOption != null){
                    max = maxOption.getAsInt();
                }
                if (minOption != null){
                    min = minOption.getAsInt();
                }
                if (max < min){
                    event.getHook().sendMessage("Максимальное число не может " +
                            "быть меньше минимального!").queue();
                } else {
                    int answer = new Random().nextInt(max - min + 1) + min;
                    event.getHook().sendMessage(event.getUser().getName() +
                            ", тебе выпало " + answer + "!").queue();
                }
                break;
            default:
                break;
        }
    }
}
