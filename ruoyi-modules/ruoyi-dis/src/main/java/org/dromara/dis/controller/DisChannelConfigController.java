package org.dromara.dis.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.web.core.BaseController;
import org.dromara.dis.domain.dto.DisGoodsPublishConfigSaveDTO;
import org.dromara.dis.domain.dto.DisGoodsScopeSaveDTO;
import org.dromara.dis.domain.dto.DisUserScopeSaveDTO;
import org.dromara.dis.domain.vo.DgoodsBuyVo;
import org.dromara.dis.domain.vo.DgoodsScopelistVo;
import org.dromara.dis.domain.vo.DgoodsVo;
import org.dromara.dis.service.IDisChannelConfigService;
import org.dromara.dis.service.IDisGoodsSyncService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/channelConfig")
public class DisChannelConfigController extends BaseController {

    private final IDisChannelConfigService channelConfigService;
    private final IDisGoodsSyncService goodsSyncService;


    @SaCheckPermission("dis:channel:edit")
    @Log(title = "渠道用户范围", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/userScope/batch")
    public R<Void> batchSaveUserScope(@Validated @RequestBody DisUserScopeSaveDTO dto) {
        channelConfigService.batchSaveUserScope(dto);
        return R.ok();
    }

    @SaCheckPermission("dis:channel:query")
    @GetMapping("/userScope/{channelCode}")
    public R<List<DgoodsBuyVo>> listUserScope(@NotBlank @PathVariable String channelCode) {
        return R.ok(channelConfigService.listUserScope(channelCode));
    }

    @SaCheckPermission("dis:channel:edit")
    @Log(title = "渠道用户范围", businessType = BusinessType.DELETE)
    @DeleteMapping("/userScope/{ids}")
    public R<Void> deleteUserScope(@NotEmpty @PathVariable Long[] ids) {
        channelConfigService.deleteUserScopeByIds(List.of(ids));
        return R.ok();
    }

    @SaCheckPermission("dis:channel:edit")
    @Log(title = "渠道商品发布设置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/publishConfig")
    public R<Void> savePublishConfig(@Validated @RequestBody DisGoodsPublishConfigSaveDTO dto) {
        channelConfigService.saveOrUpdatePublishConfig(dto);
        return R.ok();
    }

    @SaCheckPermission("dis:channel:query")
    @GetMapping("/publishConfig/{channelCode}")
    public R<DgoodsVo> getPublishConfig(@NotBlank @PathVariable String channelCode) {
        return R.ok(channelConfigService.getPublishConfig(channelCode));
    }


    @SaCheckPermission("dis:channel:edit")
    @Log(title = "渠道商品选品范围", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/goodsScope/batch")
    public R<Void> batchSaveGoodsScope(@Validated @RequestBody DisGoodsScopeSaveDTO dto) {
        channelConfigService.batchSaveGoodsScope(dto);
        return R.ok();
    }

    @SaCheckPermission("dis:channel:query")
    @GetMapping("/goodsScope/{channelCode}")
    public R<List<DgoodsScopelistVo>> listGoodsScope(@NotBlank @PathVariable String channelCode) {
        return R.ok(channelConfigService.listGoodsScope(channelCode));
    }

    @SaCheckPermission("dis:channel:edit")
    @Log(title = "渠道商品选品范围", businessType = BusinessType.DELETE)
    @DeleteMapping("/goodsScope/{ids}")
    public R<Void> deleteGoodsScope(@NotEmpty @PathVariable Long[] ids) {
        channelConfigService.deleteGoodsScopeByIds(List.of(ids));
        return R.ok();
    }

    @SaCheckPermission("dis:channel:edit")
    @Log(title = "渠道手动同步商品", businessType = BusinessType.OTHER)
    @RepeatSubmit()
    @PostMapping("/manualSync/{channelCode}")
    public R<Void> manualSync(@NotBlank @PathVariable String channelCode) {
        goodsSyncService.startManualGoodsSync(LoginHelper.getTenantId(), channelCode);
        return R.ok();
    }
}
