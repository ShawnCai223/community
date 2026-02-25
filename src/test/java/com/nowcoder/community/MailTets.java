package com.nowcoder.community;


import com.nowcoder.community.uti.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;

import org.thymeleaf.context.Context;
import javax.xml.transform.Templates;


@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTets {
    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTextMail() {
        mailClient.sendMail("shawn.jx.cai@gmail.com", "TEST", "WELCOME");
    }

    @Test
    public void testHtmlMail() {
        Context context = new Context();
        context.setVariable("username", "Shawn");
        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        mailClient.sendMail("shawn.jx.cai@gmail.com", "TestHtml", content);
    }

}
