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
import org.dromara.user.domain.bo.GroupBo;
import org.dromara.user.domain.vo.GroupVo;
import org.dromara.user.domain.Group;
import org.dromara.user.mapper.GroupMapper;
import org.dromara.user.service.IGroupService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 群组Service业务层处理
 *
 * @author ljs
 * @date 2026-07-27
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class GroupServiceImpl implements IGroupService {

    private final GroupMapper baseMapper;

    /**
     * 查询群组
     *
     * @param id 主键
     * @return 群组
     */
    @Override
    public GroupVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询群组列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 群组分页列表
     */
    @Override
    public TableDataInfo<GroupVo> queryPageList(GroupBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Group> lqw = buildQueryWrapper(bo);
        Page<GroupVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的群组列表
     *
     * @param bo 查询条件
     * @return 群组列表
     */
    @Override
    public List<GroupVo> queryList(GroupBo bo) {
        LambdaQueryWrapper<Group> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Group> buildQueryWrapper(GroupBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Group> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Group::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getOrgCode()), Group::getOrgCode, bo.getOrgCode());
        lqw.like(StringUtils.isNotBlank(bo.getGroupName()), Group::getGroupName, bo.getGroupName());
        return lqw;
    }

    /**
     * 新增群组
     *
     * @param bo 群组
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(GroupBo bo) {
        Group add = MapstructUtils.convert(bo, Group.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改群组
     *
     * @param bo 群组
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(GroupBo bo) {
        Group update = MapstructUtils.convert(bo, Group.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Group entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除群组信息
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
