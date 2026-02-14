package com.nowcoder.community;

import com.nowcoder.community.dao.AlphaDao;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.AlphaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)

public class MapperTests {
    @Autowired
    private UserMapper userMapper;

    @Autowired DiscussPostMapper discussPostMapper;

    @Test
    public void testSelectUser() {
        User user = userMapper.selectById(101);
        System.out.println(user);

        User user1 = userMapper.selectByName("liubei");
        System.out.println(user1);

        User user2 = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user2);
    }

    @Test
    public void testInsertUser() {
        User user3 = new User();
        user3.setUsername("test");
        user3.setPassword("123456");
        user3.setSalt("abc");
        user3.setEmail("test@qq.com");
        user3.setHeaderUrl("https://www.nowcoder.com/101.png");
        user3.setCreateTime(new Date());

        int rows = userMapper.insertUser(user3);
        System.out.println(rows);
        System.out.println(user3.getId());
    }

    @Test
    public void testUpdateUser() {
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);

        rows = userMapper.updateHeader(150, "https://www.nowcoder.com/102.png");
        System.out.println(rows);

        rows = userMapper.updatePassword(150, "hello");
        System.out.println(rows);
    }

    @Test
    public void testSelectPosts() {
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149,0,10);
        for(DiscussPost post: list) {
            System.out.println(post);
        }

        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }
}
