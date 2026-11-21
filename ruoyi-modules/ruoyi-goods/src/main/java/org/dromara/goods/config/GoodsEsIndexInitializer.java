package org.dromara.goods.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.goods.constant.GoodsEsConstants;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

/**
 * 启动时确保 goods_sku_index 存在；已存在则仅补充 channel/promo 等新字段 mapping。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "goods.elasticsearch", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GoodsEsIndexInitializer implements ApplicationRunner {

    private static final String MAPPING_PATH = "es/goods_sku_index.json";

    private final ObjectProvider<RestHighLevelClient> restHighLevelClient;

    @Override
    public void run(ApplicationArguments args) {
        RestHighLevelClient client = restHighLevelClient.getIfAvailable();
        if (client == null) {
            log.warn("【ES索引】RestHighLevelClient 未启用，跳过索引初始化");
            return;
        }
        try {
            String body = StreamUtils.copyToString(
                new ClassPathResource(MAPPING_PATH).getInputStream(), StandardCharsets.UTF_8);
            GetIndexRequest existsReq = new GetIndexRequest(GoodsEsConstants.INDEX_GOODS_SKU);
            if (!client.indices().exists(existsReq, RequestOptions.DEFAULT)) {
                CreateIndexRequest create = new CreateIndexRequest(GoodsEsConstants.INDEX_GOODS_SKU);
                create.source(body, XContentType.JSON);
                client.indices().create(create, RequestOptions.DEFAULT);
                log.info("【ES索引】已创建 {}", GoodsEsConstants.INDEX_GOODS_SKU);
                return;
            }
            // 已存在：只 put 新增字段（ES 允许新增 property；冲突类型会打日志）
            String propertiesJson = extractPropertiesObject(body);
            PutMappingRequest putMapping = new PutMappingRequest(GoodsEsConstants.INDEX_GOODS_SKU);
            putMapping.source(propertiesJson, XContentType.JSON);
            client.indices().putMapping(putMapping, RequestOptions.DEFAULT);
            log.info("【ES索引】已尝试更新 mapping {}", GoodsEsConstants.INDEX_GOODS_SKU);
        } catch (Exception e) {
            log.error("【ES索引】初始化/更新 mapping 失败，请检查 ES 或手动重建索引", e);
        }
    }

    /**
     * 从完整 create body 取出 {"properties":{...}} 供 PutMapping 使用
     */
    private String extractPropertiesObject(String createBody) {
        int idx = createBody.indexOf("\"properties\"");
        if (idx < 0) {
            throw new IllegalStateException("mapping 缺少 properties");
        }
        int brace = createBody.indexOf('{', idx);
        int depth = 0;
        for (int i = brace; i < createBody.length(); i++) {
            char c = createBody.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return "{\"properties\":" + createBody.substring(brace, i + 1) + "}";
                }
            }
        }
        throw new IllegalStateException("无法解析 mapping properties");
    }
}
