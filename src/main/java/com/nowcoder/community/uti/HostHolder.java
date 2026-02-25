package com.nowcoder.community.uti;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

/*
Hold the user information, to substitute session objects.
 */
@Component
public class HostHolder {

    public ThreadLocal<User> users = new ThreadLocal<>();

    public void setUsers(User user) {
        users.set(user);
    }

    public User getUsers() {
        return users.get();
    }

    public void clear() {
        users.remove();
    }
}
