package org.dromara.bidding.service;

import org.dromara.bidding.domain.Resource;
import org.dromara.bidding.domain.vo.ResourceVo;
import org.dromara.bidding.domain.bo.ResourceBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 竞价资源主Service接口
 *
 * @author ljs
 * @date 2026-06-21
 */
public interface IResourceService {

    /**
     * 查询竞价资源主
     *
     * @param id 主键
     * @return 竞价资源主
     */
    ResourceVo queryById(Long id);

    /**
     * 分页查询竞价资源主列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 竞价资源主分页列表
     */
    TableDataInfo<ResourceVo> queryPageList(ResourceBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的竞价资源主列表
     *
     * @param bo 查询条件
     * @return 竞价资源主列表
     */
    List<ResourceVo> queryList(ResourceBo bo);

    /**
     * 新增竞价资源主
     *
     * @param bo 竞价资源主
     * @return 是否新增成功
     */
    Boolean insertByBo(ResourceBo bo);

    /**
     * 修改竞价资源主
     *
     * @param bo 竞价资源主
     * @return 是否修改成功
     */
    Boolean updateByBo(ResourceBo bo);

    /**
     * 校验并批量删除竞价资源主信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
