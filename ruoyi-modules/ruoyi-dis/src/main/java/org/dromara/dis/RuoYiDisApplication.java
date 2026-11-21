package org.dromara.dis;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

/**
 * 分销中心模块
 *
 * @author ruoyi
 */
@EnableDubbo
@SpringBootApplication
public class RuoYiDisApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RuoYiDisApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("(♥◠‿◠)ﾉﾞ  分销中心模块启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }

}
