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
import org.dromara.dis.domain.bo.DistGroupBo;
import org.dromara.dis.domain.vo.DistGroupVo;
import org.dromara.dis.domain.DistGroup;
import org.dromara.dis.mapper.DistGroupMapper;
import org.dromara.dis.service.IDistGroupService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 分销员分组Service业务层处理
 *
 * @author ljs
 * @date 2026-08-06
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DistGroupServiceImpl implements IDistGroupService {

    private final DistGroupMapper baseMapper;

    /**
     * 查询分销员分组
     *
     * @param id 主键
     * @return 分销员分组
     */
    @Override
    public DistGroupVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询分销员分组列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分销员分组分页列表
     */
    @Override
    public TableDataInfo<DistGroupVo> queryPageList(DistGroupBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<DistGroup> lqw = buildQueryWrapper(bo);
        Page<DistGroupVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的分销员分组列表
     *
     * @param bo 查询条件
     * @return 分销员分组列表
     */
    @Override
    public List<DistGroupVo> queryList(DistGroupBo bo) {
        LambdaQueryWrapper<DistGroup> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<DistGroup> buildQueryWrapper(DistGroupBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<DistGroup> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(DistGroup::getId);
        lqw.like(StringUtils.isNotBlank(bo.getGroupName()), DistGroup::getGroupName, bo.getGroupName());
        return lqw;
    }

    /**
     * 新增分销员分组
     *
     * @param bo 分销员分组
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(DistGroupBo bo) {
        DistGroup add = MapstructUtils.convert(bo, DistGroup.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改分销员分组
     *
     * @param bo 分销员分组
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(DistGroupBo bo) {
        DistGroup update = MapstructUtils.convert(bo, DistGroup.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(DistGroup entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除分销员分组信息
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
