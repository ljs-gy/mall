package org.dromara.dis.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 分销渠道商品 ES 客户端
 *
 * @author ljs
 * @date 2026-08-06
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "dis.elasticsearch", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DisElasticsearchConfig {

    @Value("${dis.elasticsearch.hosts:localhost:9200}")
    private String hosts;

    @Bean(destroyMethod = "close")
    public RestHighLevelClient restHighLevelClient() {
        String[] hostArr = hosts.split(",");
        HttpHost[] httpHosts = new HttpHost[hostArr.length];
        for (int i = 0; i < hostArr.length; i++) {
            String[] hp = hostArr[i].trim().split(":");
            String host = hp[0];
            int port = hp.length > 1 ? Integer.parseInt(hp[1]) : 9200;
            httpHosts[i] = new HttpHost(host, port, "http");
        }
        log.info("初始化分销 RestHighLevelClient, hosts={}", hosts);
        return new RestHighLevelClient(RestClient.builder(httpHosts));
    }
}
