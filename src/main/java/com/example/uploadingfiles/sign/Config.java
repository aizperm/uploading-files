package com.example.uploadingfiles.sign;

public class Config {
    float scale = 0.04F;
    boolean systemFont = true;
    String fontFamily = "Cambria Math";
    String color = "#00FF00";
    String glyphColor = "#000000";

    public boolean isSystemFont() {
        return this.systemFont;
    }

    public void setSystemFont(boolean paramBoolean) {
        this.systemFont = paramBoolean;
    }

    public float getScale() {
        return this.scale;
    }

    public void setScale(float paramFloat) {
        this.scale = paramFloat;
    }

    public String getFontFamily() {
        return this.fontFamily;
    }

    public void setFontFamily(String paramString) {
        this.fontFamily = paramString;
    }

    public String getColor() {
        return this.color;
    }

    public void setColor(String paramString) {
        this.color = paramString;
    }

    public String getGlyphColor() {
        return glyphColor;
    }

    public void setGlyphColor(String glyphColor) {
        this.glyphColor = glyphColor;
    }
}
