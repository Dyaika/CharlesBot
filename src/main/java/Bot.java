import commands.InviteCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class Bot {
    public static void main(String[] args) {
        JDABuilder builder = JDABuilder.createDefault("");

        // Set activity (like "playing Something")
        builder.setActivity(Activity.watching("Mr. Dyaika's youtube channel"));

        JDA jda = builder.build();
        jda.addEventListener(new InviteCommand());
    }
}
