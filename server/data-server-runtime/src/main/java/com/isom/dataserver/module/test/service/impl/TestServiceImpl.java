package com.isom.dataserver.module.test.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import com.isom.dataserver.module.api.entity.ApiDefinition;
import com.isom.dataserver.module.api.entity.ApiParam;
import com.isom.dataserver.module.api.entity.ApiResponseField;
import com.isom.dataserver.module.api.mapper.ApiDefinitionMapper;
import com.isom.dataserver.module.api.mapper.ApiParamMapper;
import com.isom.dataserver.module.api.mapper.ApiResponseFieldMapper;
import com.isom.dataserver.module.datasource.service.DatasourcePoolManager;
import com.isom.dataserver.module.gateway.engine.BoundSqlResult;
import com.isom.dataserver.module.gateway.engine.SqlExecuteEngine;
import com.isom.dataserver.module.gateway.engine.SqlRenderEngine;
import com.isom.dataserver.module.gateway.masker.DataMasker;
import com.isom.dataserver.module.gateway.masker.MaskResult;
import com.isom.dataserver.module.test.dto.RunTestReq;
import com.isom.dataserver.module.test.dto.TestCaseReq;
import com.isom.dataserver.module.test.entity.TestCase;
import com.isom.dataserver.module.test.entity.TestRun;
import com.isom.dataserver.module.test.mapper.TestCaseMapper;
import com.isom.dataserver.module.test.mapper.TestRunMapper;
import com.isom.dataserver.module.test.service.TestService;
import com.isom.dataserver.module.test.vo.RunTestResp;
import com.isom.dataserver.module.test.vo.TestCaseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.isom.dataserver.common.page.PageQuery;
import com.isom.dataserver.common.page.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final ApiDefinitionMapper apiDefinitionMapper;
    private final ApiParamMapper apiParamMapper;
    private final ApiResponseFieldMapper apiResponseFieldMapper;
    private final TestCaseMapper testCaseMapper;
    private final TestRunMapper testRunMapper;
    private final DatasourcePoolManager poolManager;
    private final SqlRenderEngine sqlRenderEngine;
    private final SqlExecuteEngine sqlExecuteEngine;
    private final DataMasker dataMasker;
    private final ObjectMapper objectMapper;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public RunTestResp runTest(Long apiId, RunTestReq req) {
        return doRunTest(apiId, req, null);
    }

    private RunTestResp doRunTest(Long apiId, RunTestReq req, Long caseId) {
        ApiDefinition api = apiDefinitionMapper.selectById(apiId);
        if (api == null) throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "API 不存在");

        // Transition to TESTING if DRAFT
        if ("DRAFT".equals(api.getStatus())) {
            api.setStatus("TESTING");
            apiDefinitionMapper.updateById(api);
        }

        long start = System.currentTimeMillis();
        RunTestResp resp = new RunTestResp();
        TestRun run = new TestRun();
        run.setApiId(apiId);
        run.setCaseId(caseId);
        run.setRunType(caseId != null ? "BATCH" : "SINGLE");
        run.setExecutedBy(1L); // Phase 1: fixed admin user

        try {
            // Auto-wrap LIKE param values with %
            Map<String, Object> params = req.getParams() != null ? new HashMap<>(req.getParams()) : new HashMap<>();
            List<ApiParam> apiParams = apiParamMapper.selectList(
                    new LambdaQueryWrapper<ApiParam>().eq(ApiParam::getApiId, apiId));
            for (ApiParam ap : apiParams) {
                if ("LIKE".equalsIgnoreCase(ap.getOperator())) {
                    Object val = params.get(ap.getName());
                    if (val != null && !val.toString().contains("%")) {
                        params.put(ap.getName(), "%" + val + "%");
                    }
                }
            }

            BoundSqlResult boundSql = sqlRenderEngine.render(api.getSqlTemplate(), params);
            run.setInputParams(objectMapper.writeValueAsString(params));

            try (Connection conn = poolManager.getConnection(api.getDatasourceId())) {
                int timeout = api.getTimeoutMs() != null ? api.getTimeoutMs() / 1000 : 30;
                List<Map<String, Object>> rows = sqlExecuteEngine.execute(conn, boundSql, timeout, 500);

                List<ApiResponseField> fields = apiResponseFieldMapper.selectList(
                        new LambdaQueryWrapper<ApiResponseField>().eq(ApiResponseField::getApiId, apiId));
                MaskResult maskResult = dataMasker.mask(rows, fields);

                resp.setData(maskResult.getRows());
                resp.setMasked(maskResult.isMasked());
                resp.setHttpStatus(200);
                resp.setBizCode(0);
                run.setStatus("SUCCESS");
                run.setOutputBody(objectMapper.writeValueAsString(maskResult.getRows()));
            }
        } catch (BizException e) {
            resp.setHttpStatus(e.getErrorCode().getHttpStatus());
            resp.setBizCode(e.getErrorCode().getCode());
            run.setStatus("FAIL");
            run.setErrorMsg(e.getMessage());
        } catch (Exception e) {
            resp.setHttpStatus(500);
            resp.setBizCode(ErrorCode.INTERNAL_ERROR.getCode());
            run.setStatus("FAIL");
            run.setErrorMsg(e.getMessage());
        }

        long costMs = System.currentTimeMillis() - start;
        resp.setCostMs((int) costMs);
        run.setCostMs((int) costMs);
        testRunMapper.insert(run);

        return resp;
    }

    @Override
    public PageResult<TestCaseVO> listCases(Long apiId, PageQuery query) {
        Page<TestCase> page = testCaseMapper.selectPage(query.toPage(),
                new LambdaQueryWrapper<TestCase>().eq(TestCase::getApiId, apiId).orderByAsc(TestCase::getId));
        List<TestCaseVO> voList = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(voList, page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    @Override
    public Map<String, Object> createCase(Long apiId, TestCaseReq req) {
        TestCase entity = new TestCase();
        entity.setApiId(apiId);
        entity.setName(req.getName());
        entity.setInputParams(toJsonString(req.getInputParams(), "{}"));
        entity.setAssertions(toJsonString(req.getAssertions(), "[]"));
        entity.setEnabled(req.getEnabled() != null && req.getEnabled() ? 1 : 1);
        entity.setCreatedBy(1L); // Phase 1: fixed admin user
        testCaseMapper.insert(entity);

        Map<String, Object> result = new HashMap<>();
        result.put("id", entity.getId());
        return result;
    }

    @Override
    public Map<String, Object> updateCase(Long caseId, TestCaseReq req) {
        TestCase entity = testCaseMapper.selectById(caseId);
        if (entity == null) throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "测试用例不存在");
        entity.setName(req.getName());
        if (req.getInputParams() != null) entity.setInputParams(toJsonString(req.getInputParams(), "{}"));
        if (req.getAssertions() != null) entity.setAssertions(toJsonString(req.getAssertions(), "[]"));
        if (req.getEnabled() != null) entity.setEnabled(req.getEnabled() ? 1 : 0);
        testCaseMapper.updateById(entity);

        Map<String, Object> result = new HashMap<>();
        result.put("id", caseId);
        return result;
    }

    @Override
    public void deleteCase(Long caseId) {
        testCaseMapper.deleteById(caseId);
    }

    @Override
    public Map<String, Object> runAll(Long apiId) {
        List<TestCase> cases = testCaseMapper.selectList(
                new LambdaQueryWrapper<TestCase>().eq(TestCase::getApiId, apiId));

        int total = cases.size();
        int passed = 0;
        int failed = 0;

        for (TestCase tc : cases) {
            try {
                Map<String, Object> params = new HashMap<>();
                if (tc.getInputParams() != null && !tc.getInputParams().isEmpty()) {
                    params = objectMapper.readValue(tc.getInputParams(), new TypeReference<Map<String, Object>>() {});
                }
                RunTestReq req = new RunTestReq();
                req.setParams(params);
                RunTestResp resp = doRunTest(apiId, req, tc.getId());

                if (resp.getBizCode() == 0) {
                    passed++;
                } else {
                    failed++;
                }
            } catch (Exception e) {
                failed++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("passed", passed);
        result.put("failed", failed);
        result.put("gatePass", failed == 0 && total > 0);
        return result;
    }

    private String toJsonString(Object value, String defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof String) {
            String s = ((String) value).trim();
            if (s.isEmpty()) return defaultValue;
            // Validate it's actually valid JSON
            try {
                objectMapper.readTree(s);
                return s;
            } catch (Exception e) {
                return defaultValue;
            }
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "参数序列化失败");
        }
    }

    private TestCaseVO toVO(TestCase entity) {
        TestCaseVO vo = new TestCaseVO();
        vo.setId(entity.getId());
        vo.setApiId(entity.getApiId());
        vo.setName(entity.getName());
        vo.setInputParams(entity.getInputParams());
        vo.setAssertions(entity.getAssertions());
        vo.setEnabled(entity.getEnabled() != null && entity.getEnabled() == 1);
        if (entity.getCreatedAt() != null) vo.setCreatedAt(entity.getCreatedAt().format(FMT));

        // Query latest test_run status for this case
        List<TestRun> runs = testRunMapper.selectList(
                new LambdaQueryWrapper<TestRun>()
                        .eq(TestRun::getCaseId, entity.getId())
                        .orderByDesc(TestRun::getId)
                        .last("LIMIT 1"));
        if (!runs.isEmpty()) {
            vo.setLastRunStatus(runs.get(0).getStatus());
        }
        return vo;
    }
}
