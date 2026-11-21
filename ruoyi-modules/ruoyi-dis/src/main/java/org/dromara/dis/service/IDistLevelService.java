package org.dromara.dis.service;

import org.dromara.dis.domain.DistLevel;
import org.dromara.dis.domain.vo.DistLevelVo;
import org.dromara.dis.domain.bo.DistLevelBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 分销员等级Service接口
 *
 * @author ljs
 * @date 2026-08-06
 */
public interface IDistLevelService {

    /**
     * 查询分销员等级
     *
     * @param id 主键
     * @return 分销员等级
     */
    DistLevelVo queryById(Long id);

    /**
     * 分页查询分销员等级列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分销员等级分页列表
     */
    TableDataInfo<DistLevelVo> queryPageList(DistLevelBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的分销员等级列表
     *
     * @param bo 查询条件
     * @return 分销员等级列表
     */
    List<DistLevelVo> queryList(DistLevelBo bo);

    /**
     * 新增分销员等级
     *
     * @param bo 分销员等级
     * @return 是否新增成功
     */
    Boolean insertByBo(DistLevelBo bo);

    /**
     * 修改分销员等级
     *
     * @param bo 分销员等级
     * @return 是否修改成功
     */
    Boolean updateByBo(DistLevelBo bo);

    /**
     * 校验并批量删除分销员等级信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
