package com.example.uploadingfiles.sign;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface ISignUtil {
    byte[] drawText(Path inAbsolutePath, Config config, String str) throws IOException, FontFormatException;

    byte[] drawText(InputStream input, Config config, String str) throws IOException, FontFormatException;
}
