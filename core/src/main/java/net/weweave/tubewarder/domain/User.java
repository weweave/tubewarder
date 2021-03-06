package net.weweave.tubewarder.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.Date;

@Entity
public class User extends AbstractPersistentObject {
    private String displayName;
    @Column(unique = true)
    private String username;
    private String hashedPassword;
    private Boolean enabled = true;
    private Date lastLogin;
    private Boolean allowAppTokens = false;
    private Boolean allowChannels = false;
    private Boolean allowTemplates = false;
    private Boolean allowSystemConfig = false;
    private Boolean allowLogs = false;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Date getLastLogin() {
        return (lastLogin == null ? null : new Date(lastLogin.getTime()));
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = (lastLogin == null ? null : new Date(lastLogin.getTime()));
    }

    public Boolean getAllowAppTokens() {
        return allowAppTokens;
    }

    public void setAllowAppTokens(Boolean allowAppTokens) {
        this.allowAppTokens = allowAppTokens;
    }

    public Boolean getAllowChannels() {
        return allowChannels;
    }

    public void setAllowChannels(Boolean allowChannels) {
        this.allowChannels = allowChannels;
    }

    public Boolean getAllowTemplates() {
        return allowTemplates;
    }

    public void setAllowTemplates(Boolean allowTemplates) {
        this.allowTemplates = allowTemplates;
    }

    public Boolean getAllowSystemConfig() {
        return allowSystemConfig;
    }

    public void setAllowSystemConfig(Boolean allowSystemConfig) {
        this.allowSystemConfig = allowSystemConfig;
    }

    public Boolean getAllowLogs() {
        return allowLogs;
    }

    public void setAllowLogs(Boolean allowLogs) {
        this.allowLogs = allowLogs;
    }
}
