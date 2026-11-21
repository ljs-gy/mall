package org.dromara.pm.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.dromara.pm.domain.vo.PromoMutexGroupVo;
import org.dromara.pm.service.IPromoMutexGroupService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 营销互斥组配置
 * 前端访问路由地址为:/pm/promoMutexGroup
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/promoMutexGroup")
public class PromoMutexGroupController extends BaseController {

    private final IPromoMutexGroupService promoMutexGroupService;

    /**
     * 查询启用中的互斥组配置
     */
    @SaCheckPermission("pm:promoActivity:list")
    @GetMapping("/list")
    public R<List<PromoMutexGroupVo>> list() {
        return R.ok(promoMutexGroupService.listEnabled());
    }

}
