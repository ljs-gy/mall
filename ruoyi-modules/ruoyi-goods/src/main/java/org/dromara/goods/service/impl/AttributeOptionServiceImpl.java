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
import org.dromara.goods.domain.bo.AttributeOptionBo;
import org.dromara.goods.domain.vo.AttributeOptionVo;
import org.dromara.goods.domain.AttributeOption;
import org.dromara.goods.mapper.AttributeOptionMapper;
import org.dromara.goods.service.IAttributeOptionService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 商品属性选项Service业务层处理
 *
 * @author ljs
 * @date 2026-07-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AttributeOptionServiceImpl implements IAttributeOptionService {

    private final AttributeOptionMapper baseMapper;

    /**
     * 查询商品属性选项
     *
     * @param id 主键
     * @return 商品属性选项
     */
    @Override
    public AttributeOptionVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询商品属性选项列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商品属性选项分页列表
     */
    @Override
    public TableDataInfo<AttributeOptionVo> queryPageList(AttributeOptionBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<AttributeOption> lqw = buildQueryWrapper(bo);
        Page<AttributeOptionVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的商品属性选项列表
     *
     * @param bo 查询条件
     * @return 商品属性选项列表
     */
    @Override
    public List<AttributeOptionVo> queryList(AttributeOptionBo bo) {
        LambdaQueryWrapper<AttributeOption> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<AttributeOption> buildQueryWrapper(AttributeOptionBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<AttributeOption> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(AttributeOption::getId);
        lqw.eq(bo.getAttributeId() != null, AttributeOption::getAttributeId, bo.getAttributeId());
        lqw.like(StringUtils.isNotBlank(bo.getAttributeOptionName()), AttributeOption::getAttributeOptionName, bo.getAttributeOptionName());
        return lqw;
    }

    /**
     * 新增商品属性选项
     *
     * @param bo 商品属性选项
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(AttributeOptionBo bo) {
        AttributeOption add = MapstructUtils.convert(bo, AttributeOption.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改商品属性选项
     *
     * @param bo 商品属性选项
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(AttributeOptionBo bo) {
        AttributeOption update = MapstructUtils.convert(bo, AttributeOption.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(AttributeOption entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除商品属性选项信息
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
