package com.weikun.server.common.entity;

import com.weikun.server.common.CodeUtil;

import java.io.File;
import java.io.Serializable;

/**
 * @author weikun
 * @date 2019/12/16
 */
public class FileData implements Serializable {
    private File file;
    private String fileName;
    private String md5;
    private long size;

    public FileData(File file){
        this.file = file;
        this.fileName = file.getName();
        this.md5 = CodeUtil.getFileMd5(file);
        this.size = file.length();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

}
