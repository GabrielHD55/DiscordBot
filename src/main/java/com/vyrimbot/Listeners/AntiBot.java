package com.vyrimbot.Listeners;

import com.vyrimbot.Main;
import com.vyrimbot.Utils.Captcha;
import com.vyrimbot.Utils.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.file.YamlFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AntiBot extends ListenerAdapter {

    private final Map<Long, Long> guild = new HashMap<>();
    private final Map<Long, String> captchas = new HashMap<>();

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        User user = event.getUser();

        YamlFile config = Main.getInstance().getConfig();
        YamlFile lang = Main.getInstance().getLang();

        if(config.getBoolean("Settings.Captcha", true)) {
            String captcha = Captcha.generateText();

            EmbedBuilder embed = EmbedUtil.getEmbed(user);
            embed.setColor(Color.getColor(lang.getString("Captcha.Color", "RED")));
            embed.setTitle(lang.getString("Captcha.Title"));

            StringBuilder description = new StringBuilder();
            for(String s : lang.getStringList("Captcha.Description")) {
                description.append(s).append("\n");
            }

            embed.setDescription(description.toString());

            PrivateChannel channel = user.openPrivateChannel().complete();

            channel.sendMessageEmbeds(embed.build()).queue();
            channel.sendFile(Captcha.generateImage(captcha), "captcha.png").complete();

            captchas.put(user.getIdLong(), captcha);
            guild.put(user.getIdLong(), event.getGuild().getIdLong());
        } else {
            Role role = event.getGuild().getRoleById(Long.parseLong(config.getString("DefaultRole")));

            event.getGuild().addRoleToMember(user.getIdLong(), role).queue();

            this.sendWelcomeBanner(user, config);
        }
    }

    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent event) {
        User user = event.getAuthor();

        YamlFile config = Main.getInstance().getConfig();
        YamlFile lang = Main.getInstance().getLang();

        if(captchas.containsKey(user.getIdLong())) {
            if(captchas.get(user.getIdLong()).equals(event.getMessage().getContentRaw())) {
                Guild guild = Main.getInstance().getJda().getGuildById(this.guild.get(user.getIdLong()));

                if(guild != null) {
                    Role role = guild.getRoleById(Long.parseLong(config.getString("DefaultRole")));

                    if(role != null) {
                        event.getChannel().sendMessage(":white_check_mark: **Captcha completed correctly!** :white_check_mark:").queue();

                        guild.addRoleToMember(user.getIdLong(), role).queue();

                        this.sendWelcomeBanner(user, config);

                        this.captchas.remove(user.getIdLong());
                        this.guild.remove(user.getIdLong());
                    } else {
                        Main.debug("ERROR", "Default role is null");
                    }
                }
            } else {
                event.getChannel().sendMessage(":x: **Incorrect captcha, try again** :x:").queue();
            }
        }
    }

    public void sendWelcomeBanner(User user, YamlFile config) {
        TextChannel channel = Main.getInstance().getJda().getTextChannelById(config.getString("WelcomeMessage.ChannelID"));

        /* Downloading user's Avatar */
        String avatar_url = user.getAvatarUrl();
        if(avatar_url==null)
        {
            avatar_url = user.getDefaultAvatarUrl();
        }

        try {
            File background = new File(Main.getInstance().getDataFolder(), config.getString("WelcomeMessage.File"));

            if(!background.exists()) return;

            /* Loading Welcome Banner */
            BufferedImage banner = ImageIO.read(background);
            URL url = new URL(avatar_url);
            /* We have to spoof a web browser or we'll get hit with 403 access denied messages */
            HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
            httpcon.addRequestProperty("User-Agent","Mozilla/4.0");
            InputStream input_stream = httpcon.getInputStream();
            BufferedImage img = ImageIO.read(input_stream);

            int w = config.getInt("WelcomeMessage.Avatar.Width");
            int h = config.getInt("WelcomeMessage.Avatar.Height");

            /* Creating our scaled avatar */
            BufferedImage scaled_avatar = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            // scales the input image to the output image
            Graphics2D g2d = scaled_avatar.createGraphics();

            g2d.setClip(new Ellipse2D.Float(0,0,w+1, h+1));
            g2d.drawImage(img, 0, 0, w+1, h+1, null);
            g2d.dispose();
            input_stream.close();


            /* Let's add the user's name to the image */
            Graphics graphics = banner.getGraphics();
            graphics.setColor(Color.getColor(config.getString("WelcomeMessage.TextColor", "WHITE")));

            Set<String> section = config.getConfigurationSection("WelcomeMessage.Text").getKeys(false);
            for(String sec : section) {
                int TEXT_X = config.getInt("WelcomeMessage.Text."+sec+".X");
                int TEXT_Y = config.getInt("WelcomeMessage.Text."+sec+".Y");

                String line = config.getString("WelcomeMessage.Text."+sec+".Line").replaceAll("%user-tag%", user.getName()+"#"+user.getDiscriminator());

                graphics.setFont(new Font(config.getString("WelcomeMessage.Text.Font"), Font.BOLD, config.getInt("WelcomeMessage.Text."+sec+".Size")));
                graphics.drawString(line, TEXT_X, TEXT_Y);
            }

            /* Let's add the user's avatar now */
            graphics.drawImage(scaled_avatar, config.getInt("WelcomeMessage.Avatar.X"), config.getInt("WelcomeMessage.Avatar.Y"), null);
            graphics.dispose();

            /* I really don't like the idea of writing to disk, so we're just going to convert this to a byte array and call it a night */
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(banner, "png",baos);
            byte[] banner_out = baos.toByteArray();
            baos.close();

            /* Sending our banner to the channel */
            String message = config.getString("WelcomeMessage.Message").replaceAll("%user-avatar%", user.getAsMention());

            channel.sendMessage(message).addFile(banner_out,"welcome.png").queue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
