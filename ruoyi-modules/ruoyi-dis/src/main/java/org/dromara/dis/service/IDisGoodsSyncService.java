package org.dromara.dis.service;


import java.util.Collection;

public interface IDisGoodsSyncService {


    void syncSingleSpuToChannels(Long spuId, String tenantId, String parentChannelCode);


    void startManualGoodsSync(String tenantId, String channelCode);

    void removeChannelGoodsBySourceSkuCodes(String tenantId, String parentChannelCode, Collection<String> sourceSkuCodes);
}
