package org.dromara.dis.service;

import org.dromara.dis.domain.DistRelation;
import org.dromara.dis.domain.vo.DistRelationVo;
import org.dromara.dis.domain.bo.DistRelationBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 分销上下级关系绑定Service接口
 *
 * @author ljs
 * @date 2026-08-06
 */
public interface IDistRelationService {

    /**
     * 查询分销上下级关系绑定
     *
     * @param id 主键
     * @return 分销上下级关系绑定
     */
    DistRelationVo queryById(Long id);

    /**
     * 分页查询分销上下级关系绑定列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分销上下级关系绑定分页列表
     */
    TableDataInfo<DistRelationVo> queryPageList(DistRelationBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的分销上下级关系绑定列表
     *
     * @param bo 查询条件
     * @return 分销上下级关系绑定列表
     */
    List<DistRelationVo> queryList(DistRelationBo bo);

    /**
     * 新增分销上下级关系绑定
     *
     * @param bo 分销上下级关系绑定
     * @return 是否新增成功
     */
    Boolean insertByBo(DistRelationBo bo);

    /**
     * 修改分销上下级关系绑定
     *
     * @param bo 分销上下级关系绑定
     * @return 是否修改成功
     */
    Boolean updateByBo(DistRelationBo bo);

    /**
     * 校验并批量删除分销上下级关系绑定信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
