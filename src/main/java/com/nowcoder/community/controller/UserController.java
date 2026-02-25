package com.nowcoder.community.controller;


import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.uti.CommunityUtil;
import com.nowcoder.community.uti.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String upleadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null || headerImage.isEmpty()) {
            model.addAttribute("error", "No image selected!");
            return "/site/setting";
        }

        String original = headerImage.getOriginalFilename();
        if (StringUtils.isBlank(original) || original.lastIndexOf(".") < 0) {
            model.addAttribute("error", "Invalid file name!");
            return "/site/setting";
        }

        String suffix = original.substring(original.lastIndexOf(".")).toLowerCase();
        // Genarate random filename
        if (!suffix.equals(".png") && !suffix.equals(".jpg") && !suffix.equals(".jpeg") && !suffix.equals(".gif")) {
            model.addAttribute("error", "Unsupported image format!");
            return "/site/setting";
        }

        String fileName = CommunityUtil.generateUUID() + suffix;
        // Check the file storage path
        File dest = new File(uploadPath + "/" + fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("Fail to uplead the picture." + e.getMessage());
            throw new RuntimeException("Fail to uplead the picture. The server gets error.", e);
        }

        // Update the user header (web)
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUsers();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        logger.info("upload header: userId={}, headerUrl={}", user == null ? null : user.getId(), headerUrl);
        int rows = userService.updateHeader(user.getId(), headerUrl);
        logger.info("updateHeader rows={}", rows);

        return "redirect:/index";
    }


    @RequestMapping(path = "/header/{filename}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response) {
        // server upload path
        filename = uploadPath + "/" + filename;
        // suffix
        String suffix = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        if ("jpg".equals(suffix)) suffix = "jpeg";
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(filename);
                OutputStream os = response.getOutputStream())
        {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("Fail to get the picture. path={}", filename, e);
            throw new RuntimeException("Fail to uplead the picture. The server gets error.", e);
        }
    }

    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(String oldPassword,
                                 String newPassword,
                                 String confirmPassword,
                                 Model model,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        User user = hostHolder.getUsers();
        if (user == null) return "redirect:/login";

        Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword, confirmPassword);

        if (map.isEmpty()) {
            // 1) 取cookie里的 ticket
            String ticket = null;
            if (request.getCookies() != null) {
                for (Cookie c : request.getCookies()) {
                    if ("ticket".equals(c.getName())) {
                        ticket = c.getValue();
                        break;
                    }
                }
            }

            // 2) 作废该ticket（login_ticket.status = 1）
            if (ticket != null) {
                userService.logout(ticket); // 你service里已经有 logout(ticket)
            }

            // 3) 清cookie（让浏览器立刻“看起来”未登录）
            Cookie cookie = new Cookie("ticket", null);
            cookie.setPath(contextPath); // 一定要和登录时保持一致
            cookie.setMaxAge(0);
            response.addCookie(cookie);

            model.addAttribute("msg", "密码修改成功，请重新登录！");
            model.addAttribute("target", "/login");
            model.addAttribute("forceLogout", true);
            return "site/operate-result";
        }

        model.addAttribute("oldPasswordMsg", map.get("oldPasswordMsg"));
        model.addAttribute("newPasswordMsg", map.get("newPasswordMsg"));
        model.addAttribute("confirmPasswordMsg", map.get("confirmPasswordMsg"));
        return "site/setting";
    }
}
