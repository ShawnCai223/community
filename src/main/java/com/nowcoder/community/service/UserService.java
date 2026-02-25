package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.uti.CommunityConstant;
import com.nowcoder.community.uti.CommunityUtil;
import com.nowcoder.community.uti.MailClient;
import org.apache.commons.lang3.ClassPathUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.nowcoder.community.dao.UserMapper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.swing.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        // Null check
        if (user == null) {
            throw new IllegalArgumentException("Parameter can't be null.");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "Username can't be null.");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "Password can't be null.");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "Email can't be null.");
            return map;
        }

        // Exist check
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "Username is already existed.");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "Email has been registered.");
            return map;
        }

        // register user
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("https://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // activation email
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("mail/activation", context);
        mailClient.sendMail(user.getEmail(), "Activation Email", content);

        return map;
    }

    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // Null check
        if(StringUtils.isBlank(username)) {
            map.put("usernameMsg", "Account can't be blank!");
            return map;
        }
        if(StringUtils.isBlank(password)) {
            map.put("passwordMsg", "Password can't be blank!");
            return map;
        }

        // Verify Account
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "This account isn't existed!");
            return map;
        }

        // Verify status
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "This account hasn't been activated!");
            return map;
        }

        // Verify password
        String hashed = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(hashed)) {
            map.put("passwordMsg", "This password doesn't match the account!");
            return map;
        }

        // Generate the login ticket
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date((System.currentTimeMillis() + expiredSeconds * 1000)));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
        loginTicketMapper.updateStatus(ticket, 1);
    }

    public LoginTicket findLoginTicket(String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
    }

    public int updateHeader(int userId, String headerUrl) {
        return userMapper.updateHeader(userId, headerUrl);
    }

    public Map<String, Object> updatePassword(int userId,
                                              String oldPassword,
                                              String newPassword,
                                              String confirmPassword) {

        Map<String, Object> map = new HashMap<>();

        if (StringUtils.isBlank(oldPassword)) {
            map.put("oldPasswordMsg", "Old password can't be empty!");
            return map;
        }

        if (StringUtils.isBlank(newPassword)) {
            map.put("newPasswordMsg", "New password can't be empty!");
            return map;
        }

        if (!newPassword.equals(confirmPassword)) {
            map.put("confirmPasswordMsg", "Passwords don't match!");
            return map;
        }

//        if (newPassword.length() < 6) {
//            map.put("newPasswordMsg", "Password must be at least 6 characters.");
//            return map;
//        }

        User user = userMapper.selectById(userId);

        String hashedOld = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!hashedOld.equals(user.getPassword())) {
            map.put("oldPasswordMsg", "Old password is incorrect!");
            return map;
        }

        String hashedNew = CommunityUtil.md5(newPassword + user.getSalt());

        userMapper.updatePassword(userId, hashedNew);

        return map;
    }
}
