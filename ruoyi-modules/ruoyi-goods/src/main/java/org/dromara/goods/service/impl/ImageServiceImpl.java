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
import org.dromara.goods.domain.bo.ImageBo;
import org.dromara.goods.domain.vo.ImageVo;
import org.dromara.goods.domain.Image;
import org.dromara.goods.mapper.ImageMapper;
import org.dromara.goods.service.IImageService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 商品图片明细Service业务层处理
 *
 * @author ljs
 * @date 2026-07-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ImageServiceImpl implements IImageService {

    private final ImageMapper baseMapper;

    /**
     * 查询商品图片明细
     *
     * @param id 主键
     * @return 商品图片明细
     */
    @Override
    public ImageVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询商品图片明细列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商品图片明细分页列表
     */
    @Override
    public TableDataInfo<ImageVo> queryPageList(ImageBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Image> lqw = buildQueryWrapper(bo);
        Page<ImageVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的商品图片明细列表
     *
     * @param bo 查询条件
     * @return 商品图片明细列表
     */
    @Override
    public List<ImageVo> queryList(ImageBo bo) {
        LambdaQueryWrapper<Image> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Image> buildQueryWrapper(ImageBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Image> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Image::getId);
        lqw.eq(bo.getSpuId() != null, Image::getSpuId, bo.getSpuId());
        lqw.eq(bo.getSkuId() != null, Image::getSkuId, bo.getSkuId());
        lqw.eq(bo.getMediaType() != null, Image::getMediaType, bo.getMediaType());
        lqw.eq(bo.getMediaScene() != null, Image::getMediaScene, bo.getMediaScene());
        lqw.eq(StringUtils.isNotBlank(bo.getImageUrl()), Image::getImageUrl, bo.getImageUrl());
        lqw.eq(bo.getSortOrder() != null, Image::getSortOrder, bo.getSortOrder());
        return lqw;
    }

    /**
     * 新增商品图片明细
     *
     * @param bo 商品图片明细
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(ImageBo bo) {
        Image add = MapstructUtils.convert(bo, Image.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改商品图片明细
     *
     * @param bo 商品图片明细
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(ImageBo bo) {
        Image update = MapstructUtils.convert(bo, Image.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Image entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除商品图片明细信息
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
