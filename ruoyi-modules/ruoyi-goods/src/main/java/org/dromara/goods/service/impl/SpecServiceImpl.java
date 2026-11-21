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
import org.dromara.goods.domain.bo.SpecBo;
import org.dromara.goods.domain.vo.SpecVo;
import org.dromara.goods.domain.Spec;
import org.dromara.goods.mapper.SpecMapper;
import org.dromara.goods.service.ISpecService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 商品规格Service业务层处理
 *
 * @author ljs
 * @date 2026-07-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SpecServiceImpl implements ISpecService {

    private final SpecMapper baseMapper;

    /**
     * 查询商品规格
     *
     * @param id 主键
     * @return 商品规格
     */
    @Override
    public SpecVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询商品规格列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商品规格分页列表
     */
    @Override
    public TableDataInfo<SpecVo> queryPageList(SpecBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Spec> lqw = buildQueryWrapper(bo);
        Page<SpecVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的商品规格列表
     *
     * @param bo 查询条件
     * @return 商品规格列表
     */
    @Override
    public List<SpecVo> queryList(SpecBo bo) {
        LambdaQueryWrapper<Spec> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Spec> buildQueryWrapper(SpecBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Spec> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Spec::getId);
        lqw.eq(bo.getSpecGroupId() != null, Spec::getSpecGroupId, bo.getSpecGroupId());
        lqw.like(StringUtils.isNotBlank(bo.getSpecName()), Spec::getSpecName, bo.getSpecName());
        return lqw;
    }

    /**
     * 新增商品规格
     *
     * @param bo 商品规格
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(SpecBo bo) {
        Spec add = MapstructUtils.convert(bo, Spec.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改商品规格
     *
     * @param bo 商品规格
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(SpecBo bo) {
        Spec update = MapstructUtils.convert(bo, Spec.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Spec entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除商品规格信息
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
