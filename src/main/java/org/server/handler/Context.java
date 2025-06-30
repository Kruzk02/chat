package org.server.handler;

import java.io.DataOutputStream;
import java.util.Map;
import java.util.Set;

public class Context {
    private String username;
    private Set<String> joinedGroup;
    private Map<String, Map<String, DataOutputStream>> groups;
    private DataOutputStream out;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<String> getJoinedGroup() {
        return joinedGroup;
    }

    public void setJoinedGroup(Set<String> joinedGroup) {
        this.joinedGroup = joinedGroup;
    }

    public Map<String, Map<String, DataOutputStream>> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, Map<String, DataOutputStream>> groups) {
        this.groups = groups;
    }

    public DataOutputStream getOut() {
        return out;
    }

    public void setOut(DataOutputStream out) {
        this.out = out;
    }
}
