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
import org.dromara.user.domain.bo.DepartmentBo;
import org.dromara.user.domain.vo.DepartmentVo;
import org.dromara.user.domain.Department;
import org.dromara.user.mapper.DepartmentMapper;
import org.dromara.user.service.IDepartmentService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 部门Service业务层处理
 *
 * @author ljs
 * @date 2026-07-27
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DepartmentServiceImpl implements IDepartmentService {

    private final DepartmentMapper baseMapper;

    /**
     * 查询部门
     *
     * @param id 主键
     * @return 部门
     */
    @Override
    public DepartmentVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询部门列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 部门分页列表
     */
    @Override
    public TableDataInfo<DepartmentVo> queryPageList(DepartmentBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Department> lqw = buildQueryWrapper(bo);
        Page<DepartmentVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的部门列表
     *
     * @param bo 查询条件
     * @return 部门列表
     */
    @Override
    public List<DepartmentVo> queryList(DepartmentBo bo) {
        LambdaQueryWrapper<Department> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Department> buildQueryWrapper(DepartmentBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Department> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Department::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getOrgCode()), Department::getOrgCode, bo.getOrgCode());
        lqw.like(StringUtils.isNotBlank(bo.getDeptName()), Department::getDeptName, bo.getDeptName());
        return lqw;
    }

    /**
     * 新增部门
     *
     * @param bo 部门
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(DepartmentBo bo) {
        Department add = MapstructUtils.convert(bo, Department.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改部门
     *
     * @param bo 部门
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(DepartmentBo bo) {
        Department update = MapstructUtils.convert(bo, Department.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Department entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除部门信息
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
