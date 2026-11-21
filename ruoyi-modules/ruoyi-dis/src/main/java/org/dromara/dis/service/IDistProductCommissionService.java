package org.dromara.dis.service;

import org.dromara.dis.domain.DistProductCommission;
import org.dromara.dis.domain.vo.DistProductCommissionVo;
import org.dromara.dis.domain.bo.DistProductCommissionBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 商品分佣配置Service接口
 *
 * @author ljs
 * @date 2026-08-06
 */
public interface IDistProductCommissionService {

    /**
     * 查询商品分佣配置
     *
     * @param id 主键
     * @return 商品分佣配置
     */
    DistProductCommissionVo queryById(Long id);

    /**
     * 分页查询商品分佣配置列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商品分佣配置分页列表
     */
    TableDataInfo<DistProductCommissionVo> queryPageList(DistProductCommissionBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的商品分佣配置列表
     *
     * @param bo 查询条件
     * @return 商品分佣配置列表
     */
    List<DistProductCommissionVo> queryList(DistProductCommissionBo bo);

    /**
     * 新增商品分佣配置
     *
     * @param bo 商品分佣配置
     * @return 是否新增成功
     */
    Boolean insertByBo(DistProductCommissionBo bo);

    /**
     * 修改商品分佣配置
     *
     * @param bo 商品分佣配置
     * @return 是否修改成功
     */
    Boolean updateByBo(DistProductCommissionBo bo);

    /**
     * 校验并批量删除商品分佣配置信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
