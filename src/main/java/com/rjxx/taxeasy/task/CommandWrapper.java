package com.rjxx.taxeasy.task;

import com.rjxx.taxeasy.socket.command.SendCommand;

/**
 * Created by Administrator on 2017/1/6.
 */
public class CommandWrapper {

    private SendCommand sendCommand;

    private String message;

    public CommandWrapper(SendCommand sendCommand, String message) {
        this.sendCommand = sendCommand;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public SendCommand getSendCommand() {
        return sendCommand;
    }

    public void setSendCommand(SendCommand sendCommand) {
        this.sendCommand = sendCommand;
    }
}
