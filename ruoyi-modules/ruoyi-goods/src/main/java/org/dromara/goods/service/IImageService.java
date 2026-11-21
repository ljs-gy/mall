package org.dromara.goods.service;

import org.dromara.goods.domain.Image;
import org.dromara.goods.domain.vo.ImageVo;
import org.dromara.goods.domain.bo.ImageBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 商品图片明细Service接口
 *
 * @author ljs
 * @date 2026-07-28
 */
public interface IImageService {

    /**
     * 查询商品图片明细
     *
     * @param id 主键
     * @return 商品图片明细
     */
    ImageVo queryById(Long id);

    /**
     * 分页查询商品图片明细列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 商品图片明细分页列表
     */
    TableDataInfo<ImageVo> queryPageList(ImageBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的商品图片明细列表
     *
     * @param bo 查询条件
     * @return 商品图片明细列表
     */
    List<ImageVo> queryList(ImageBo bo);

    /**
     * 新增商品图片明细
     *
     * @param bo 商品图片明细
     * @return 是否新增成功
     */
    Boolean insertByBo(ImageBo bo);

    /**
     * 修改商品图片明细
     *
     * @param bo 商品图片明细
     * @return 是否修改成功
     */
    Boolean updateByBo(ImageBo bo);

    /**
     * 校验并批量删除商品图片明细信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
