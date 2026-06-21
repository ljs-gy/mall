package org.dromara.bidding;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

/**
 * 竞价中心模块
 *
 * @author ruoyi
 */
@EnableDubbo
@SpringBootApplication
public class RuoYiBiddingApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RuoYiBiddingApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("(♥◠‿◠)ﾉﾞ  竞价中心模块启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }

}
