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
import org.dromara.goods.domain.bo.AttributeBo;
import org.dromara.goods.domain.vo.AttributeVo;
import org.dromara.goods.domain.Attribute;
import org.dromara.goods.mapper.AttributeMapper;
import org.dromara.goods.service.IAttributeService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 商品属性Service业务层处理
 *
 * @author ljs
 * @date 2026-07-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AttributeServiceImpl implements IAttributeService {

    private final AttributeMapper baseMapper;

    /**
     * 查询商品属性
     *
     * @param id 主键
     * @return 商品属性
     */
    @Override
    public AttributeVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询商品属性列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商品属性分页列表
     */
    @Override
    public TableDataInfo<AttributeVo> queryPageList(AttributeBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Attribute> lqw = buildQueryWrapper(bo);
        Page<AttributeVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的商品属性列表
     *
     * @param bo 查询条件
     * @return 商品属性列表
     */
    @Override
    public List<AttributeVo> queryList(AttributeBo bo) {
        LambdaQueryWrapper<Attribute> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Attribute> buildQueryWrapper(AttributeBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Attribute> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Attribute::getId);
        lqw.eq(bo.getTypeId() != null, Attribute::getTypeId, bo.getTypeId());
        lqw.like(StringUtils.isNotBlank(bo.getAttributeName()), Attribute::getAttributeName, bo.getAttributeName());
        return lqw;
    }

    /**
     * 新增商品属性
     *
     * @param bo 商品属性
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(AttributeBo bo) {
        Attribute add = MapstructUtils.convert(bo, Attribute.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改商品属性
     *
     * @param bo 商品属性
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(AttributeBo bo) {
        Attribute update = MapstructUtils.convert(bo, Attribute.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Attribute entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除商品属性信息
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
