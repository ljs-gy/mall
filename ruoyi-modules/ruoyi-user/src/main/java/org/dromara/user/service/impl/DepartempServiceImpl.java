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
import org.dromara.user.domain.bo.DepartempBo;
import org.dromara.user.domain.vo.DepartempVo;
import org.dromara.user.domain.Departemp;
import org.dromara.user.mapper.DepartempMapper;
import org.dromara.user.service.IDepartempService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 部门员工职位关系Service业务层处理
 *
 * @author ljs
 * @date 2026-07-27
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DepartempServiceImpl implements IDepartempService {

    private final DepartempMapper baseMapper;

    /**
     * 查询部门员工职位关系
     *
     * @param id 主键
     * @return 部门员工职位关系
     */
    @Override
    public DepartempVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询部门员工职位关系列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 部门员工职位关系分页列表
     */
    @Override
    public TableDataInfo<DepartempVo> queryPageList(DepartempBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Departemp> lqw = buildQueryWrapper(bo);
        Page<DepartempVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的部门员工职位关系列表
     *
     * @param bo 查询条件
     * @return 部门员工职位关系列表
     */
    @Override
    public List<DepartempVo> queryList(DepartempBo bo) {
        LambdaQueryWrapper<Departemp> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Departemp> buildQueryWrapper(DepartempBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Departemp> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Departemp::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getEmployeeCode()), Departemp::getEmployeeCode, bo.getEmployeeCode());
        lqw.eq(bo.getDeptId() != null, Departemp::getDeptId, bo.getDeptId());
        lqw.eq(StringUtils.isNotBlank(bo.getDeptPositionCode()), Departemp::getDeptPositionCode, bo.getDeptPositionCode());
        return lqw;
    }

    /**
     * 新增部门员工职位关系
     *
     * @param bo 部门员工职位关系
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(DepartempBo bo) {
        Departemp add = MapstructUtils.convert(bo, Departemp.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改部门员工职位关系
     *
     * @param bo 部门员工职位关系
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(DepartempBo bo) {
        Departemp update = MapstructUtils.convert(bo, Departemp.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Departemp entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除部门员工职位关系信息
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
