package org.dromara.dis.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.dis.domain.dto.DisStockRuleSaveDTO;
import org.dromara.dis.domain.vo.StockRuleVo;
import org.dromara.dis.service.IStockRuleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/stockRule")
public class StockRuleController extends BaseController {

    private final IStockRuleService stockRuleService;

    @SaCheckPermission("dis:stockRule:list")
    @GetMapping("/list")
    public TableDataInfo<StockRuleVo> list(@RequestParam(required = false) String channelCode,
                                           @RequestParam(required = false) String ruleName,
                                           PageQuery pageQuery) {
        return stockRuleService.queryPageList(channelCode, ruleName, pageQuery);
    }

    @SaCheckPermission("dis:stockRule:export")
    @Log(title = "渠道库存规则", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(@RequestParam(required = false) String channelCode, HttpServletResponse response) {
        List<StockRuleVo> list = stockRuleService.queryList(channelCode);
        ExcelUtil.exportExcel(list, "渠道库存规则", StockRuleVo.class, response);
    }

    @SaCheckPermission("dis:stockRule:query")
    @GetMapping("/{id}")
    public R<StockRuleVo> getInfo(@NotNull @PathVariable Long id) {
        return R.ok(stockRuleService.queryById(id));
    }


    @SaCheckPermission(value = {"dis:stockRule:add", "dis:stockRule:edit"}, mode = SaMode.OR)
    @Log(title = "渠道库存规则", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping()
    public R<Long> save(@Validated @RequestBody DisStockRuleSaveDTO dto) {
        return R.ok(stockRuleService.saveOrUpdateStockRule(dto));
    }

    @SaCheckPermission("dis:stockRule:remove")
    @Log(title = "渠道库存规则", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty @PathVariable Long[] ids) {
        return toAjax(stockRuleService.deleteByIds(List.of(ids)));
    }
}
