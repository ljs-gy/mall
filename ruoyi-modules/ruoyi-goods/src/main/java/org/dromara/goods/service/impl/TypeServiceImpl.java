package org.dromara.goods.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.utils.IdGeneratorUtil;
import org.dromara.goods.domain.Attribute;
import org.dromara.goods.domain.AttributeOption;
import org.dromara.goods.domain.Brand;
import org.dromara.goods.domain.Parameter;
import org.dromara.goods.domain.Spec;
import org.dromara.goods.domain.SpecGroup;
import org.dromara.goods.domain.SpecOption;
import org.dromara.goods.domain.Type;
import org.dromara.goods.domain.TypeBrand;
import org.dromara.goods.domain.bo.TypeBo;
import org.dromara.goods.domain.vo.AttributeOptionVo;
import org.dromara.goods.domain.vo.AttributeVo;
import org.dromara.goods.domain.vo.BrandVo;
import org.dromara.goods.domain.vo.ParameterVo;
import org.dromara.goods.domain.vo.SpecOptionVo;
import org.dromara.goods.domain.vo.TypeTemplateVo;
import org.dromara.goods.domain.vo.TypeVo;
import org.dromara.goods.mapper.AttributeMapper;
import org.dromara.goods.mapper.AttributeOptionMapper;
import org.dromara.goods.mapper.BrandMapper;
import org.dromara.goods.mapper.ParameterMapper;
import org.dromara.goods.mapper.SpecGroupMapper;
import org.dromara.goods.mapper.SpecMapper;
import org.dromara.goods.mapper.SpecOptionMapper;
import org.dromara.goods.mapper.TypeBrandMapper;
import org.dromara.goods.mapper.TypeMapper;
import org.dromara.goods.service.ITypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 商品类型Service业务层处理
 *
 * @author ljs
 * @date 2026-07-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TypeServiceImpl implements ITypeService {

    private final TypeMapper baseMapper;
    private final TypeBrandMapper typeBrandMapper;
    private final BrandMapper brandMapper;
    private final SpecGroupMapper specGroupMapper;
    private final SpecMapper specMapper;
    private final SpecOptionMapper specOptionMapper;
    private final ParameterMapper parameterMapper;
    private final AttributeMapper attributeMapper;
    private final AttributeOptionMapper attributeOptionMapper;

    @Override
    public TypeVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<TypeVo> queryPageList(TypeBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Type> lqw = buildQueryWrapper(bo);
        Page<TypeVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<TypeVo> queryList(TypeBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<Type> buildQueryWrapper(TypeBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Type> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Type::getId);
        lqw.like(StringUtils.isNotBlank(bo.getTypeName()), Type::getTypeName, bo.getTypeName());
        return lqw;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(TypeBo bo) {
        Type add = MapstructUtils.convert(bo, Type.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
            rebuildTypeBrands(add.getId(), bo.getBrandIds());
        }
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(TypeBo bo) {
        Type update = MapstructUtils.convert(bo, Type.class);
        validEntityBeforeSave(update);
        boolean flag = baseMapper.updateById(update) > 0;
        if (flag) {
            rebuildTypeBrands(update.getId(), bo.getBrandIds());
        }
        return flag;
    }

    private void validEntityBeforeSave(Type entity) {
        // 名称唯一靠库约束
    }

    private void rebuildTypeBrands(Long typeId, List<Long> brandIds) {
        // null 表示本次不改品牌关联
        if (brandIds == null) {
            return;
        }
        typeBrandMapper.delete(Wrappers.lambdaQuery(TypeBrand.class).eq(TypeBrand::getTypeId, typeId));
        if (CollUtil.isEmpty(brandIds)) {
            return;
        }
        List<Long> distinctIds = brandIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        for (Long brandId : distinctIds) {
            Brand brand = brandMapper.selectById(brandId);
            if (brand == null) {
                throw new ServiceException("品牌不存在: " + brandId);
            }
            TypeBrand rel = new TypeBrand();
            rel.setId(IdGeneratorUtil.nextLongId());
            rel.setTypeId(typeId);
            rel.setBrandId(brandId);
            typeBrandMapper.insert(rel);
        }
    }

    @Override
    public TypeTemplateVo queryTemplate(Long typeId) {
        Type type = baseMapper.selectById(typeId);
        if (type == null) {
            throw new ServiceException("商品类型不存在");
        }
        TypeTemplateVo vo = new TypeTemplateVo();
        vo.setTypeId(type.getId());
        vo.setTypeName(type.getTypeName());

        List<TypeBrand> typeBrands = typeBrandMapper.selectList(Wrappers.lambdaQuery(TypeBrand.class)
            .eq(TypeBrand::getTypeId, typeId));
        if (CollUtil.isNotEmpty(typeBrands)) {
            List<Long> brandIds = typeBrands.stream().map(TypeBrand::getBrandId).toList();
            vo.setBrands(brandMapper.selectVoList(Wrappers.lambdaQuery(Brand.class)
                .in(Brand::getId, brandIds)
                .orderByAsc(Brand::getId)));
        }

        List<SpecGroup> groups = specGroupMapper.selectList(Wrappers.lambdaQuery(SpecGroup.class)
            .eq(SpecGroup::getTypeId, typeId)
            .orderByAsc(SpecGroup::getId));
        if (CollUtil.isNotEmpty(groups)) {
            List<Long> groupIds = groups.stream().map(SpecGroup::getId).toList();
            List<Spec> specs = specMapper.selectList(Wrappers.lambdaQuery(Spec.class)
                .in(Spec::getSpecGroupId, groupIds)
                .orderByAsc(Spec::getId));
            Map<Long, List<Spec>> specsByGroup = specs.stream().collect(Collectors.groupingBy(Spec::getSpecGroupId));
            List<Long> specIds = specs.stream().map(Spec::getId).toList();
            Map<Long, List<SpecOptionVo>> optionsBySpec = CollUtil.isEmpty(specIds)
                ? Map.of()
                : specOptionMapper.selectVoList(Wrappers.lambdaQuery(SpecOption.class)
                    .in(SpecOption::getSpecId, specIds)
                    .orderByAsc(SpecOption::getId))
                .stream().collect(Collectors.groupingBy(SpecOptionVo::getSpecId));

            List<TypeTemplateVo.SpecGroupTemplateVo> groupVos = new ArrayList<>();
            for (SpecGroup group : groups) {
                TypeTemplateVo.SpecGroupTemplateVo groupVo = new TypeTemplateVo.SpecGroupTemplateVo();
                groupVo.setId(group.getId());
                groupVo.setSpecGroupName(group.getSpecGroupName());
                List<Spec> groupSpecs = specsByGroup.getOrDefault(group.getId(), List.of());
                List<TypeTemplateVo.SpecTemplateVo> specVos = new ArrayList<>();
                for (Spec spec : groupSpecs) {
                    TypeTemplateVo.SpecTemplateVo specVo = new TypeTemplateVo.SpecTemplateVo();
                    specVo.setId(spec.getId());
                    specVo.setSpecName(spec.getSpecName());
                    specVo.setOptions(optionsBySpec.getOrDefault(spec.getId(), new ArrayList<>()));
                    specVos.add(specVo);
                }
                groupVo.setSpecs(specVos);
                groupVos.add(groupVo);
            }
            vo.setSpecGroups(groupVos);
        }

        vo.setParameters(parameterMapper.selectVoList(Wrappers.lambdaQuery(Parameter.class)
            .eq(Parameter::getTypeId, typeId)
            .orderByAsc(Parameter::getId)));

        List<AttributeVo> attributes = attributeMapper.selectVoList(Wrappers.lambdaQuery(Attribute.class)
            .eq(Attribute::getTypeId, typeId)
            .orderByAsc(Attribute::getId));
        if (CollUtil.isNotEmpty(attributes)) {
            List<Long> attrIds = attributes.stream().map(AttributeVo::getId).toList();
            Map<Long, List<AttributeOptionVo>> optionsByAttr = attributeOptionMapper.selectVoList(
                    Wrappers.lambdaQuery(AttributeOption.class)
                        .in(AttributeOption::getAttributeId, attrIds)
                        .orderByAsc(AttributeOption::getId))
                .stream().collect(Collectors.groupingBy(AttributeOptionVo::getAttributeId));
            List<TypeTemplateVo.AttributeTemplateVo> attrVos = new ArrayList<>();
            for (AttributeVo attr : attributes) {
                TypeTemplateVo.AttributeTemplateVo attrVo = new TypeTemplateVo.AttributeTemplateVo();
                attrVo.setId(attr.getId());
                attrVo.setAttributeName(attr.getAttributeName());
                attrVo.setOptions(optionsByAttr.getOrDefault(attr.getId(), new ArrayList<>()));
                attrVos.add(attrVo);
            }
            vo.setAttributes(attrVos);
        }

        return vo;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (Boolean.TRUE.equals(isValid)) {
            // 预留业务校验
        }
        typeBrandMapper.delete(Wrappers.lambdaQuery(TypeBrand.class).in(TypeBrand::getTypeId, ids));
        return baseMapper.deleteByIds(ids) > 0;
    }
}
