package org.dromara.user.service;

import org.dromara.user.domain.Position;
import org.dromara.user.domain.vo.PositionVo;
import org.dromara.user.domain.bo.PositionBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 职位Service接口
 *
 * @author ljs
 * @date 2026-07-27
 */
public interface IPositionService {

    /**
     * 查询职位
     *
     * @param id 主键
     * @return 职位
     */
    PositionVo queryById(Long id);

    /**
     * 分页查询职位列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 职位分页列表
     */
    TableDataInfo<PositionVo> queryPageList(PositionBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的职位列表
     *
     * @param bo 查询条件
     * @return 职位列表
     */
    List<PositionVo> queryList(PositionBo bo);

    /**
     * 新增职位
     *
     * @param bo 职位
     * @return 是否新增成功
     */
    Boolean insertByBo(PositionBo bo);

    /**
     * 修改职位
     *
     * @param bo 职位
     * @return 是否修改成功
     */
    Boolean updateByBo(PositionBo bo);

    /**
     * 校验并批量删除职位信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
