package org.dromara.dis.service;

import org.dromara.dis.domain.Channel;
import org.dromara.dis.domain.vo.ChannelVo;
import org.dromara.dis.domain.bo.ChannelBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 渠道Service接口
 *
 * @author ljs
 * @date 2026-08-06
 */
public interface IChannelService {

    /**
     * 查询渠道
     *
     * @param id 主键
     * @return 渠道
     */
    ChannelVo queryById(Long id);

    /**
     * 分页查询渠道列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 渠道分页列表
     */
    TableDataInfo<ChannelVo> queryPageList(ChannelBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的渠道列表
     *
     * @param bo 查询条件
     * @return 渠道列表
     */
    List<ChannelVo> queryList(ChannelBo bo);

    /**
     * 新增渠道
     *
     * @param bo 渠道
     * @return 是否新增成功
     */
    Boolean insertByBo(ChannelBo bo);

    /**
     * 修改渠道
     *
     * @param bo 渠道
     * @return 是否修改成功
     */
    Boolean updateByBo(ChannelBo bo);

    Boolean deleteByIds(Collection<Long> ids);
}
