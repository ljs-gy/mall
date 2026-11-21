package org.dromara.pm.service;

import org.dromara.pm.domain.PromoScopeTarget;
import org.dromara.pm.domain.vo.PromoScopeTargetVo;
import org.dromara.pm.domain.bo.PromoScopeTargetBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 营销范围Service接口
 *
 * @author ljs
 * @date 2026-07-21
 */
public interface IPromoScopeTargetService {

    /**
     * 查询营销范围
     *
     * @param id 主键
     * @return 营销范围
     */
    PromoScopeTargetVo queryById(Long id);

    /**
     * 分页查询营销范围列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 营销范围分页列表
     */
    TableDataInfo<PromoScopeTargetVo> queryPageList(PromoScopeTargetBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的营销范围列表
     *
     * @param bo 查询条件
     * @return 营销范围列表
     */
    List<PromoScopeTargetVo> queryList(PromoScopeTargetBo bo);

    /**
     * 新增营销范围
     *
     * @param bo 营销范围
     * @return 是否新增成功
     */
    Boolean insertByBo(PromoScopeTargetBo bo);

    /**
     * 修改营销范围
     *
     * @param bo 营销范围
     * @return 是否修改成功
     */
    Boolean updateByBo(PromoScopeTargetBo bo);

    /**
     * 校验并批量删除营销范围信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
