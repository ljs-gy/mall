package org.dromara.user.controller;

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
import org.dromara.user.domain.vo.BuyerAddressVo;
import org.dromara.user.domain.bo.BuyerAddressBo;
import org.dromara.user.service.IBuyerAddressService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 买家收货地址
 * 前端访问路由地址为:/user/buyerAddress
 *
 * @author ljs
 * @date 2026-08-20
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/buyerAddress")
public class BuyerAddressController extends BaseController {

    private final IBuyerAddressService buyerAddressService;

    /**
     * 查询买家收货地址列表
     */
    @SaCheckPermission("user:buyerAddress:list")
    @GetMapping("/list")
    public TableDataInfo<BuyerAddressVo> list(BuyerAddressBo bo, PageQuery pageQuery) {
        return buyerAddressService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出买家收货地址列表
     */
    @SaCheckPermission("user:buyerAddress:export")
    @Log(title = "买家收货地址", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(BuyerAddressBo bo, HttpServletResponse response) {
        List<BuyerAddressVo> list = buyerAddressService.queryList(bo);
        ExcelUtil.exportExcel(list, "买家收货地址", BuyerAddressVo.class, response);
    }

    /**
     * 获取买家收货地址详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("user:buyerAddress:query")
    @GetMapping("/{id}")
    public R<BuyerAddressVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(buyerAddressService.queryById(id));
    }

    /**
     * 新增买家收货地址
     */
    @SaCheckPermission("user:buyerAddress:add")
    @Log(title = "买家收货地址", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody BuyerAddressBo bo) {
        return toAjax(buyerAddressService.insertByBo(bo));
    }

    /**
     * 修改买家收货地址
     */
    @SaCheckPermission("user:buyerAddress:edit")
    @Log(title = "买家收货地址", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody BuyerAddressBo bo) {
        return toAjax(buyerAddressService.updateByBo(bo));
    }

    /**
     * 删除买家收货地址
     *
     * @param ids 主键串
     */
    @SaCheckPermission("user:buyerAddress:remove")
    @Log(title = "买家收货地址", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(buyerAddressService.deleteWithValidByIds(List.of(ids), true));
    }
}
