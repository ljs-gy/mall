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
import org.dromara.goods.domain.bo.TypeBrandBo;
import org.dromara.goods.domain.vo.TypeBrandVo;
import org.dromara.goods.domain.TypeBrand;
import org.dromara.goods.mapper.TypeBrandMapper;
import org.dromara.goods.service.ITypeBrandService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 商品类型与品牌关系Service业务层处理
 *
 * @author ljs
 * @date 2026-07-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TypeBrandServiceImpl implements ITypeBrandService {

    private final TypeBrandMapper baseMapper;

    /**
     * 查询商品类型与品牌关系
     *
     * @param id 主键
     * @return 商品类型与品牌关系
     */
    @Override
    public TypeBrandVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询商品类型与品牌关系列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商品类型与品牌关系分页列表
     */
    @Override
    public TableDataInfo<TypeBrandVo> queryPageList(TypeBrandBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<TypeBrand> lqw = buildQueryWrapper(bo);
        Page<TypeBrandVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的商品类型与品牌关系列表
     *
     * @param bo 查询条件
     * @return 商品类型与品牌关系列表
     */
    @Override
    public List<TypeBrandVo> queryList(TypeBrandBo bo) {
        LambdaQueryWrapper<TypeBrand> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<TypeBrand> buildQueryWrapper(TypeBrandBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<TypeBrand> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(TypeBrand::getId);
        lqw.eq(bo.getTypeId() != null, TypeBrand::getTypeId, bo.getTypeId());
        lqw.eq(bo.getBrandId() != null, TypeBrand::getBrandId, bo.getBrandId());
        return lqw;
    }

    /**
     * 新增商品类型与品牌关系
     *
     * @param bo 商品类型与品牌关系
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(TypeBrandBo bo) {
        TypeBrand add = MapstructUtils.convert(bo, TypeBrand.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改商品类型与品牌关系
     *
     * @param bo 商品类型与品牌关系
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(TypeBrandBo bo) {
        TypeBrand update = MapstructUtils.convert(bo, TypeBrand.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(TypeBrand entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除商品类型与品牌关系信息
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
