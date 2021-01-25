package com.github.ltprc.jigpipe.command;

/**
 * Connect Command
 * @author tuoli
 *
 */
public class ConnectCommand extends Command {

    static {
        commandType = CommandType.BMQ_CONNECT;
    }

    private int role;

    private String sessionId;

    private String username;

    private String password;

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
