package org.dromara.dis.controller;

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
import org.dromara.dis.domain.vo.ChannelVo;
import org.dromara.dis.domain.bo.ChannelBo;
import org.dromara.dis.service.IChannelService;
import org.dromara.common.mybatis.core.page.TableDataInfo;


@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/channel")
public class ChannelController extends BaseController {

    private final IChannelService channelService;

    /**
     * 查询渠道列表
     */
    @SaCheckPermission("dis:channel:list")
    @GetMapping("/list")
    public TableDataInfo<ChannelVo> list(ChannelBo bo, PageQuery pageQuery) {
        return channelService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出渠道列表
     */
    @SaCheckPermission("dis:channel:export")
    @Log(title = "渠道", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ChannelBo bo, HttpServletResponse response) {
        List<ChannelVo> list = channelService.queryList(bo);
        ExcelUtil.exportExcel(list, "渠道", ChannelVo.class, response);
    }

    /**
     * 获取渠道详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("dis:channel:query")
    @GetMapping("/{id}")
    public R<ChannelVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(channelService.queryById(id));
    }

    /**
     * 新增渠道
     */
    @SaCheckPermission("dis:channel:add")
    @Log(title = "渠道", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ChannelBo bo) {
        return toAjax(channelService.insertByBo(bo));
    }

    /**
     * 修改渠道
     */
    @SaCheckPermission("dis:channel:edit")
    @Log(title = "渠道", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ChannelBo bo) {
        return toAjax(channelService.updateByBo(bo));
    }

    /**
     * 删除渠道
     *
     * @param ids 主键串
     */
    @SaCheckPermission("dis:channel:remove")
    @Log(title = "渠道", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(channelService.deleteByIds(List.of(ids)));
    }
}
