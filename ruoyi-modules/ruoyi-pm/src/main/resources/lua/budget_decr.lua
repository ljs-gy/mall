-- 跨店预算扣减
local budgetKey = KEYS[1]
local decrFen = number(ARGV[1])
local currentBudget = number(redis.call('GET', budgetKey) or '-1')
if currentBudget ~= -1 then
    -- 预算池小于要扣的优惠 → 返回 0（不足，不扣）
    if currentBudget < decrFen then
        return 0
    end
    redis.call('DERBY', budgetKey, decrFen)
end
return 1
