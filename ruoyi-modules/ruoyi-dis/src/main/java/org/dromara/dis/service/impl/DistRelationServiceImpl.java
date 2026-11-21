package org.dromara.dis.service.impl;

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
import org.dromara.dis.domain.bo.DistRelationBo;
import org.dromara.dis.domain.vo.DistRelationVo;
import org.dromara.dis.domain.DistRelation;
import org.dromara.dis.mapper.DistRelationMapper;
import org.dromara.dis.service.IDistRelationService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 分销上下级关系绑定Service业务层处理
 *
 * @author ljs
 * @date 2026-08-06
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DistRelationServiceImpl implements IDistRelationService {

    private final DistRelationMapper baseMapper;

    /**
     * 查询分销上下级关系绑定
     *
     * @param id 主键
     * @return 分销上下级关系绑定
     */
    @Override
    public DistRelationVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询分销上下级关系绑定列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分销上下级关系绑定分页列表
     */
    @Override
    public TableDataInfo<DistRelationVo> queryPageList(DistRelationBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<DistRelation> lqw = buildQueryWrapper(bo);
        Page<DistRelationVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的分销上下级关系绑定列表
     *
     * @param bo 查询条件
     * @return 分销上下级关系绑定列表
     */
    @Override
    public List<DistRelationVo> queryList(DistRelationBo bo) {
        LambdaQueryWrapper<DistRelation> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<DistRelation> buildQueryWrapper(DistRelationBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<DistRelation> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(DistRelation::getId);
        lqw.eq(bo.getUserinfoId() != null, DistRelation::getUserinfoId, bo.getUserinfoId());
        lqw.eq(bo.getParentUserinfoId() != null, DistRelation::getParentUserinfoId, bo.getParentUserinfoId());
        lqw.eq(bo.getBindTime() != null, DistRelation::getBindTime, bo.getBindTime());
        return lqw;
    }

    /**
     * 新增分销上下级关系绑定
     *
     * @param bo 分销上下级关系绑定
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(DistRelationBo bo) {
        DistRelation add = MapstructUtils.convert(bo, DistRelation.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改分销上下级关系绑定
     *
     * @param bo 分销上下级关系绑定
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(DistRelationBo bo) {
        DistRelation update = MapstructUtils.convert(bo, DistRelation.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(DistRelation entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除分销上下级关系绑定信息
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
