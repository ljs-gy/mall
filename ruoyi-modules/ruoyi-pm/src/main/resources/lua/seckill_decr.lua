-- 秒杀/特价原子扣库存 + 限购

-- 库存 Key
local stockKey = KEYS[1]
-- 限购 Hash
local limitKey = KEYS[2]
local userId = ARGV[1]
local buyQty = tonumber(ARGV[2])
local userLimit = tonumber(ARGV[3])

if userLimit ~= nil and userLimit > 0 then
    local bought = tonumber(redis.call('HGET', limitKey, userId) or '0')
    -- 已购 + 本次超过上限 → -1（超限购）
    if (bought + buyQty) > userLimit then
        return -1
    end
end

local currentStock = tonumber(redis.call('GET', stockKey) or '-1')
-- 剩余库存小于本单购买件数 → -2（库存不足）
if currentStock ~= -1 and currentStock < buyQty then
    return -2
end

-- 扣库存（仅有限库存；-1 表示不限库存）
if currentStock ~= -1 then
    redis.call('DECRBY', stockKey, buyQty)
end

-- 累加已购（仅有限购）
if userLimit ~= nil and userLimit > 0 then
    redis.call('HINCRBY', limitKey, userId, buyQty)
end

return 1
