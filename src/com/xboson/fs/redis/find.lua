-- -----------------------------------------------------------------------------
--
-- Copyright 2017 本文件属于 xBoson 项目, 该项目由 J.yanming 维护,
-- 本文件和项目的全部权利由 [荆彦铭] 和 [王圣波] 个人所有, 如有对本文件或项目的任何修改,
-- 必须通知权利人; 该项目非开源项目, 任何将本文件和项目未经权利人同意而发行给第三方
-- 的行为都属于侵权行为, 权利人有权对侵权的个人和企业进行索赔; 未经其他合同约束而
-- 由本项目(程序)引起的计算机软件/硬件问题, 本项目权利人不负任何责任, 切不对此做任何承诺.
--
-- 文件创建日期: 17-12-18 17:19
-- 原始文件路径: D:/javaee-project/xBoson/src/com/xboson/fs/ui/find.lua
-- 授权说明版本: 1.1
--
-- [ J.yanming - Q.412475540 ]
--
-- -----------------------------------------------------------------------------

--
-- 数组开始于 1
-- caseSensitive = true 效率较高, 否则将进行很复杂的表达式比较
--
local find_arr      = {};
local cursor        = '0';
local key           = KEYS[1];
local what          = ARGV[1];
local basePath      = ARGV[2];
local caseSensitive = ARGV[3] == "true";
local max           = tonumber(ARGV[4] or 20);
local fileExt       = { '*.html', '*.htm', '*.css', '*.js',
                        '*.md', '*.markdown', '*.txt', '*.json' };

--
-- 转换为小写效率非常低, 低到好几十秒才能返回, 不可用.
-- 该方法将被比较字符串转换为支持大小写比较的表达式与被比较文件进行比较, 性能很好.
--
local function case_insensitive_pattern(pattern)
    --
    -- find an optional '%' (group 1) followed by any character (group 2)
    --
    local p = pattern:gsub("(%%?)(.)", function(percent, letter)
        if percent ~= "" or not letter:match("%a") then
            --
            -- if the '%' matched, or `letter` is not a letter, return "as is"
            --
            return percent .. letter
        else
            --
            -- else, return a case-insensitive character class of the matched letter
            --
            return string.format("[%s%s]", letter:lower(), letter:upper())
        end
    end)

    return p
end


local function findContent(fileNameMatch)
    local file_pattern = basePath .. fileNameMatch

    while (true) do
        local ret = redis.call('hscan', key, cursor, "MATCH", file_pattern);
        cursor = ret[1]

        if (next(ret[2])) then
            local len = #ret[2];

            for t = 1, len, 2 do
                local filename = ret[2][t]
                local filecontent = ret[2][t + 1]

                if (string.find(filecontent, what, 1, caseSensitive)) then
                    table.insert(find_arr, filename);
                    max = max - 1;
                end

                if (max <= 0) then
                    return;
                end
            end
        end

        if (cursor == '0' or max <= 0) then
            return;
        end
    end
end


if (caseSensitive == false) then
    what = case_insensitive_pattern(what);
end


for _, file_name_pattern in pairs(fileExt) do
    findContent(file_name_pattern)
    if (max <= 0) then
        break
    end
end


return { find_arr, basePath, what, caseSensitive, max <= 0 };