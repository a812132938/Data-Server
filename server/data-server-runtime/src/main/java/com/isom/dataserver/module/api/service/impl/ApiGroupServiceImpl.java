package com.isom.dataserver.module.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import com.isom.dataserver.module.api.dto.ApiGroupReq;
import com.isom.dataserver.module.api.entity.ApiDefinition;
import com.isom.dataserver.module.api.entity.ApiGroup;
import com.isom.dataserver.module.api.mapper.ApiDefinitionMapper;
import com.isom.dataserver.module.api.mapper.ApiGroupMapper;
import com.isom.dataserver.module.api.service.ApiGroupService;
import com.isom.dataserver.module.api.vo.ApiGroupTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApiGroupServiceImpl implements ApiGroupService {

    private final ApiGroupMapper apiGroupMapper;
    private final ApiDefinitionMapper apiDefinitionMapper;

    @Override
    public List<ApiGroupTreeVO> tree() {
        List<ApiGroup> all = apiGroupMapper.selectList(
                new LambdaQueryWrapper<ApiGroup>().orderByAsc(ApiGroup::getSort));
        return buildTree(all, 0L);
    }

    @Override
    public Map<String, Object> create(ApiGroupReq req) {
        Long parentId = req.getParentId() != null ? req.getParentId() : 0L;
        checkNameUnique(req.getName(), parentId, null);

        ApiGroup entity = new ApiGroup();
        entity.setName(req.getName());
        entity.setParentId(parentId);
        entity.setSort(req.getSort() != null ? req.getSort() : 0);
        entity.setDescription(req.getDescription());
        apiGroupMapper.insert(entity);

        Map<String, Object> result = new HashMap<>();
        result.put("id", entity.getId());
        return result;
    }

    @Override
    public Map<String, Object> update(Long id, ApiGroupReq req) {
        ApiGroup entity = apiGroupMapper.selectById(id);
        if (entity == null) throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "分组不存在");

        Long parentId = req.getParentId() != null ? req.getParentId() : entity.getParentId();
        checkNameUnique(req.getName(), parentId, id);

        entity.setName(req.getName());
        entity.setParentId(parentId);
        entity.setSort(req.getSort() != null ? req.getSort() : entity.getSort());
        entity.setDescription(req.getDescription());
        apiGroupMapper.updateById(entity);

        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        return result;
    }

    @Override
    public void delete(Long id) {
        Long childCount = apiGroupMapper.selectCount(
                new LambdaQueryWrapper<ApiGroup>().eq(ApiGroup::getParentId, id));
        if (childCount > 0) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "存在子分组，无法删除");
        }
        Long apiCount = apiDefinitionMapper.selectCount(
                new LambdaQueryWrapper<ApiDefinition>().eq(ApiDefinition::getGroupId, id));
        if (apiCount > 0) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "分组下存在 API，无法删除");
        }
        apiGroupMapper.deleteById(id);
    }

    private void checkNameUnique(String name, Long parentId, Long excludeId) {
        LambdaQueryWrapper<ApiGroup> wrapper = new LambdaQueryWrapper<ApiGroup>()
                .eq(ApiGroup::getName, name)
                .eq(ApiGroup::getParentId, parentId);
        if (excludeId != null) wrapper.ne(ApiGroup::getId, excludeId);
        if (apiGroupMapper.selectCount(wrapper) > 0) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "同级分组名称重复");
        }
    }

    private List<ApiGroupTreeVO> buildTree(List<ApiGroup> all, Long parentId) {
        return all.stream()
                .filter(g -> Objects.equals(g.getParentId(), parentId))
                .map(g -> {
                    ApiGroupTreeVO vo = new ApiGroupTreeVO();
                    vo.setId(g.getId());
                    vo.setName(g.getName());
                    vo.setParentId(g.getParentId());
                    vo.setSort(g.getSort());
                    vo.setDescription(g.getDescription());
                    vo.setChildren(buildTree(all, g.getId()));
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
