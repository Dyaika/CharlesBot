import commands.InviteCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class Bot {
    public static void main(String[] args) {
        JDABuilder builder = JDABuilder.createDefault("MTA4NTE2NTUwMzIyOTc0MzExNA.GMeR-2.Btd_NG41KefaplOQSvQbYxYnDEqbsYUSqcQOxo");

        // Set activity (like "playing Something")
        builder.setActivity(Activity.watching("Mr. Dyaika's youtube channel"));

        JDA jda = builder.build();
        jda.addEventListener(new InviteCommand());
    }
}
