package org.dromara.pm;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 营销中心模块
 *
 * @author ruoyi
 */
@EnableDubbo
@EnableAsync
@SpringBootApplication
public class RuoYiPmApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RuoYiPmApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("(♥◠‿◠)ﾉﾞ  营销中心模块启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }

}
