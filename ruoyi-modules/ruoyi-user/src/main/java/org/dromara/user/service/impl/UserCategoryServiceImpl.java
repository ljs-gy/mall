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
import org.dromara.user.domain.bo.UserCategoryBo;
import org.dromara.user.domain.vo.UserCategoryVo;
import org.dromara.user.domain.UserCategory;
import org.dromara.user.mapper.UserCategoryMapper;
import org.dromara.user.service.IUserCategoryService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 用户经营类目资质Service业务层处理
 *
 * @author ljs
 * @date 2026-07-27
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UserCategoryServiceImpl implements IUserCategoryService {

    private final UserCategoryMapper baseMapper;

    /**
     * 查询用户经营类目资质
     *
     * @param id 主键
     * @return 用户经营类目资质
     */
    @Override
    public UserCategoryVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询用户经营类目资质列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户经营类目资质分页列表
     */
    @Override
    public TableDataInfo<UserCategoryVo> queryPageList(UserCategoryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<UserCategory> lqw = buildQueryWrapper(bo);
        Page<UserCategoryVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的用户经营类目资质列表
     *
     * @param bo 查询条件
     * @return 用户经营类目资质列表
     */
    @Override
    public List<UserCategoryVo> queryList(UserCategoryBo bo) {
        LambdaQueryWrapper<UserCategory> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<UserCategory> buildQueryWrapper(UserCategoryBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<UserCategory> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(UserCategory::getId);
        lqw.eq(bo.getUserinfoId() != null, UserCategory::getUserinfoId, bo.getUserinfoId());
        lqw.eq(StringUtils.isNotBlank(bo.getBizType()), UserCategory::getBizType, bo.getBizType());
        lqw.eq(bo.getCategoryId() != null, UserCategory::getCategoryId, bo.getCategoryId());
        lqw.eq(bo.getAuditStatus() != null, UserCategory::getAuditStatus, bo.getAuditStatus());
        return lqw;
    }

    /**
     * 新增用户经营类目资质
     *
     * @param bo 用户经营类目资质
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(UserCategoryBo bo) {
        UserCategory add = MapstructUtils.convert(bo, UserCategory.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改用户经营类目资质
     *
     * @param bo 用户经营类目资质
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(UserCategoryBo bo) {
        UserCategory update = MapstructUtils.convert(bo, UserCategory.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(UserCategory entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除用户经营类目资质信息
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
