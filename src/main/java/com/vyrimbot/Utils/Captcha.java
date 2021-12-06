package com.vyrimbot.Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.UUID;

public class Captcha {

    public static String generateText() {
        return new StringTokenizer(UUID.randomUUID().toString(), "-").nextToken();
    }

    public static byte[] generateImage(String text) {
        int w = 190, h = 50;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Color.white);
        g.fillRect(0, 0, w, h);
        g.setFont(new Font("Serif", Font.BOLD, 42));
        g.setColor(Color.blue);
        int start = 10;
        byte[] bytes = text.getBytes();

        Random random = new Random();
        for (int i = 0; i < bytes.length; i++) {
            g.setColor( new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            g.drawString(new String(new byte[]{bytes[i]}), start + (i * 20), (int) (Math.random() * 20 + 30));
        }
        g.setColor(Color.white);
        for (int i = 0; i < 25; i++) {
            g.drawOval((int) (Math.random() * 160), (int) (Math.random() * 10), 31, 31);
        }
        g.dispose();

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", bout);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        byte[] captcha = bout.toByteArray();
        try {
            bout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return captcha;
    }
}