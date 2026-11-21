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
import org.dromara.user.domain.bo.UserBo;
import org.dromara.user.domain.vo.UserVo;
import org.dromara.user.domain.User;
import org.dromara.user.mapper.UserMapper;
import org.dromara.user.service.IUserService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 用户Service业务层处理
 *
 * @author ljs
 * @date 2026-07-27
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements IUserService {

    private final UserMapper baseMapper;

    /**
     * 查询用户
     *
     * @param id 主键
     * @return 用户
     */
    @Override
    public UserVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询用户列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户分页列表
     */
    @Override
    public TableDataInfo<UserVo> queryPageList(UserBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<User> lqw = buildQueryWrapper(bo);
        Page<UserVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的用户列表
     *
     * @param bo 查询条件
     * @return 用户列表
     */
    @Override
    public List<UserVo> queryList(UserBo bo) {
        LambdaQueryWrapper<User> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<User> buildQueryWrapper(UserBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<User> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(User::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getUserCode()), User::getUserCode, bo.getUserCode());
        lqw.eq(bo.getUserinfoId() != null, User::getUserinfoId, bo.getUserinfoId());
        lqw.eq(StringUtils.isNotBlank(bo.getUserPcode()), User::getUserPcode, bo.getUserPcode());
        lqw.eq(StringUtils.isNotBlank(bo.getUserMobile()), User::getUserMobile, bo.getUserMobile());
        lqw.eq(StringUtils.isNotBlank(bo.getUserBindMobile()), User::getUserBindMobile, bo.getUserBindMobile());
        lqw.eq(StringUtils.isNotBlank(bo.getUserEmail()), User::getUserEmail, bo.getUserEmail());
        lqw.eq(StringUtils.isNotBlank(bo.getUserPassword()), User::getUserPassword, bo.getUserPassword());
        lqw.eq(StringUtils.isNotBlank(bo.getUserPayPassword()), User::getUserPayPassword, bo.getUserPayPassword());
        lqw.eq(StringUtils.isNotBlank(bo.getUserCustomerServicePhone()), User::getUserCustomerServicePhone, bo.getUserCustomerServicePhone());
        lqw.eq(bo.getIsEnabled() != null, User::getIsEnabled, bo.getIsEnabled());
        return lqw;
    }

    /**
     * 新增用户
     *
     * @param bo 用户
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(UserBo bo) {
        User add = MapstructUtils.convert(bo, User.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改用户
     *
     * @param bo 用户
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(UserBo bo) {
        User update = MapstructUtils.convert(bo, User.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(User entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除用户信息
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
