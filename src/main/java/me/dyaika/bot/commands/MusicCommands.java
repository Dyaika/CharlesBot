package me.dyaika.bot.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.dyaika.bot.Bot;
import me.dyaika.bot.lavaplayer.GuildMusicManager;
import me.dyaika.bot.lavaplayer.PlayerManager;
import me.dyaika.bot.lavaplayer.TrackScheduler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MusicCommands extends ListenerAdapter
{

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        List<SlashCommandData> SlashCommandData = new ArrayList<>();
        SlashCommandData.add(Commands.slash("play", "Проигрывает ваш трек")
                .addOption(OptionType.STRING, "url", "Ссылка на трек (YouTube)", true));
        SlashCommandData.add(Commands.slash("skip", "Пропускает текущий трек"));
        SlashCommandData.add(Commands.slash("stop", "Останавливает воспроизведение трека"));
        SlashCommandData.add(Commands.slash("now", "Отправляет информацию о текущем треке"));
        SlashCommandData.add(Commands.slash("queue", "Отправляет текущую очередь треков"));
        SlashCommandData.add(Commands.slash("repeat", "Включает/отключает повторение текущего трека"));
        SlashCommandData.add(Commands.slash("volume", "Позволяет изменить гроскость")
                .addOption(OptionType.INTEGER, "vol", "Значение громкости", true));
        Bot.addSlashCommands(SlashCommandData);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        switch (event.getName()){
            case "play":
            {
                Member member = event.getMember();
                GuildVoiceState memberVoiceState = member.getVoiceState();

                if (!memberVoiceState.inAudioChannel())
                {
                    event.reply("Ты должен быть в голосовом канале").queue();
                    return;
                }

                Member self = event.getGuild().getSelfMember();
                GuildVoiceState selfVoiceState = self.getVoiceState();


                if (!selfVoiceState.inAudioChannel())
                {
                    event.getGuild().getAudioManager().openAudioConnection(memberVoiceState.getChannel());
                }
                else
                {
                    if (selfVoiceState.getChannel() != memberVoiceState.getChannel())
                    {
                        event.reply("Ты должен быть в одном канале со мной").queue();
                        return;
                    }
                }

                String name = event.getOption("url").getAsString();
                Pattern pattern = Pattern.compile("^((ftp|http|https):\\/\\/)?(www\\.)?([A-Za-zА-Яа-я0-9]{1}[A-Za-zА-Яа-я0-9\\-]*\\.?)*\\.{1}[A-Za-zА-Яа-я0-9-]{2,8}(\\/([\\w#!:.?+=&%@!\\-\\/])*)?");
                if (!pattern.matcher(name).matches())
                {
                    name = "ytsearch:" + name;
                }
                PlayerManager playerManager = PlayerManager.get();
                event.reply("Играет " + name).queue();
                playerManager.play(event.getGuild(), name);
                break;
            }
            case "skip":
            {
                Member member = event.getMember();
                GuildVoiceState memberVoiceState = member.getVoiceState();

                if (!memberVoiceState.inAudioChannel())
                {
                    event.reply("Ты должен быть в голосовом канале").queue();
                    return;
                }

                Member self = event.getGuild().getSelfMember();
                GuildVoiceState selfVoiceState = self.getVoiceState();

                if (!selfVoiceState.inAudioChannel())
                {
                    event.reply("Я не нахожусь в аудио канале").queue();
                    return;
                }

                if (selfVoiceState.getChannel() != memberVoiceState.getChannel())
                {
                    event.reply("Ты должен быть в одном канале со мной").queue();
                    return;
                }

                GuildMusicManager guildMusicManager = PlayerManager.get().getGuildMusicManager(event.getGuild());
                guildMusicManager.getTrackScheduler().getPlayer().stopTrack();
                event.reply("Трек пропущен").queue();
                break;
            }
            case "stop":
            {
                Member member = event.getMember();
                GuildVoiceState memberVoiceState = member.getVoiceState();

                if (!memberVoiceState.inAudioChannel())
                {
                    event.reply("Ты должен быть в голосовом канале").queue();
                    return;
                }

                Member self = event.getGuild().getSelfMember();
                GuildVoiceState selfVoiceState = self.getVoiceState();

                if (!selfVoiceState.inAudioChannel())
                {
                    event.reply("Я не нахожусь в аудио канале").queue();
                    return;
                }

                if (selfVoiceState.getChannel() != memberVoiceState.getChannel())
                {
                    event.reply("Ты должен быть в одном канале со мной").queue();
                    return;
                }

                GuildMusicManager guildMusicManager = PlayerManager.get().getGuildMusicManager(event.getGuild());
                TrackScheduler trackScheduler = guildMusicManager.getTrackScheduler();
                trackScheduler.getQueue().clear();
                trackScheduler.getPlayer().stopTrack();
                event.reply("Воспроизведение остоновлено").queue();
                break;
            }
            case "now":
            {
                Member member = event.getMember();
                GuildVoiceState memberVoiceState = member.getVoiceState();

                if (!memberVoiceState.inAudioChannel())
                {
                    event.reply("Ты должен быть в голосовом канале").queue();
                    return;
                }

                Member self = event.getGuild().getSelfMember();
                GuildVoiceState selfVoiceState = self.getVoiceState();

                if (!selfVoiceState.inAudioChannel())
                {
                    event.reply("Я не нахожусь в аудио канале").queue();
                    return;
                }

                if (selfVoiceState.getChannel() != memberVoiceState.getChannel())
                {
                    event.reply("Ты должен быть в одном канале со мной").queue();
                    return;
                }

                GuildMusicManager guildMusicManager = PlayerManager.get().getGuildMusicManager(event.getGuild());
                if (guildMusicManager.getTrackScheduler().getPlayer().getPlayingTrack() == null)
                {
                    event.reply("Сейчас ничего не играет").queue();
                    return;
                }
                AudioTrackInfo info = guildMusicManager.getTrackScheduler().getPlayer().getPlayingTrack().getInfo();
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("Сейчас играет:");
                builder.setDescription("**Название: **" + info.title);
                builder.appendDescription("\n**Автор: **" + info.author);
                event.replyEmbeds(builder.build()).queue();
                break;
            }
            case "queue":
            {
                Member member = event.getMember();
                GuildVoiceState memberVoiceState = member.getVoiceState();

                if (!memberVoiceState.inAudioChannel())
                {
                    event.reply("Ты должен быть в голосовом канале").queue();
                    return;
                }

                Member self = event.getGuild().getSelfMember();
                GuildVoiceState selfVoiceState = self.getVoiceState();

                if (!selfVoiceState.inAudioChannel())
                {
                    event.reply("Я не нахожусь в аудио канале").queue();
                    return;
                }

                if (selfVoiceState.getChannel() != memberVoiceState.getChannel())
                {
                    event.reply("Ты должен быть в одном канале со мной").queue();
                    return;
                }

                GuildMusicManager guildMusicManager = PlayerManager.get().getGuildMusicManager(event.getGuild());
                List<AudioTrack> queue = new ArrayList<>(guildMusicManager.getTrackScheduler().getQueue());
                if (queue.isEmpty())
                {
                    event.reply("Очередь пуста :(").queue();
                    return;
                }
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("Текущая очередь");
                for (int i = 0; i < queue.size(); i++)
                {
                    AudioTrackInfo info = queue.get(i).getInfo();
                    builder.addField(i + 1 + ":", info.title, false);
                }
                event.replyEmbeds(builder.build()).queue();
                break;
            }
            case "repeat":
            {
                Member member = event.getMember();
                GuildVoiceState memberVoiceState = member.getVoiceState();

                if (!memberVoiceState.inAudioChannel())
                {
                    event.reply("Ты должен быть в голосовом канале").queue();
                    return;
                }

                Member self = event.getGuild().getSelfMember();
                GuildVoiceState selfVoiceState = self.getVoiceState();

                if (!selfVoiceState.inAudioChannel())
                {
                    event.reply("Я не нахожусь в аудио канале").queue();
                    return;
                }

                if (selfVoiceState.getChannel() != memberVoiceState.getChannel())
                {
                    event.reply("Ты должен быть в одном канале со мной").queue();
                    return;
                }

                GuildMusicManager guildMusicManager = PlayerManager.get().getGuildMusicManager(event.getGuild());
                boolean repeat = guildMusicManager.getTrackScheduler().isRepeat();
                AudioTrack playingTrack = guildMusicManager.getTrackScheduler().getPlayer().getPlayingTrack();
                if (playingTrack == null)
                {
                    event.reply("Сейчас ничего не играет").queue();
                    return;
                }
                if (repeat)
                {
                    event.reply("Трек " + playingTrack.getInfo().title + " теперь не повторяется").queue();
                }
                else
                {
                    event.reply("Трек " + playingTrack.getInfo().title + " теперь повторяется").queue();
                }
                guildMusicManager.getTrackScheduler().setRepeat(!repeat);
                break;
            }
            case "volume":
            {
                Member member = event.getMember();
                GuildVoiceState memberVoiceState = member.getVoiceState();

                if (!memberVoiceState.inAudioChannel())
                {
                    event.reply("Ты должен быть в голосовом канале").queue();
                    return;
                }

                Member self = event.getGuild().getSelfMember();
                GuildVoiceState selfVoiceState = self.getVoiceState();

                if (!selfVoiceState.inAudioChannel())
                {
                    event.reply("Я не нахожусь в аудио канале").queue();
                    return;
                }

                if (selfVoiceState.getChannel() != memberVoiceState.getChannel())
                {
                    event.reply("Ты должен быть в одном канале со мной").queue();
                    return;
                }

                GuildMusicManager guildMusicManager = PlayerManager.get().getGuildMusicManager(event.getGuild());
                if (guildMusicManager.getTrackScheduler().getPlayer().getPlayingTrack() == null)
                {
                    event.reply("Сейчас ничего не играет").queue();
                    return;
                }
                guildMusicManager.getTrackScheduler().getPlayer().setVolume(event.getOption("vol").getAsInt());
                event.reply("Текущая громкость: " + guildMusicManager.getTrackScheduler().getPlayer().getVolume()).queue();
                break;
            }
            default:
                break;
        }
    }
}
