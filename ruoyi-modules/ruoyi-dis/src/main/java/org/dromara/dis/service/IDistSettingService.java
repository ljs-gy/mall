package org.dromara.dis.service;

import org.dromara.dis.domain.DistSetting;
import org.dromara.dis.domain.vo.DistSettingVo;
import org.dromara.dis.domain.bo.DistSettingBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 分销基础配置Service接口
 *
 * @author ljs
 * @date 2026-08-06
 */
public interface IDistSettingService {

    /**
     * 查询分销基础配置
     *
     * @param id 主键
     * @return 分销基础配置
     */
    DistSettingVo queryById(Long id);

    /**
     * 分页查询分销基础配置列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分销基础配置分页列表
     */
    TableDataInfo<DistSettingVo> queryPageList(DistSettingBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的分销基础配置列表
     *
     * @param bo 查询条件
     * @return 分销基础配置列表
     */
    List<DistSettingVo> queryList(DistSettingBo bo);

    /**
     * 新增分销基础配置
     *
     * @param bo 分销基础配置
     * @return 是否新增成功
     */
    Boolean insertByBo(DistSettingBo bo);

    /**
     * 修改分销基础配置
     *
     * @param bo 分销基础配置
     * @return 是否修改成功
     */
    Boolean updateByBo(DistSettingBo bo);

    /**
     * 校验并批量删除分销基础配置信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
