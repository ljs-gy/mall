package org.dromara.user.service.impl;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.dromara.user.domain.bo.PositionBo;
import org.dromara.user.domain.vo.PositionVo;
import org.dromara.user.domain.Position;
import org.dromara.user.mapper.PositionMapper;
import org.dromara.user.service.IPositionService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 职位Service业务层处理
 *
 * @author ljs
 * @date 2026-07-27
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PositionServiceImpl implements IPositionService {

    private final PositionMapper baseMapper;

    /**
     * 查询职位
     *
     * @param id 主键
     * @return 职位
     */
    @Override
    public PositionVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询职位列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 职位分页列表
     */
    @Override
    public TableDataInfo<PositionVo> queryPageList(PositionBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Position> lqw = buildQueryWrapper(bo);
        Page<PositionVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的职位列表
     *
     * @param bo 查询条件
     * @return 职位列表
     */
    @Override
    public List<PositionVo> queryList(PositionBo bo) {
        LambdaQueryWrapper<Position> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Position> buildQueryWrapper(PositionBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Position> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Position::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getOrgCode()), Position::getOrgCode, bo.getOrgCode());
        lqw.eq(bo.getRelationType() != null, Position::getRelationType, bo.getRelationType());
        lqw.eq(bo.getRelationId() != null, Position::getRelationId, bo.getRelationId());
        lqw.eq(StringUtils.isNotBlank(bo.getPositionCode()), Position::getPositionCode, bo.getPositionCode());
        lqw.like(StringUtils.isNotBlank(bo.getPositionName()), Position::getPositionName, bo.getPositionName());
        return lqw;
    }

    /**
     * 新增职位
     *
     * @param bo 职位
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(PositionBo bo) {
        Position add = MapstructUtils.convert(bo, Position.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改职位
     *
     * @param bo 职位
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(PositionBo bo) {
        Position update = MapstructUtils.convert(bo, Position.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Position entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除职位信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }
}
