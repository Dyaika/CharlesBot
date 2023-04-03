package me.dyaika.bot.commands;

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

public class ModeratorCommands extends ListenerAdapter {
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        List<SlashCommandData> SlashCommandData = new ArrayList<>();
        SlashCommandData.add(Commands.slash("ping", "Ping someone")
                .addOption(OptionType.USER, "user", "User you want to ping", false));
        event.getJDA().updateCommands().addCommands(SlashCommandData).queue();
    }

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
