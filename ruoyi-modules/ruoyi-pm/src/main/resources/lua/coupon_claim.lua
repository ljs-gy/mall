-- KEYS[1] 券详情 JSON
-- KEYS[2]  总库存
-- KEYS[3]  个人已领 Hash
-- KEYS[4]/[5]  用户白 / 黑名单 Set
-- KEYS[6]/[7]   等级白 / 黑名单 Set
-- ARGV[1]     userId
-- ARGV[2]     当前时间（毫秒）
-- ARGV[3]     用户等级
-- ARGV[4]/[5]    领取起止（毫秒）
-- ARGV[6]     限领 Hash 的 TTL（秒）



-- 获取卷详情
local detailJson = redis.call('GET', KEYS[1])
if not detailJson then
    return -1
end

local detail = cjson.decode(detailJson)
-- 当前时间
local now = tonumber(ARGV[2])
local userId = ARGV[1]
local userLevelId = ARGV[3]

-- 仅普通自领券
local grantType = detail.grantType
if grantType ~= 'NORMAL' then
    return -7
end

-- 只有「进行中」可领
local status = tonumber(detail.status or 0)
if status ~= 2 then
    return -1
end


local claimStart = tonumber(ARGV[4])
local claimEnd = tonumber(ARGV[5])
if claimStart and now < claimStart then
    return -2
end
if claimEnd and now > claimEnd then
    return -3
end


local scopeUserType = tonumber(detail.userScopeType or 0)
local participateType = tonumber(detail.userParticipateType or 1)

if scopeUserType == 1 then
    if participateType == 1 then
        if redis.call('SISMEMBER', KEYS[4], userId) == 0 then
            return -4
        end
    elseif participateType == 2 then
        if redis.call('SISMEMBER', KEYS[5], userId) == 1 then
            return -4
        end
    end
elseif scopeUserType == 2 then
    if (not userLevelId) or userLevelId == '' then
        return -4
    end
    if participateType == 1 then
        if redis.call('SISMEMBER', KEYS[6], userLevelId) == 0 then
            return -4
        end
    elseif participateType == 2 then
        if redis.call('SISMEMBER', KEYS[7], userLevelId) == 1 then
            return -4
        end
    end
end

-- 个人限领校验
local limitCount = tonumber(detail.userLimitCount)
local needUserLimit = false
if limitCount == -1 then
    needUserLimit = false
elseif limitCount ~= nil and limitCount > 0 then
    needUserLimit = true
    local taken = redis.call('HGET', KEYS[3], userId)
    -- 读该用户已领张数，已达上限则-5
    if taken and tonumber(taken) >= limitCount then
        return -5
    end
else
    return -5
end


-- 扣总库存
local stock = redis.call('GET', KEYS[2])
if (not stock) or tonumber(stock) <= 0 then
    return -6
end
redis.call('DECR', KEYS[2])

-- 个人限领记账 + 过期
if needUserLimit then
    redis.call('HINCRBY', KEYS[3], userId, 1)
    local ttl = tonumber(ARGV[6] or '0')
    if ttl and ttl > 0 then
        local curTtl = redis.call('TTL', KEYS[3])
        if curTtl < 0 then
            redis.call('EXPIRE', KEYS[3], ttl)
        end
    end
end

return 1
