package com.nowcoder.community;


import com.nowcoder.community.uti.SensitiveFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTests {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void SensitiveFilter() {
        String text = "You can drug, can prostitution, can bride or gamble lol.";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "You can d**r*ug, can pros**titution, can bride or gam*ble lol.";
        text = sensitiveFilter.filter(text);
        System.out.println(text);
    }
}
