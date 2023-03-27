package me.dyaika.bot.commands;

import com.google.firebase.database.*;
import me.dyaika.bot.Bot;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class InviteCommand extends ListenerAdapter {

    private Long count = 0L;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMember().getUser().isBot()){
            return;
        }

        Bot.getRef().child(event.getGuild().getId()).child("messages_count").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Long value = mutableData.getValue(Long.class);
                if (value == null) {
                    value = 0L;
                }
                count = value + 1L;
                mutableData.setValue(value + 1L);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    // errors handler
                } else {
                    // value changed successfully
                }
            }
        });
        String answer = event.getMember().getEffectiveName() + ", your message #" + count + " "
                + event.getMessage().getContentDisplay();
        System.out.println(event.getGuild().getId() + ": " + count);
        event.getChannel().sendMessage(answer).queue();
    }

}
