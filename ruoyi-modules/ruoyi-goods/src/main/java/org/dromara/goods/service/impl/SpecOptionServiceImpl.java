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
import org.dromara.goods.domain.bo.SpecOptionBo;
import org.dromara.goods.domain.vo.SpecOptionVo;
import org.dromara.goods.domain.SpecOption;
import org.dromara.goods.mapper.SpecOptionMapper;
import org.dromara.goods.service.ISpecOptionService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 商品规格选项Service业务层处理
 *
 * @author ljs
 * @date 2026-07-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SpecOptionServiceImpl implements ISpecOptionService {

    private final SpecOptionMapper baseMapper;

    /**
     * 查询商品规格选项
     *
     * @param id 主键
     * @return 商品规格选项
     */
    @Override
    public SpecOptionVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询商品规格选项列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商品规格选项分页列表
     */
    @Override
    public TableDataInfo<SpecOptionVo> queryPageList(SpecOptionBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SpecOption> lqw = buildQueryWrapper(bo);
        Page<SpecOptionVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的商品规格选项列表
     *
     * @param bo 查询条件
     * @return 商品规格选项列表
     */
    @Override
    public List<SpecOptionVo> queryList(SpecOptionBo bo) {
        LambdaQueryWrapper<SpecOption> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<SpecOption> buildQueryWrapper(SpecOptionBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SpecOption> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(SpecOption::getId);
        lqw.eq(bo.getSpecId() != null, SpecOption::getSpecId, bo.getSpecId());
        lqw.like(StringUtils.isNotBlank(bo.getSpecOptionName()), SpecOption::getSpecOptionName, bo.getSpecOptionName());
        return lqw;
    }

    /**
     * 新增商品规格选项
     *
     * @param bo 商品规格选项
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(SpecOptionBo bo) {
        SpecOption add = MapstructUtils.convert(bo, SpecOption.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改商品规格选项
     *
     * @param bo 商品规格选项
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(SpecOptionBo bo) {
        SpecOption update = MapstructUtils.convert(bo, SpecOption.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(SpecOption entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除商品规格选项信息
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
