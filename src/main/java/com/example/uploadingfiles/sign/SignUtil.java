package com.example.uploadingfiles.sign;

import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;

@Service
public class SignUtil implements ISignUtil {

    @Override
    public byte[] drawText(Path inAbsolutePath, Config config, String str) throws IOException, FontFormatException {
        FileInputStream input = new FileInputStream(inAbsolutePath.toFile());
        return drawText(input, config, str);
    }

    @Override
    public byte[] drawText(InputStream input, Config config, String str) throws IOException, FontFormatException {
        BufferedImage localBufferedImage = ImageIO.read(input);
        Graphics localGraphics = localBufferedImage.getGraphics();
        int i = localBufferedImage.getHeight();
        int j = localBufferedImage.getWidth();

        int k = (int) (i * config.getScale());
        localGraphics.setColor(Color.decode(config.getColor()));
        FontPref localFontPref = FontPref.builder().systemFont().fontFamily(config.getFontFamily()).size(k).type(0).build();
        localFontPref.apply(localGraphics);
        int m = localGraphics.getFontMetrics().stringWidth(str);
        int n = j - m;
        localGraphics.drawString(str, n - 10, i - 10);
        if ((localGraphics instanceof Graphics2D)) {
            Graphics2D graphics = (Graphics2D) localGraphics;
            Font font = localFontPref.createFont();
            GlyphVector glyphVector = font.createGlyphVector(graphics.getFontRenderContext(), str);
            Shape shape = glyphVector.getOutline();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            BasicStroke localBasicStroke = new BasicStroke(1.0F);
            graphics.setColor(Color.decode(config.getGlyphColor()));
            graphics.setStroke(localBasicStroke);
            graphics.translate(n - 10, i - 10);
            graphics.draw(shape);
        }
        ImageWriter iWriter = (ImageWriter) ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam writeParam = ((ImageWriter) iWriter).getDefaultWriteParam();
        writeParam.setCompressionMode(2);
        writeParam.setCompressionQuality(1.0F);
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        ImageOutputStream iOut = ImageIO.createImageOutputStream(outBytes);
        iWriter.setOutput(iOut);
        IIOImage iImage = new IIOImage(localBufferedImage, null, null);
        iWriter.write(null, iImage, writeParam);
        iWriter.dispose();
        return outBytes.toByteArray();
    }


    static class FontPref {
        boolean systemFont;
        String fontFamily;
        File path;
        int size;
        int type;

        private FontPref(Builder paramBuilder) {
            this.systemFont = paramBuilder.systemFont;
            this.fontFamily = paramBuilder.fontFamily;
            this.path = paramBuilder.path;
            this.size = paramBuilder.size;
            this.type = paramBuilder.type;
        }

        public Font createFont()
                throws IOException, FontFormatException {
            if (this.systemFont) {
                return new Font(this.fontFamily, this.type, this.size);
            }
            File localFile = new File("Testo.ttf");
            System.out.println(localFile.getAbsolutePath());
            Font localFont = Font.createFont(0, localFile);
            GraphicsEnvironment localGraphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
            localGraphicsEnvironment.registerFont(localFont);
            return localFont;
        }

        void apply(Graphics paramGraphics)
                throws IOException, FontFormatException {
            paramGraphics.setFont(createFont());
        }

        static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            boolean systemFont;
            String fontFamily;
            File path;
            int size;
            int type;

            Builder systemFont() {
                this.systemFont = true;
                return this;
            }

            Builder fontFamily(String paramString) {
                this.fontFamily = paramString;
                return this;
            }

            Builder path(File paramFile) {
                this.path = paramFile;
                return this;
            }

            Builder size(int paramInt) {
                this.size = paramInt;
                return this;
            }

            Builder type(int paramInt) {
                this.type = paramInt;
                return this;
            }

            SignUtil.FontPref build() {
                return new SignUtil.FontPref(this);
            }
        }
    }
}
