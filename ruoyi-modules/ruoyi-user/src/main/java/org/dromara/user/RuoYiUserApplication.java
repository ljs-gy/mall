package org.dromara.user;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

/**
 * 业务用户中心模块（与 system 系统用户无关）
 *
 * @author ruoyi
 */
@EnableDubbo
@SpringBootApplication
public class RuoYiUserApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RuoYiUserApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("(♥◠‿◠)ﾉﾞ  业务用户中心模块启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }

}
