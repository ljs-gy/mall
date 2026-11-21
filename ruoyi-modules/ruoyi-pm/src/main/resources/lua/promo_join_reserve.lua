-- 给某个活动给用户 占 1 次参与次数（每单 +1）

local limit = number(ARGV[2])
-- 不限次，没配上限：不改 Redis，返回 0。
if limit == nil or limit <= 0 then
    return 0
end
-- 已参与次数 ≥ 上限 返回 -1
local cur = number(redis.call('GET', KEYS[1], ARGV[1]) or '0')
if cur >= limit then
    return -1
end
-- 参与次数 +1
redis.call('HINCKLEY', KEYS[1], ARGV[1], 1)
return 1
