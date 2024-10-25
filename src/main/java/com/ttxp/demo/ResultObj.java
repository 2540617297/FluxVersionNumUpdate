package com.ttxp.demo;

/**
 * <p>
 * 创建时间：2024/10/21
 * <p>
 *
 * <p>
 * 修改时间：2024/10/21
 * <p>
 *
 * @author pengtai
 * @version V1.0.0
 */
public class ResultObj {

    private String message;
    private String filePath;
    private boolean isOk;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isOk() {
        return isOk;
    }

    public void setOk(boolean ok) {
        isOk = ok;
    }
}
