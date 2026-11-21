package org.dromara.pm.service;

import org.dromara.pm.domain.PromoTierRule;
import org.dromara.pm.domain.vo.PromoTierRuleVo;
import org.dromara.pm.domain.bo.PromoTierRuleBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 营销活动阶梯规则Service接口
 *
 * @author ljs
 * @date 2026-07-21
 */
public interface IPromoTierRuleService {

    /**
     * 查询营销活动阶梯规则
     *
     * @param id 主键
     * @return 营销活动阶梯规则
     */
    PromoTierRuleVo queryById(Long id);

    /**
     * 分页查询营销活动阶梯规则列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 营销活动阶梯规则分页列表
     */
    TableDataInfo<PromoTierRuleVo> queryPageList(PromoTierRuleBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的营销活动阶梯规则列表
     *
     * @param bo 查询条件
     * @return 营销活动阶梯规则列表
     */
    List<PromoTierRuleVo> queryList(PromoTierRuleBo bo);

    /**
     * 新增营销活动阶梯规则
     *
     * @param bo 营销活动阶梯规则
     * @return 是否新增成功
     */
    Boolean insertByBo(PromoTierRuleBo bo);

    /**
     * 修改营销活动阶梯规则
     *
     * @param bo 营销活动阶梯规则
     * @return 是否修改成功
     */
    Boolean updateByBo(PromoTierRuleBo bo);

    /**
     * 校验并批量删除营销活动阶梯规则信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
