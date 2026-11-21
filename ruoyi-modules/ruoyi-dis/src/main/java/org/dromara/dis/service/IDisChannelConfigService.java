package org.dromara.dis.service;

import org.dromara.dis.domain.dto.DisGoodsPublishConfigSaveDTO;
import org.dromara.dis.domain.dto.DisGoodsScopeSaveDTO;
import org.dromara.dis.domain.dto.DisUserScopeSaveDTO;
import org.dromara.dis.domain.vo.DgoodsBuyVo;
import org.dromara.dis.domain.vo.DgoodsScopelistVo;
import org.dromara.dis.domain.vo.DgoodsVo;

import java.util.Collection;
import java.util.List;


public interface IDisChannelConfigService {


    void batchSaveUserScope(DisUserScopeSaveDTO dto);

    List<DgoodsBuyVo> listUserScope(String channelCode);

    void deleteUserScopeByIds(Collection<Long> ids);

    void saveOrUpdatePublishConfig(DisGoodsPublishConfigSaveDTO dto);


    void initDefaultPublishConfig(String tenantId, String channelCode);


    void removeByChannel(String tenantId, String channelCode);

    DgoodsVo getPublishConfig(String channelCode);


    void batchSaveGoodsScope(DisGoodsScopeSaveDTO dto);

    List<DgoodsScopelistVo> listGoodsScope(String channelCode);

    void deleteGoodsScopeByIds(Collection<Long> ids);
}
