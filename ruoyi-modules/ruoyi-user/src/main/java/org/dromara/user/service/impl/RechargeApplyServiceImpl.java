package org.dromara.user.service.impl;

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
import org.dromara.user.domain.bo.RechargeApplyBo;
import org.dromara.user.domain.vo.RechargeApplyVo;
import org.dromara.user.domain.RechargeApply;
import org.dromara.user.mapper.RechargeApplyMapper;
import org.dromara.user.service.IRechargeApplyService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 充值申请Service业务层处理
 *
 * @author ljs
 * @date 2026-07-27
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class RechargeApplyServiceImpl implements IRechargeApplyService {

    private final RechargeApplyMapper baseMapper;

    /**
     * 查询充值申请
     *
     * @param id 主键
     * @return 充值申请
     */
    @Override
    public RechargeApplyVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询充值申请列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 充值申请分页列表
     */
    @Override
    public TableDataInfo<RechargeApplyVo> queryPageList(RechargeApplyBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<RechargeApply> lqw = buildQueryWrapper(bo);
        Page<RechargeApplyVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的充值申请列表
     *
     * @param bo 查询条件
     * @return 充值申请列表
     */
    @Override
    public List<RechargeApplyVo> queryList(RechargeApplyBo bo) {
        LambdaQueryWrapper<RechargeApply> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<RechargeApply> buildQueryWrapper(RechargeApplyBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<RechargeApply> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(RechargeApply::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getApplyNo()), RechargeApply::getApplyNo, bo.getApplyNo());
        lqw.eq(bo.getUserinfoId() != null, RechargeApply::getUserinfoId, bo.getUserinfoId());
        lqw.eq(StringUtils.isNotBlank(bo.getUserCode()), RechargeApply::getUserCode, bo.getUserCode());
        lqw.like(StringUtils.isNotBlank(bo.getUserinfoName()), RechargeApply::getUserinfoName, bo.getUserinfoName());
        lqw.eq(StringUtils.isNotBlank(bo.getAccountNo()), RechargeApply::getAccountNo, bo.getAccountNo());
        lqw.eq(bo.getRechargeAmount() != null, RechargeApply::getRechargeAmount, bo.getRechargeAmount());
        lqw.eq(StringUtils.isNotBlank(bo.getVoucherNo()), RechargeApply::getVoucherNo, bo.getVoucherNo());
        lqw.eq(StringUtils.isNotBlank(bo.getContractNo()), RechargeApply::getContractNo, bo.getContractNo());
        lqw.eq(StringUtils.isNotBlank(bo.getVoucherUrls()), RechargeApply::getVoucherUrls, bo.getVoucherUrls());
        lqw.eq(StringUtils.isNotBlank(bo.getSummary()), RechargeApply::getSummary, bo.getSummary());
        lqw.eq(bo.getAuditStatus() != null, RechargeApply::getAuditStatus, bo.getAuditStatus());
        lqw.eq(bo.getAuditTime() != null, RechargeApply::getAuditTime, bo.getAuditTime());
        lqw.eq(bo.getAuditUserId() != null, RechargeApply::getAuditUserId, bo.getAuditUserId());
        lqw.like(StringUtils.isNotBlank(bo.getAuditUserName()), RechargeApply::getAuditUserName, bo.getAuditUserName());
        lqw.eq(StringUtils.isNotBlank(bo.getAuditRemark()), RechargeApply::getAuditRemark, bo.getAuditRemark());
        return lqw;
    }

    /**
     * 新增充值申请
     *
     * @param bo 充值申请
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(RechargeApplyBo bo) {
        RechargeApply add = MapstructUtils.convert(bo, RechargeApply.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改充值申请
     *
     * @param bo 充值申请
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(RechargeApplyBo bo) {
        RechargeApply update = MapstructUtils.convert(bo, RechargeApply.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(RechargeApply entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除充值申请信息
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
