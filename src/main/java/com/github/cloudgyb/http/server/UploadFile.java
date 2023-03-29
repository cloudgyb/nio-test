package com.github.cloudgyb.http.server;

import java.io.File;

/**
 * @author geng
 * @since 2023/03/29 16:47:12
 */
public class UploadFile {
    private final File file;
    private final String ContentType;

    public UploadFile(File file, String contentType) {
        this.file = file;
        ContentType = contentType;
    }

    public File getFile() {
        return file;
    }
}
