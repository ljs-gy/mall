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
import org.dromara.goods.domain.bo.SpuParameterValueBo;
import org.dromara.goods.domain.vo.SpuParameterValueVo;
import org.dromara.goods.domain.SpuParameterValue;
import org.dromara.goods.mapper.SpuParameterValueMapper;
import org.dromara.goods.service.ISpuParameterValueService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 商品SPU参数值Service业务层处理
 *
 * @author ljs
 * @date 2026-07-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SpuParameterValueServiceImpl implements ISpuParameterValueService {

    private final SpuParameterValueMapper baseMapper;

    @Override
    public SpuParameterValueVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<SpuParameterValueVo> queryPageList(SpuParameterValueBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SpuParameterValue> lqw = buildQueryWrapper(bo);
        Page<SpuParameterValueVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<SpuParameterValueVo> queryList(SpuParameterValueBo bo) {
        LambdaQueryWrapper<SpuParameterValue> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<SpuParameterValue> buildQueryWrapper(SpuParameterValueBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SpuParameterValue> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(SpuParameterValue::getId);
        lqw.eq(bo.getSpuId() != null, SpuParameterValue::getSpuId, bo.getSpuId());
        lqw.eq(bo.getParameterId() != null, SpuParameterValue::getParameterId, bo.getParameterId());
        lqw.like(StringUtils.isNotBlank(bo.getParameterValueText()), SpuParameterValue::getParameterValueText, bo.getParameterValueText());
        return lqw;
    }

    @Override
    public Boolean insertByBo(SpuParameterValueBo bo) {
        SpuParameterValue add = MapstructUtils.convert(bo, SpuParameterValue.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(SpuParameterValueBo bo) {
        SpuParameterValue update = MapstructUtils.convert(bo, SpuParameterValue.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    private void validEntityBeforeSave(SpuParameterValue entity) {
        // 唯一约束 uk_spu_param 由数据库保证
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            // 业务校验可按需补充
        }
        return baseMapper.deleteByIds(ids) > 0;
    }
}
