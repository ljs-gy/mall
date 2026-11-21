package org.dromara.dis.service;

import org.dromara.dis.domain.DistGroup;
import org.dromara.dis.domain.vo.DistGroupVo;
import org.dromara.dis.domain.bo.DistGroupBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 分销员分组Service接口
 *
 * @author ljs
 * @date 2026-08-06
 */
public interface IDistGroupService {

    /**
     * 查询分销员分组
     *
     * @param id 主键
     * @return 分销员分组
     */
    DistGroupVo queryById(Long id);

    /**
     * 分页查询分销员分组列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分销员分组分页列表
     */
    TableDataInfo<DistGroupVo> queryPageList(DistGroupBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的分销员分组列表
     *
     * @param bo 查询条件
     * @return 分销员分组列表
     */
    List<DistGroupVo> queryList(DistGroupBo bo);

    /**
     * 新增分销员分组
     *
     * @param bo 分销员分组
     * @return 是否新增成功
     */
    Boolean insertByBo(DistGroupBo bo);

    /**
     * 修改分销员分组
     *
     * @param bo 分销员分组
     * @return 是否修改成功
     */
    Boolean updateByBo(DistGroupBo bo);

    /**
     * 校验并批量删除分销员分组信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
