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
import org.dromara.user.domain.bo.UserIdentityRelBo;
import org.dromara.user.domain.vo.UserIdentityRelVo;
import org.dromara.user.domain.UserIdentityRel;
import org.dromara.user.mapper.UserIdentityRelMapper;
import org.dromara.user.service.IUserIdentityRelService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 用户主体身份关系Service业务层处理
 *
 * @author ljs
 * @date 2026-07-27
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UserIdentityRelServiceImpl implements IUserIdentityRelService {

    private final UserIdentityRelMapper baseMapper;

    /**
     * 查询用户主体身份关系
     *
     * @param id 主键
     * @return 用户主体身份关系
     */
    @Override
    public UserIdentityRelVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询用户主体身份关系列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户主体身份关系分页列表
     */
    @Override
    public TableDataInfo<UserIdentityRelVo> queryPageList(UserIdentityRelBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<UserIdentityRel> lqw = buildQueryWrapper(bo);
        Page<UserIdentityRelVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的用户主体身份关系列表
     *
     * @param bo 查询条件
     * @return 用户主体身份关系列表
     */
    @Override
    public List<UserIdentityRelVo> queryList(UserIdentityRelBo bo) {
        LambdaQueryWrapper<UserIdentityRel> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<UserIdentityRel> buildQueryWrapper(UserIdentityRelBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<UserIdentityRel> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(UserIdentityRel::getId);
        lqw.eq(bo.getUserinfoId() != null, UserIdentityRel::getUserinfoId, bo.getUserinfoId());
        lqw.eq(StringUtils.isNotBlank(bo.getIdentityType()), UserIdentityRel::getIdentityType, bo.getIdentityType());
        lqw.eq(bo.getStatus() != null, UserIdentityRel::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 新增用户主体身份关系
     *
     * @param bo 用户主体身份关系
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(UserIdentityRelBo bo) {
        UserIdentityRel add = MapstructUtils.convert(bo, UserIdentityRel.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改用户主体身份关系
     *
     * @param bo 用户主体身份关系
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(UserIdentityRelBo bo) {
        UserIdentityRel update = MapstructUtils.convert(bo, UserIdentityRel.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(UserIdentityRel entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除用户主体身份关系信息
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
