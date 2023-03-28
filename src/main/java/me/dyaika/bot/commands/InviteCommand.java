package me.dyaika.bot.commands;

import com.google.firebase.database.*;
import com.google.gson.JsonObject;
import me.dyaika.bot.Bot;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class InviteCommand extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMember().getUser().isBot()){
            return;
        }
        JsonObject guilds = Bot.getGuildsJson();

        //id of guild where message were received
        String guild_id = event.getGuild().getId();

        //safe value reading from json
        if (!guilds.has(guild_id)) {
            guilds.add(guild_id, new JsonObject());
        }
        long count = 0L;
        if (guilds.getAsJsonObject(guild_id).has("messages_count")){
            count = guilds.getAsJsonObject(guild_id).get("messages_count").getAsLong();
        }
        count++;

        //value writing to json and database
        guilds.getAsJsonObject(guild_id).addProperty("messages_count", count);
        Bot.getRef().child("guilds")
                .child(guild_id)
                .child("messages_count")
                .setValueAsync(count);
        String answer = event.getMember().getEffectiveName() + ", your message #" + count + " "
                + event.getMessage().getContentDisplay();
        System.out.println(event.getGuild().getId() + ": " + count);
        event.getChannel().sendMessage(answer).queue();
    }

}
