package org.dromara.order.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.order.domain.vo.MerchantReturnAddressVo;
import org.dromara.order.domain.bo.MerchantReturnAddressBo;
import org.dromara.order.service.IMerchantReturnAddressService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 商家退货收货地址库
 * 前端访问路由地址为:/order/merchantReturnAddress
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/merchantReturnAddress")
public class MerchantReturnAddressController extends BaseController {

    private final IMerchantReturnAddressService merchantReturnAddressService;

    /**
     * 查询商家退货收货地址列表
     */
    @SaCheckPermission("order:merchantReturnAddress:list")
    @GetMapping("/list")
    public TableDataInfo<MerchantReturnAddressVo> list(MerchantReturnAddressBo bo, PageQuery pageQuery) {
        return merchantReturnAddressService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出商家退货收货地址列表
     */
    @SaCheckPermission("order:merchantReturnAddress:export")
    @Log(title = "商家退货收货地址", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(MerchantReturnAddressBo bo, HttpServletResponse response) {
        List<MerchantReturnAddressVo> list = merchantReturnAddressService.queryList(bo);
        ExcelUtil.exportExcel(list, "商家退货收货地址", MerchantReturnAddressVo.class, response);
    }

    /**
     * 获取商家退货收货地址详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("order:merchantReturnAddress:query")
    @GetMapping("/{id}")
    public R<MerchantReturnAddressVo> getInfo(@NotNull(message = "主键不能为空")
                                              @PathVariable("id") Long id) {
        return R.ok(merchantReturnAddressService.queryById(id));
    }

    /**
     * 新增商家退货收货地址
     */
    @SaCheckPermission("order:merchantReturnAddress:add")
    @Log(title = "商家退货收货地址", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody MerchantReturnAddressBo bo) {
        return toAjax(merchantReturnAddressService.insertByBo(bo));
    }

    /**
     * 修改商家退货收货地址
     */
    @SaCheckPermission("order:merchantReturnAddress:edit")
    @Log(title = "商家退货收货地址", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody MerchantReturnAddressBo bo) {
        return toAjax(merchantReturnAddressService.updateByBo(bo));
    }

    /**
     * 删除商家退货收货地址
     *
     * @param ids 主键串
     */
    @SaCheckPermission("order:merchantReturnAddress:remove")
    @Log(title = "商家退货收货地址", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(merchantReturnAddressService.deleteWithValidByIds(List.of(ids), true));
    }
}
