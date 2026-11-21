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
import org.dromara.goods.domain.bo.SpecGroupBo;
import org.dromara.goods.domain.vo.SpecGroupVo;
import org.dromara.goods.domain.SpecGroup;
import org.dromara.goods.mapper.SpecGroupMapper;
import org.dromara.goods.service.ISpecGroupService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 商品规格组Service业务层处理
 *
 * @author ljs
 * @date 2026-07-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SpecGroupServiceImpl implements ISpecGroupService {

    private final SpecGroupMapper baseMapper;

    /**
     * 查询商品规格组
     *
     * @param id 主键
     * @return 商品规格组
     */
    @Override
    public SpecGroupVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询商品规格组列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商品规格组分页列表
     */
    @Override
    public TableDataInfo<SpecGroupVo> queryPageList(SpecGroupBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SpecGroup> lqw = buildQueryWrapper(bo);
        Page<SpecGroupVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的商品规格组列表
     *
     * @param bo 查询条件
     * @return 商品规格组列表
     */
    @Override
    public List<SpecGroupVo> queryList(SpecGroupBo bo) {
        LambdaQueryWrapper<SpecGroup> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<SpecGroup> buildQueryWrapper(SpecGroupBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SpecGroup> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(SpecGroup::getId);
        lqw.eq(bo.getTypeId() != null, SpecGroup::getTypeId, bo.getTypeId());
        lqw.like(StringUtils.isNotBlank(bo.getSpecGroupName()), SpecGroup::getSpecGroupName, bo.getSpecGroupName());
        return lqw;
    }

    /**
     * 新增商品规格组
     *
     * @param bo 商品规格组
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(SpecGroupBo bo) {
        SpecGroup add = MapstructUtils.convert(bo, SpecGroup.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改商品规格组
     *
     * @param bo 商品规格组
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(SpecGroupBo bo) {
        SpecGroup update = MapstructUtils.convert(bo, SpecGroup.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(SpecGroup entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除商品规格组信息
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
