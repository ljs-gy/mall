package org.dromara.dis.service.impl;

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
import org.dromara.dis.domain.bo.DistProductCommissionBo;
import org.dromara.dis.domain.vo.DistProductCommissionVo;
import org.dromara.dis.domain.DistProductCommission;
import org.dromara.dis.mapper.DistProductCommissionMapper;
import org.dromara.dis.service.IDistProductCommissionService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 商品分佣配置Service业务层处理
 *
 * @author ljs
 * @date 2026-08-06
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DistProductCommissionServiceImpl implements IDistProductCommissionService {

    private final DistProductCommissionMapper baseMapper;

    /**
     * 查询商品分佣配置
     *
     * @param id 主键
     * @return 商品分佣配置
     */
    @Override
    public DistProductCommissionVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询商品分佣配置列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商品分佣配置分页列表
     */
    @Override
    public TableDataInfo<DistProductCommissionVo> queryPageList(DistProductCommissionBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<DistProductCommission> lqw = buildQueryWrapper(bo);
        Page<DistProductCommissionVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的商品分佣配置列表
     *
     * @param bo 查询条件
     * @return 商品分佣配置列表
     */
    @Override
    public List<DistProductCommissionVo> queryList(DistProductCommissionBo bo) {
        LambdaQueryWrapper<DistProductCommission> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<DistProductCommission> buildQueryWrapper(DistProductCommissionBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<DistProductCommission> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(DistProductCommission::getId);
        lqw.eq(bo.getSpuId() != null, DistProductCommission::getSpuId, bo.getSpuId());
        lqw.eq(bo.getLevelId() != null, DistProductCommission::getLevelId, bo.getLevelId());
        lqw.eq(bo.getFirstRate() != null, DistProductCommission::getFirstRate, bo.getFirstRate());
        lqw.eq(bo.getSecondRate() != null, DistProductCommission::getSecondRate, bo.getSecondRate());
        lqw.eq(bo.getThirdRate() != null, DistProductCommission::getThirdRate, bo.getThirdRate());
        return lqw;
    }

    /**
     * 新增商品分佣配置
     *
     * @param bo 商品分佣配置
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(DistProductCommissionBo bo) {
        DistProductCommission add = MapstructUtils.convert(bo, DistProductCommission.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改商品分佣配置
     *
     * @param bo 商品分佣配置
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(DistProductCommissionBo bo) {
        DistProductCommission update = MapstructUtils.convert(bo, DistProductCommission.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(DistProductCommission entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除商品分佣配置信息
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
