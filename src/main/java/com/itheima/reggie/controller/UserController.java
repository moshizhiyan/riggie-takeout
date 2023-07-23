package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发送手机验证码
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //懒得搞阿里云，做个假验证码


        log.info(user.toString());
        //将验证码存入session
        //session.setAttribute(user.getPhone(), "1234");
        // 将验证码存入redis
        redisTemplate.opsForValue().set(user.getPhone(), "1234", 5, TimeUnit.MINUTES);

        return R.success("发送验证码");
    }

    /**
     * 移动端用户登录
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
        //从session中获取验证码
        //Object codeInSession = session.getAttribute(phone);
        // 从redis缓存中获取验证码
        Object codeInSession = redisTemplate.opsForValue().get(phone);
        //如果拿不到验证码，给出提示
        if (codeInSession == null) throw new CustomException("请先获取验证码");

        log.info(phone);
        log.info(code);
        log.info(codeInSession.toString());
        //比对验证码
        if (codeInSession != null && codeInSession.equals(code)){
            //如果比对成功，则登录成功
            //判断手机号是否为新用户，是新用户则注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);
            if (user == null){
                user = new User();
                user.setPhone(phone);
                userService.save(user);
            }
            //将用户id加入到session中
            session.setAttribute("user", user.getId());
            // 如果登录成功，则删除缓存的验证码
            redisTemplate.delete(phone);
            return R.success(user);
        }
        return R.error("验证码不正确");
    }

    /**
     * 用户退出登录
     * @param session
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpSession session){
        session.removeAttribute("user");
        return R.success("退出登录成功");
    }

}
