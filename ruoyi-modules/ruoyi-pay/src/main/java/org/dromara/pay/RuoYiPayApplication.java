package org.dromara.pay;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

/**
 * 支付中心模块
 */
@EnableDubbo
@SpringBootApplication
public class RuoYiPayApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RuoYiPayApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("(♥◠‿◠)ﾉﾞ  支付中心模块启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}
