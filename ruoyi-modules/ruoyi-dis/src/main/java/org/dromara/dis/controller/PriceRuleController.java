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
import org.dromara.dis.domain.dto.DisPriceRuleSaveDTO;
import org.dromara.dis.domain.vo.PriceRuleVo;
import org.dromara.dis.service.IPriceRuleService;
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
@RequestMapping("/priceRule")
public class PriceRuleController extends BaseController {

    private final IPriceRuleService priceRuleService;

    @SaCheckPermission("dis:priceRule:list")
    @GetMapping("/list")
    public TableDataInfo<PriceRuleVo> list(@RequestParam(required = false) String channelCode,
                                           @RequestParam(required = false) String ruleName,
                                           PageQuery pageQuery) {
        return priceRuleService.queryPageList(channelCode, ruleName, pageQuery);
    }

    @SaCheckPermission("dis:priceRule:export")
    @Log(title = "渠道价格规则", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(@RequestParam(required = false) String channelCode, HttpServletResponse response) {
        List<PriceRuleVo> list = priceRuleService.queryList(channelCode);
        ExcelUtil.exportExcel(list, "渠道价格规则", PriceRuleVo.class, response);
    }

    @SaCheckPermission("dis:priceRule:query")
    @GetMapping("/{id}")
    public R<PriceRuleVo> getInfo(@NotNull @PathVariable Long id) {
        return R.ok(priceRuleService.queryById(id));
    }


    @SaCheckPermission(value = {"dis:priceRule:add", "dis:priceRule:edit"}, mode = SaMode.OR)
    @Log(title = "渠道价格规则", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping()
    public R<Long> save(@Validated @RequestBody DisPriceRuleSaveDTO dto) {
        return R.ok(priceRuleService.saveOrUpdatePriceRule(dto));
    }

    @SaCheckPermission("dis:priceRule:remove")
    @Log(title = "渠道价格规则", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty @PathVariable Long[] ids) {
        return toAjax(priceRuleService.deleteByIds(List.of(ids)));
    }
}
