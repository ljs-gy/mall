package org.dromara.goods.service.impl;

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
import org.dromara.goods.domain.bo.ParameterBo;
import org.dromara.goods.domain.vo.ParameterVo;
import org.dromara.goods.domain.Parameter;
import org.dromara.goods.mapper.ParameterMapper;
import org.dromara.goods.service.IParameterService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 商品参数Service业务层处理
 *
 * @author ljs
 * @date 2026-07-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ParameterServiceImpl implements IParameterService {

    private final ParameterMapper baseMapper;

    /**
     * 查询商品参数
     *
     * @param id 主键
     * @return 商品参数
     */
    @Override
    public ParameterVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询商品参数列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商品参数分页列表
     */
    @Override
    public TableDataInfo<ParameterVo> queryPageList(ParameterBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Parameter> lqw = buildQueryWrapper(bo);
        Page<ParameterVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的商品参数列表
     *
     * @param bo 查询条件
     * @return 商品参数列表
     */
    @Override
    public List<ParameterVo> queryList(ParameterBo bo) {
        LambdaQueryWrapper<Parameter> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Parameter> buildQueryWrapper(ParameterBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Parameter> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Parameter::getId);
        lqw.eq(bo.getTypeId() != null, Parameter::getTypeId, bo.getTypeId());
        lqw.like(StringUtils.isNotBlank(bo.getParameterName()), Parameter::getParameterName, bo.getParameterName());
        return lqw;
    }

    /**
     * 新增商品参数
     *
     * @param bo 商品参数
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(ParameterBo bo) {
        Parameter add = MapstructUtils.convert(bo, Parameter.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改商品参数
     *
     * @param bo 商品参数
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(ParameterBo bo) {
        Parameter update = MapstructUtils.convert(bo, Parameter.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Parameter entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除商品参数信息
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
