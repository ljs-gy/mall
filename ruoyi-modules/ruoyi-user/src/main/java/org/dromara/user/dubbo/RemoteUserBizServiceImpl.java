package org.dromara.user.dubbo;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.utils.IdGeneratorUtil;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.user.api.RemoteUserBizService;
import org.dromara.user.api.domain.RemoteBuyerAddressBo;
import org.dromara.user.api.domain.RemoteBuyerContextBo;
import org.dromara.user.domain.Account;
import org.dromara.user.domain.AccountJournal;
import org.dromara.user.domain.BuyerAddress;
import org.dromara.user.domain.Store;
import org.dromara.user.domain.UserIdentityRel;
import org.dromara.user.domain.Userinfo;
import org.dromara.user.mapper.AccountJournalMapper;
import org.dromara.user.mapper.AccountMapper;
import org.dromara.user.mapper.BuyerAddressMapper;
import org.dromara.user.mapper.StoreMapper;
import org.dromara.user.mapper.UserIdentityRelMapper;
import org.dromara.user.mapper.UserinfoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * 业务用户中心远程服务
 *
 * @author ljs
 * @date 2026-08-08
 */
@RequiredArgsConstructor
@Service
@DubboService
public class RemoteUserBizServiceImpl implements RemoteUserBizService {

    private static final long STORE_STATUS_NORMAL = 1L;
    private static final long IDENTITY_ENABLED = 1L;
    private static final long ACCOUNT_STATUS_NORMAL = 1L;
    private static final long JOURNAL_DIR_IN = 1L;
    private static final String ACCOUNT_TYPE_COMMISSION = "COMMISSION";
    private static final String BIZ_TYPE_COMMISSION = "COMMISSION";
    private static final String CURRENCY_CNY = "CNY";

    private final UserinfoMapper userinfoMapper;
    private final BuyerAddressMapper buyerAddressMapper;
    private final StoreMapper storeMapper;
    private final UserIdentityRelMapper userIdentityRelMapper;
    private final AccountMapper accountMapper;
    private final AccountJournalMapper accountJournalMapper;

    @Override
    public RemoteBuyerContextBo getBuyerContext(Long userinfoId) {
        if (userinfoId == null) {
            return null;
        }
        Userinfo userinfo = userinfoMapper.selectById(userinfoId);
        if (userinfo == null) {
            return null;
        }
        RemoteBuyerContextBo bo = new RemoteBuyerContextBo();
        bo.setUserinfoId(userinfo.getId());
        bo.setUserinfoCode(userinfo.getUserinfoCode());
        bo.setOrgCode(userinfo.getSalesmanOrgCode());
        bo.setSalesmanDeptId(userinfo.getSalesmanDeptId());
        bo.setSalesmanEmpCode(userinfo.getSalesmanEmpCode());
        bo.setProvinceCode(userinfo.getProvinceCode());
        bo.setCityCode(userinfo.getCityCode());
        bo.setDistrictCode(userinfo.getDistrictCode());
        bo.setIsBlacklisted(userinfo.getIsBlacklisted());
        bo.setDistLevelId(userinfo.getDistLevelId());
        return bo;
    }

    @Override
    public RemoteBuyerAddressBo getBuyerAddress(Long addressId) {
        if (addressId == null) {
            return null;
        }
        BuyerAddress address = buyerAddressMapper.selectById(addressId);
        return toRemoteAddress(address);
    }

    @Override
    public RemoteBuyerAddressBo getDefaultBuyerAddress(Long userinfoId) {
        if (userinfoId == null) {
            return null;
        }
        BuyerAddress address = buyerAddressMapper.selectOne(
            Wrappers.<BuyerAddress>lambdaQuery()
                .eq(BuyerAddress::getUserinfoId, userinfoId)
                .eq(BuyerAddress::getIsDefault, 1L)
                .orderByDesc(BuyerAddress::getId)
                .last("LIMIT 1")
        );
        return toRemoteAddress(address);
    }

    @Override
    public String getUserinfoCode(Long userinfoId) {
        if (userinfoId == null) {
            return null;
        }
        Userinfo userinfo = userinfoMapper.selectById(userinfoId);
        return userinfo == null ? null : userinfo.getUserinfoCode();
    }

    @Override
    public Long getDistLevelId(Long userinfoId) {
        if (userinfoId == null) {
            return null;
        }
        Userinfo userinfo = userinfoMapper.selectById(userinfoId);
        return userinfo == null ? null : userinfo.getDistLevelId();
    }

    @Override
    public String getDefaultMemberCode(Long userinfoId) {
        if (userinfoId == null) {
            return null;
        }
        List<Store> stores = storeMapper.selectList(
            Wrappers.<Store>lambdaQuery()
                .eq(Store::getUserinfoId, userinfoId)
                .eq(Store::getStatus, STORE_STATUS_NORMAL)
                .orderByAsc(Store::getId)
                .last("LIMIT 2")
        );
        if (stores == null || stores.size() != 1) {
            return null;
        }
        String storeNo = stores.get(0).getStoreNo();
        return StringUtils.isBlank(storeNo) ? null : storeNo;
    }

    @Override
    public List<Long> pageBuyerUserinfoIds(String tenantId, String channelCode, long lastId, int size) {
        return TenantHelper.dynamic(tenantId,
            () -> userinfoMapper.selectBuyerUserinfoIdsAfter(channelCode, lastId, size));
    }

    @Override
    public List<Long> listBuyerUserinfoIdsByDistLevels(String tenantId, String channelCode, List<Long> distLevelIds) {
        if (StringUtils.isBlank(tenantId) || StringUtils.isBlank(channelCode) || CollUtil.isEmpty(distLevelIds)) {
            return Collections.emptyList();
        }
        return TenantHelper.dynamic(tenantId,
            () -> userinfoMapper.selectBuyerUserinfoIdsByDistLevels(channelCode, distLevelIds));
    }

    @Override
    public Long getUserinfoIdByInviteCode(String inviteCode) {
        if (StringUtils.isBlank(inviteCode)) {
            return null;
        }
        Userinfo userinfo = userinfoMapper.selectOne(
            Wrappers.<Userinfo>lambdaQuery()
                .eq(Userinfo::getInviteCode, inviteCode.trim())
                .last("LIMIT 1")
        );
        return userinfo == null ? null : userinfo.getId();
    }

    @Override
    public boolean hasIdentity(Long userinfoId, String identityType) {
        if (userinfoId == null || StringUtils.isBlank(identityType)) {
            return false;
        }
        Long cnt = userIdentityRelMapper.selectCount(
            Wrappers.<UserIdentityRel>lambdaQuery()
                .eq(UserIdentityRel::getUserinfoId, userinfoId)
                .eq(UserIdentityRel::getIdentityType, identityType)
                .eq(UserIdentityRel::getStatus, IDENTITY_ENABLED)
        );
        return cnt != null && cnt > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String creditCommission(Long userinfoId, BigDecimal amount, String bizOrderNo) {
        if (userinfoId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0
            || StringUtils.isBlank(bizOrderNo)) {
            return null;
        }
        AccountJournal existed = accountJournalMapper.selectOne(
            Wrappers.<AccountJournal>lambdaQuery()
                .eq(AccountJournal::getBizType, BIZ_TYPE_COMMISSION)
                .eq(AccountJournal::getBizOrderNo, bizOrderNo)
                .last("LIMIT 1")
        );
        if (existed != null) {
            return existed.getJournalNo();
        }

        Account account = getOrCreateCommissionAccount(userinfoId);
        BigDecimal before = account.getBalance() == null ? BigDecimal.ZERO : account.getBalance();
        BigDecimal available = account.getAvailableBalance() == null ? BigDecimal.ZERO : account.getAvailableBalance();
        BigDecimal after = before.add(amount);
        account.setBalance(after);
        account.setAvailableBalance(available.add(amount));
        if (accountMapper.updateById(account) <= 0) {
            throw new ServiceException("佣金入账失败，请重试");
        }

        AccountJournal journal = new AccountJournal();
        journal.setJournalNo(IdGeneratorUtil.nextIdWithPrefix("AJ"));
        journal.setAccountNo(account.getAccountNo());
        journal.setBizType(BIZ_TYPE_COMMISSION);
        journal.setBizOrderNo(bizOrderNo);
        journal.setDirection(JOURNAL_DIR_IN);
        journal.setAmount(amount);
        journal.setBeforeBalance(before);
        journal.setAfterBalance(after);
        accountJournalMapper.insert(journal);
        return journal.getJournalNo();
    }

    private Account getOrCreateCommissionAccount(Long userinfoId) {
        Account account = accountMapper.selectOne(
            Wrappers.<Account>lambdaQuery()
                .eq(Account::getUserinfoId, userinfoId)
                .eq(Account::getAccountType, ACCOUNT_TYPE_COMMISSION)
                .eq(Account::getCurrency, CURRENCY_CNY)
                .last("LIMIT 1")
        );
        if (account != null) {
            return account;
        }
        Account created = new Account();
        created.setUserinfoId(userinfoId);
        created.setAccountNo(IdGeneratorUtil.nextIdWithPrefix("AC"));
        created.setAccountName("分销佣金户");
        created.setAccountType(ACCOUNT_TYPE_COMMISSION);
        created.setCurrency(CURRENCY_CNY);
        created.setBalance(BigDecimal.ZERO);
        created.setAvailableBalance(BigDecimal.ZERO);
        created.setFrozenBalance(BigDecimal.ZERO);
        created.setStatus(ACCOUNT_STATUS_NORMAL);
        accountMapper.insert(created);
        return created;
    }

    private RemoteBuyerAddressBo toRemoteAddress(BuyerAddress address) {
        if (address == null) {
            return null;
        }
        RemoteBuyerAddressBo bo = new RemoteBuyerAddressBo();
        bo.setAddressId(address.getId());
        bo.setUserinfoId(address.getUserinfoId());
        bo.setConsigneeName(address.getConsigneeName());
        bo.setConsigneeMobile(address.getConsigneeMobile());
        bo.setProvinceCode(address.getProvinceCode());
        bo.setProvinceName(address.getProvinceName());
        bo.setCityCode(address.getCityCode());
        bo.setCityName(address.getCityName());
        bo.setDistrictCode(address.getDistrictCode());
        bo.setDistrictName(address.getDistrictName());
        bo.setDetailAddress(address.getDetailAddress());
        bo.setIsDefault(address.getIsDefault() == null ? null : address.getIsDefault().intValue());
        return bo;
    }
}
