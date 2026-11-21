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
import org.dromara.goods.domain.bo.SpuAttributeValueBo;
import org.dromara.goods.domain.vo.SpuAttributeValueVo;
import org.dromara.goods.domain.SpuAttributeValue;
import org.dromara.goods.mapper.SpuAttributeValueMapper;
import org.dromara.goods.service.ISpuAttributeValueService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 商品SPU属性值Service业务层处理
 *
 * @author ljs
 * @date 2026-07-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SpuAttributeValueServiceImpl implements ISpuAttributeValueService {

    private final SpuAttributeValueMapper baseMapper;

    /**
     * 查询商品SPU属性值
     *
     * @param id 主键
     * @return 商品SPU属性值
     */
    @Override
    public SpuAttributeValueVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询商品SPU属性值列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商品SPU属性值分页列表
     */
    @Override
    public TableDataInfo<SpuAttributeValueVo> queryPageList(SpuAttributeValueBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SpuAttributeValue> lqw = buildQueryWrapper(bo);
        Page<SpuAttributeValueVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的商品SPU属性值列表
     *
     * @param bo 查询条件
     * @return 商品SPU属性值列表
     */
    @Override
    public List<SpuAttributeValueVo> queryList(SpuAttributeValueBo bo) {
        LambdaQueryWrapper<SpuAttributeValue> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<SpuAttributeValue> buildQueryWrapper(SpuAttributeValueBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SpuAttributeValue> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(SpuAttributeValue::getId);
        lqw.eq(bo.getSpuId() != null, SpuAttributeValue::getSpuId, bo.getSpuId());
        lqw.eq(bo.getAttributeId() != null, SpuAttributeValue::getAttributeId, bo.getAttributeId());
        lqw.eq(bo.getAttributeOptionId() != null, SpuAttributeValue::getAttributeOptionId, bo.getAttributeOptionId());
        return lqw;
    }

    /**
     * 新增商品SPU属性值
     *
     * @param bo 商品SPU属性值
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(SpuAttributeValueBo bo) {
        SpuAttributeValue add = MapstructUtils.convert(bo, SpuAttributeValue.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改商品SPU属性值
     *
     * @param bo 商品SPU属性值
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(SpuAttributeValueBo bo) {
        SpuAttributeValue update = MapstructUtils.convert(bo, SpuAttributeValue.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(SpuAttributeValue entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除商品SPU属性值信息
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
