package org.dromara.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.goods.domain.Brand;
import org.dromara.goods.domain.Spu;
import org.dromara.goods.domain.bo.BrandBo;
import org.dromara.goods.domain.vo.BrandVo;
import org.dromara.goods.mapper.BrandMapper;
import org.dromara.goods.mapper.SpuMapper;
import org.dromara.goods.mq.GoodsMqConstants;
import org.dromara.goods.service.IBrandService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 商品品牌Service业务层处理
 *
 * @author ljs
 * @date 2026-07-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BrandServiceImpl implements IBrandService {

    private final BrandMapper baseMapper;
    private final SpuMapper spuMapper;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public BrandVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public TableDataInfo<BrandVo> queryPageList(BrandBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Brand> lqw = buildQueryWrapper(bo);
        Page<BrandVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<BrandVo> queryList(BrandBo bo) {
        LambdaQueryWrapper<Brand> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Brand> buildQueryWrapper(BrandBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Brand> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Brand::getId);
        lqw.like(StringUtils.isNotBlank(bo.getBrandName()), Brand::getBrandName, bo.getBrandName());
        lqw.eq(StringUtils.isNotBlank(bo.getLogoUrl()), Brand::getLogoUrl, bo.getLogoUrl());
        return lqw;
    }

    @Override
    public Boolean insertByBo(BrandBo bo) {
        Brand add = MapstructUtils.convert(bo, Brand.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(BrandBo bo) {
        Brand old = baseMapper.selectById(bo.getId());
        Brand update = MapstructUtils.convert(bo, Brand.class);
        validEntityBeforeSave(update);
        boolean flag = baseMapper.updateById(update) > 0;
        if (!flag) {
            return false;
        }
        // 品牌改名：回写 SPU 品牌名；在架商品再刷 ES
        if (old != null && StringUtils.isNotBlank(bo.getBrandName())
            && !Objects.equals(old.getBrandName(), bo.getBrandName())) {
            spuMapper.update(null, Wrappers.lambdaUpdate(Spu.class)
                .eq(Spu::getBrandId, bo.getId())
                .set(Spu::getBrandName, bo.getBrandName()));
            List<Long> onShelfSpuIds = spuMapper.selectList(Wrappers.lambdaQuery(Spu.class)
                    .select(Spu::getId)
                    .eq(Spu::getBrandId, bo.getId())
                    .eq(Spu::getSaleStatus, 1L))
                .stream().map(Spu::getId).toList();
            afterCommit(() -> {
                for (Long spuId : onShelfSpuIds) {
                    rabbitTemplate.convertAndSend(
                        GoodsMqConstants.EXCHANGE_GOODS_DIRECT,
                        GoodsMqConstants.KEY_GOODS_SYNC_ES,
                        spuId
                    );
                }
                if (!onShelfSpuIds.isEmpty()) {
                    log.info("【品牌改名】已回写 SPU 并触发 ES 同步，brandId={}, count={}",
                        bo.getId(), onShelfSpuIds.size());
                }
            });
        }
        return true;
    }

    private void validEntityBeforeSave(Brand entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}
