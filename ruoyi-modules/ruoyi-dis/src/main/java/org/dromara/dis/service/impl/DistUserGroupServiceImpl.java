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
import org.dromara.dis.domain.bo.DistUserGroupBo;
import org.dromara.dis.domain.vo.DistUserGroupVo;
import org.dromara.dis.domain.DistUserGroup;
import org.dromara.dis.mapper.DistUserGroupMapper;
import org.dromara.dis.service.IDistUserGroupService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 分销员与分组关系Service业务层处理
 *
 * @author ljs
 * @date 2026-08-06
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DistUserGroupServiceImpl implements IDistUserGroupService {

    private final DistUserGroupMapper baseMapper;

    /**
     * 查询分销员与分组关系
     *
     * @param id 主键
     * @return 分销员与分组关系
     */
    @Override
    public DistUserGroupVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询分销员与分组关系列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分销员与分组关系分页列表
     */
    @Override
    public TableDataInfo<DistUserGroupVo> queryPageList(DistUserGroupBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<DistUserGroup> lqw = buildQueryWrapper(bo);
        Page<DistUserGroupVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的分销员与分组关系列表
     *
     * @param bo 查询条件
     * @return 分销员与分组关系列表
     */
    @Override
    public List<DistUserGroupVo> queryList(DistUserGroupBo bo) {
        LambdaQueryWrapper<DistUserGroup> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<DistUserGroup> buildQueryWrapper(DistUserGroupBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<DistUserGroup> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(DistUserGroup::getId);
        lqw.eq(bo.getUserinfoId() != null, DistUserGroup::getUserinfoId, bo.getUserinfoId());
        lqw.eq(bo.getGroupId() != null, DistUserGroup::getGroupId, bo.getGroupId());
        return lqw;
    }

    /**
     * 新增分销员与分组关系
     *
     * @param bo 分销员与分组关系
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(DistUserGroupBo bo) {
        DistUserGroup add = MapstructUtils.convert(bo, DistUserGroup.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改分销员与分组关系
     *
     * @param bo 分销员与分组关系
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(DistUserGroupBo bo) {
        DistUserGroup update = MapstructUtils.convert(bo, DistUserGroup.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(DistUserGroup entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除分销员与分组关系信息
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
