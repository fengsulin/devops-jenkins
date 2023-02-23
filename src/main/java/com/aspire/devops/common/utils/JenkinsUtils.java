package com.aspire.devops.common.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aspire.devops.common.beans.*;
import com.aspire.devops.common.config.JenkinsProperty;
import com.aspire.devops.common.service.impl.BasicAuth;
import com.aspire.devops.common.vo.HttpResponseVo;
import com.aspire.devops.common.vo.ServiceException;
import com.aspire.devops.common.vo.enums.ParamTypeEnum;
import com.aspire.devops.common.vo.enums.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Component
/**
 * jenkins工具类，交由spring容器管理
 */
public class JenkinsUtils {
    private final JenkinsProperty jenkinsProperty;

    public JenkinsUtils(JenkinsProperty jenkinsProperty) {
        this.jenkinsProperty = jenkinsProperty;
    }

    /**
     * 创建job，需要传入config配置
     * @param dir 文件夹名称，optional
     * @param jobName job名称
     * @param xmlData config字符串
     * @return boolean 是否创建成功
     */
    public  void createJob(String dir,String jobName,String xmlData){
        if (StringUtils.isBlank(xmlData)){
            throw new ServiceException("["+jobName +"]"+"config的配置不能为空");
        }
        Map<String,String> headers = new HashMap<>(1);
        headers.put(HttpHeaders.AUTHORIZATION,SpringUtils.getBean(BasicAuth.class).getAuth());
        Map<String ,String> params = new HashMap<>(1);
        params.put("name",jobName);
        String pathJ = jenkinsProperty.getUrl();
        if (StringUtils.isNotBlank(dir)){
            if (dir.startsWith("/")){
                pathJ = pathJ + dir + "/createItem";
            }else {
                pathJ = pathJ + "/" + dir + "/createItem";
            }
        }else {
            pathJ = pathJ + "/createItem";
        }
        HttpResponseVo responseVo = null;
        try {
            responseVo = HttpUtils.postX(pathJ, params, headers,xmlData);
            if(responseVo.getCode() < 200 || responseVo.getCode() >= 400) throw new ServiceException("job创建失败");
            log.info("["+jobName+"]创建成功");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除job
     * @param dir 文件夹，optional
     * @param jobName job名称
     * @return
     */
    public boolean deleteJob(String dir,String jobName){
        String pathJ = jenkinsProperty.getUrl();
        Map<String,String> headers = new HashMap<>(2);
        headers.put(HttpHeaders.AUTHORIZATION,SpringUtils.getBean(BasicAuth.class).getAuth());

        if (StringUtils.isNotBlank(dir)){
            if (dir.startsWith("/")){
                pathJ = pathJ + dir + "/job/" + jobName + "/doDelete";
            }else {
                pathJ = pathJ + "/" + dir + "/job/" + jobName + "/doDelete";
            }
        }else {
            pathJ = pathJ + "/job/" + jobName +"/doDelete";
        }
        try {
            HttpResponseVo responseVo = HttpUtils.post(pathJ, null, headers);
            if(responseVo.getCode() < 200 || responseVo.getCode() >= 400) throw new ServiceException("job删除失败");
            log.info("["+jobName+"]删除成功");
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @description: Jenkins参数化构建
     * @author: FSL
     * @date: 2023/2/17
     * @param dir Jenkins中文件夹路径
     * @param params 参数
     * @param jobName job名称
     * @return: void
     */
    public void buildWithParameters(String dir,Map<String,String> params,String jobName) throws IOException {
        Map<String,String> headers = new HashMap<>(2);
        headers.put("Content-Type","application/x-www-form-urlencoded");
        headers.put(HttpHeaders.AUTHORIZATION,SpringUtils.getBean(BasicAuth.class).getAuth());

        String pathJ = jenkinsProperty.getUrl();
        if (StringUtils.isNotBlank(dir)){
            if (dir.startsWith("/")){
                pathJ = pathJ + dir + "/job/" + jobName + "/buildWithParameters";
            }else {
                pathJ = pathJ + "/" + dir + "/job/" + jobName + "/buildWithParameters";
            }
        }else {
            pathJ = pathJ + "/job/" + jobName +"/buildWithParameters";
        }

        HttpResponseVo responseVo = HttpUtils.post(pathJ, params, headers, "utf-8");
        if(responseVo.getCode() < 200 || responseVo.getCode() >= 400) throw new ServiceException("执行参数化构建失败");
        log.info("["+jobName+"]构建操作成功");
    }
    /**
     * 获取某一次构建信息
     * @param dir
     * @param jobName
     * @param buildId
     * @return
     */
    public BuildDetailDTO searchBuildInfo(String dir, String jobName, int buildId){
        BuildDetailDTO buildDetailDTO = new BuildDetailDTO();
        String pathJ = jenkinsProperty.getUrl();
        if (StringUtils.isNotBlank(dir)){
            if (dir.startsWith("/")){
                pathJ = pathJ + dir + "/job/" + jobName + "/" + buildId + "/api/json";
            }else {
                pathJ = pathJ + "/" + dir + "/job/" + jobName + "/" + buildId + "/api/json";
            }
        }else {
            pathJ = pathJ + "/job/" + jobName +"/" + buildId + "/api/json";
        }
        Map<String,String> reqHeader = new HashMap<>(1);
        reqHeader.put(HttpHeaders.AUTHORIZATION,SpringUtils.getBean(BasicAuth.class).getAuth());
        HttpResponseVo responseVo = HttpUtils.get(pathJ,reqHeader);
        if (responseVo.getCode() >= 400 || responseVo.getCode() < 200){
            log.info("["+jobName +"-"+ buildId+"]查询失败");
            return null;
        }
        JSONObject buildObject = JSONObject.parseObject(responseVo.getContent());
        String building = buildObject.getString("building");
        String duration = buildObject.getString("duration");
        String result = buildObject.getString("result");
        String id = buildObject.getString("id");
        String timestamp = buildObject.getString("timestamp");
        Instant instant = Instant.ofEpochMilli(Long.valueOf(timestamp));
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime buildTime = LocalDateTime.ofInstant(instant, zoneId);

        buildDetailDTO.setBuildId(Integer.valueOf(id))
                .setBuildTime(buildTime)
                .setDuration(Double.valueOf(duration)/1000)
                .setResult(result)
                .setStage(building.equals("false") ? "FINALIZED" : "STARTED")
                .setEndFlag("false".equals(building) ? Boolean.FALSE : Boolean.TRUE);
        log.info("[" + jobName + "-" +buildId + "]查询成功");
        return buildDetailDTO;
    }

    /**
     * 构建参数整合
     * @param paramJson
     * @param xmlData config配置字符串
     * @return
     */
    public String joinParamXml(String paramJson,String xmlData){
        Document document = null;
        try {
            document = DocumentHelper.parseText(xmlData);
        } catch (DocumentException e) {
            throw new ServiceException(e.getMessage());
        }
        // 每次构建自动生成一个构建唯一id，作为构建参数存储
        Element rootElement = document.getRootElement();
        Element properties = rootElement.element("properties");
        Element propertiesE = properties.element("hudson.model.ParametersDefinitionProperty");
        Element parameterDef = propertiesE.element("parameterDefinitions");
        Element stringPram = parameterDef.element("hudson.model.StringParameterDefinition");
        stringPram.element("defaultValue").setText(IdUtil.getSnowflakeNextIdStr());
        xmlData = document.asXML();
        if (StringUtils.isBlank(paramJson)) return xmlData;
        JSONArray parseArray = JSONObject.parseArray(paramJson);
        List<JenkinsParamDTO> params = parseArray.toJavaList(JenkinsParamDTO.class);
        // 参数校验
        checkParameters(params);
        for (int i = 0;i < params.size();i++){
            xmlData = handleParamXml(params.get(i),xmlData);
        }
        log.info("[构建参数]设置成功");
        return xmlData;
    }

    /**
     * @description: 构建参数校验（不允许同名参数）
     * @author: FSL
     * @date: 2023/2/20
     * @param params
     * @return: void
     */
    public void checkParameters(List<JenkinsParamDTO> params) {
        int originSize = params.size();
        Set<String> param = new HashSet<>();
        params.stream().forEach(jenkinsParamDTO -> param.add(jenkinsParamDTO.getName()));
        if (param.size() < originSize) throw new ServiceException(ResultEnum.VALID_ERROR);
    }

    /**
     * 设置pipeline脚本
     * @param script pipeline脚本
     * @param xmlData config字符串
     */
    public String joinScriptXml(String script,String xmlData){
        if (StringUtils.isBlank(script)) throw new ServiceException("pipeline脚本不能为空");

        try {
            Document document = DocumentHelper.parseText(xmlData);
            Element rootElement = document.getRootElement();
            Element definitionE = rootElement.element("definition");
            Element scriptE = definitionE.element("script");
            scriptE.setText(script);
            xmlData = document.asXML();
            log.info("[pipeline脚本]设置成功");
        } catch (DocumentException e) {
            throw new ServiceException(e.getMessage());
        }
        return xmlData;
    }

    /**
     * 将构建参数拼接到xml
     * @param jenkinsParamDTO
     * @param xmlData config字符串
     */
    private String handleParamXml(JenkinsParamDTO jenkinsParamDTO, String xmlData) {
        Document document = null;
        try {
            document = DocumentHelper.parseText(xmlData);
        } catch (DocumentException e) {
            throw new ServiceException(e.getMessage());
        }
        Element rootElement = document.getRootElement();

        if (ParamTypeEnum.STRING.getType().equals(jenkinsParamDTO.getType())){
            Element properties = rootElement.element("properties");
            Element propertiesE = properties.element("hudson.model.ParametersDefinitionProperty");
            if (propertiesE == null){
                propertiesE = properties.addElement("hudson.model.ParametersDefinitionProperty");
            }
            Element parameterDef = propertiesE.element("parameterDefinitions");
            if (parameterDef == null){
                parameterDef = propertiesE.addElement("parameterDefinitions");

            }

            Element stringPram = parameterDef.addElement("hudson.model.StringParameterDefinition");
            stringPram.addElement("name").setText(jenkinsParamDTO.getName());
            stringPram.addElement("description").setText(jenkinsParamDTO.getDesc());
            stringPram.addElement("defaultValue").setText(jenkinsParamDTO.getValue());
            stringPram.addElement("trim").setText("true");
            xmlData = document.asXML();
        }
        // TODO 其他类型参数
        return xmlData;
    }

    /**
     * @description: 获取日志
     * @author: FSL
     * @date: 2023/2/17
     * @param dir 文件夹
     * @param jobName job名称
     * @param buildId 构建number
     * @param start 日志读取开始位置
     * @return: java.lang.String
     */
    public HttpResponseVo receiveLogs(String dir,String jobName,String buildId,String start){
        String pathJ = jenkinsProperty.getUrl();
        if (StringUtils.isNotBlank(dir)){
            if (dir.startsWith("/")){
                pathJ = pathJ + dir + "/job/" + jobName + "/" + buildId + "/logText/progressiveText";
            }else {
                pathJ = pathJ + "/" + dir + "/job/" + jobName + "/" + buildId + "/logText/progressiveText";
            }
        }else {
            pathJ = pathJ + "/job/" + jobName +"/" + buildId + "/logText/progressiveText";
        }
        Map<String,String> reqMap = new HashMap<>(1);
        reqMap.put("start",start);
        Map<String,String> reqHeader = new HashMap<>(1);
        reqHeader.put(HttpHeaders.AUTHORIZATION,SpringUtils.getBean(BasicAuth.class).getAuth());
        HttpResponseVo httpResponseVo = HttpUtils.get(pathJ, reqMap,reqHeader);
        if (httpResponseVo.getCode() >= 400 || httpResponseVo.getCode() < 200){
            log.info("["+jobName +"]日志获取失败");
            return null;
        }
        return httpResponseVo;
    }

    /**
     * @description: 判断job在jenkins中是否存在
     * @author: FSL
     * @date: 2023/2/20
     * @param dir
     * @param jobName
     * @return: com.ap.devops.core.common.vo.HttpResponseVo
     */
    public boolean checkJob(String dir,String jobName){
        String pathJ = jenkinsProperty.getUrl();
        if (StringUtils.isNotBlank(dir)){
            if (dir.startsWith("/")){
                pathJ = pathJ + dir + "/job/" + jobName;
            }else {
                pathJ = pathJ + "/" + dir + "/job/" + jobName;
            }
        }else {
            pathJ = pathJ + "/job/" + jobName;
        }
        Map<String,String> reqHeader = new HashMap<>(1);
        reqHeader.put(HttpHeaders.AUTHORIZATION,SpringUtils.getBean(BasicAuth.class).getAuth());
        HttpResponseVo httpResponseVo = HttpUtils.get(pathJ, null, reqHeader);
        if (httpResponseVo.getCode() >= 400 || httpResponseVo.getCode() < 200){
            log.info("["+jobName +"]在Jenkins中不存在");
            return false;
        }
        return true;
    }

    /**
     * @description: 停止job构建
     * @author: FSL
     * @date: 2023/2/21
     * @param dir
     * @param jobName
     * @param buildId
     * @return: boolean
     */
    public boolean stopBuild(String dir,String jobName,String buildId){
        String pathJ = jenkinsProperty.getUrl();
        if (StringUtils.isNotBlank(dir)){
            if (dir.startsWith("/")){
                pathJ = pathJ + dir + "/job/" + jobName + "/" + buildId + "/stop";
            }else {
                pathJ = pathJ + "/" + dir + "/job/" + jobName + "/" + buildId + "/stop";
            }
        }else {
            pathJ = pathJ + "/job/" + jobName +"/" + buildId + "/stop";
        }
        Map<String,String> reqHeader = new HashMap<>(1);
        reqHeader.put(HttpHeaders.AUTHORIZATION,SpringUtils.getBean(BasicAuth.class).getAuth());
        HttpResponseVo httpResponseVo = null;
        try {
            httpResponseVo = HttpUtils.post(pathJ, null, reqHeader);
            if (httpResponseVo.getCode() >= 400 || httpResponseVo.getCode() < 200){
                throw new ServiceException("停止构建异常");
            }
            log.info("["+jobName+"]停止构建成功");
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @description: 获取job构建的stage信息
     * @author: FSL
     * @date: 2023/2/21
     * @param dir
     * @param jobName
     * @param buildId
     * @return: java.util.List<com.ap.devops.core.cicd.beans.JobStages>
     */
    public List<JobStagesDTO> queryBuildStage(String dir, String jobName, String buildId){
        String pathJ = jenkinsProperty.getUrl();
        if (StringUtils.isNotBlank(dir)){
            if (dir.startsWith("/")){
                pathJ = pathJ + dir + "/job/" + jobName + "/wfapi/runs";
            }else {
                pathJ = pathJ + "/" + dir + "/job/" + jobName + "/wfapi/runs";
            }
        }else {
            pathJ = pathJ + "/job/" + jobName +"/wfapi/runs";
        }
        Map<String,String> reqHeader = new HashMap<>(1);
        reqHeader.put(HttpHeaders.AUTHORIZATION,SpringUtils.getBean(BasicAuth.class).getAuth());
        Map<String,String> params = new HashMap<>(2);
        // %23表示=#
        params.put("since","%23"+buildId);
        // fullStages=true表示获取某一构建的全阶段信息
//        params.put("fullStages","true");
        HttpResponseVo httpResponseVo = HttpUtils.get(pathJ, params, reqHeader);
        if (httpResponseVo.getCode() >= 400 || httpResponseVo.getCode() < 200){
            return null;
        }
        String content = httpResponseVo.getContent();
        JSONArray parseArray = JSONObject.parseArray(content);
        JSONObject object = parseArray.getJSONObject(0);
        String id = object.getString("id");
        JSONArray stages = object.getJSONArray("stages");
        List<JobStagesDTO> stagesList = new ArrayList<>();

        int size = stages.size();
        for (int i = 0;i < size;i++){
            JobStagesDTO jobStagesDTO = new JobStagesDTO();
            JSONObject jsonObject = stages.getJSONObject(i);
            String name = jsonObject.getString("name");
            String status = jsonObject.getString("status");
            String startTimeMillis = jsonObject.getString("startTimeMillis");
            String durationMillis = jsonObject.getString("durationMillis");
            String duration = DateUtil.formatBetween(Long.valueOf(durationMillis));
            Instant instant = Instant.ofEpochMilli(Long.valueOf(startTimeMillis));
            ZoneId zoneId = ZoneId.systemDefault();
            LocalDateTime startTime = LocalDateTime.ofInstant(instant, zoneId);
            jobStagesDTO.setName(name)
                    .setBuildId(Long.valueOf(id)) // TODO id取值？
                    .setDuration(duration)
                    .setStatus(status)
                    .setStartTime(startTime);
            stagesList.add(jobStagesDTO);
        }
        return stagesList;
    }

    /**
     * @description: 调用Jenkins 等待队列接口
     * @author: FSL
     * @date: 2023/2/22
     * @return: java.util.List<com.ap.devops.core.cicd.beans.QueueItem>
     */
    public List<QueueItemDTO> queryQueue(){
        String pathJ = jenkinsProperty.getUrl() + "/queue/api/json";
        Map<String,String> reqHeader = new HashMap<>(1);
        reqHeader.put(HttpHeaders.AUTHORIZATION,SpringUtils.getBean(BasicAuth.class).getAuth());

        HttpResponseVo httpResponseVo = HttpUtils.get(pathJ, reqHeader);
        if (httpResponseVo.getCode() >= 400 || httpResponseVo.getCode() < 200){
            log.info("[queue]任务队列获取失败");
            return null;
        }
        String content = httpResponseVo.getContent();
        JSONObject queueJson = JSONObject.parseObject(content);
        JSONArray items = queueJson.getJSONArray("items");
        List<QueueItemDTO> queueItemDTOS = new ArrayList<>();
        int size = items.size();
        for (int i = 0;i < size;i++){
            QueueItemDTO queueItemDTO = new QueueItemDTO();
            JSONObject jsonObject = items.getJSONObject(i);
            boolean blocked = "false".equals(jsonObject.getString("blocked")) ? Boolean.FALSE : Boolean.TRUE;
            boolean buildable = "false".equals(jsonObject.getString("buildable")) ? Boolean.FALSE : Boolean.TRUE;
            String id = jsonObject.getString("id");
            String inQueueSince = jsonObject.getString("inQueueSince");
            boolean stuck = "false".equals(jsonObject.getString("stuck")) ? Boolean.FALSE : Boolean.TRUE;
            String startMilliseconds = jsonObject.getString("buildableStartMilliseconds");
            boolean pending = "false".equals(jsonObject.getString("pending")) ? Boolean.FALSE : Boolean.TRUE;
            Instant instant = Instant.ofEpochMilli(Long.valueOf(inQueueSince));
            ZoneId zoneId = ZoneId.systemDefault();
            LocalDateTime inTime = LocalDateTime.ofInstant(instant, zoneId);
            Instant instantS = Instant.ofEpochMilli(Long.valueOf(startMilliseconds));
            ZoneId zoneIdS = ZoneId.systemDefault();
            LocalDateTime startTime = LocalDateTime.ofInstant(instantS, zoneIdS);
            queueItemDTO.setInQueueTime(inTime)
                    .setId(id)
                    .setBuildable(buildable)
                    .setPending(pending)
                    .setStuck(stuck)
                    .setBlocked(blocked)
                    .setStartBuildTime(startTime);
            queueItemDTOS.add(queueItemDTO);

        }
        return queueItemDTOS;
    }

    /**
     * @description: 获取凭证,这里system是可以换成其他域，depth表示查询深度
     * @author: FSL
     * @date: 2023/2/22
     * @return: com.ap.devops.core.cicd.beans.CredentialStoreDTO
     */
    public CredentialStoreDTO queryCredentials(){
        String pathJ = jenkinsProperty.getUrl() + "/manage/credentials/store/system/domain/_/api/json?depth=1";
        Map<String,String> reqHeader = new HashMap<>(1);
        reqHeader.put(HttpHeaders.AUTHORIZATION,SpringUtils.getBean(BasicAuth.class).getAuth());

        HttpResponseVo httpResponseVo = HttpUtils.get(pathJ, reqHeader);
        if (httpResponseVo.getCode() >= 400 || httpResponseVo.getCode() < 200){
            log.info("[queue]任务队列获取失败");
            return null;
        }
        String content = httpResponseVo.getContent();
        JSONObject contentJson = JSONObject.parseObject(content);
        String description = contentJson.getString("description");
        String displayName = contentJson.getString("displayName");
        String fullName = contentJson.getString("fullName");
        boolean global = "false".equals(contentJson.getString("global")) ? Boolean.FALSE : Boolean.TRUE;
        JSONArray credentials = contentJson.getJSONArray("credentials");
        int size = credentials.size();
        List<CredentialDTO> credentialDTOS = new ArrayList<>();
        CredentialStoreDTO credentialStoreDTO = new CredentialStoreDTO();
        for (int i = 0;i < size;i++){
            CredentialDTO credentialDTO = new CredentialDTO();
            JSONObject jsonObject = credentials.getJSONObject(i);
            String description1 = jsonObject.getString("description");
            String displayName1 = jsonObject.getString("displayName");
            String fingerprint = jsonObject.getString("fingerprint");
            String id = jsonObject.getString("id");
            String typeName = jsonObject.getString("typeName");
            credentialDTO.setId(id)
                    .setDescription(description1)
                    .setFingerprint(fingerprint)
                    .setTypeName(typeName)
                    .setDisplayName(displayName1);
            credentialDTOS.add(credentialDTO);
        }
        credentialStoreDTO.setCredentialDTOS(credentialDTOS)
                .setDescription(description)
                .setDisplayName(displayName)
                .setGlobal(global)
                .setFullName(fullName);
        return credentialStoreDTO;
    }

    /**
     * @description: 创建账号密码凭证
     * @author: FSL
     * @date: 2023/2/22
     * @param dto
     * @return: void
     */
    public void createCredential(UsernamePasswordDTO dto) throws IOException {
        if (StringUtils.isBlank(dto.getUsername()) || StringUtils.isBlank(dto.getPassword())) throw new ServiceException("账号或密码不能为空");
        String pathJ = jenkinsProperty.getUrl() + "/manage/credentials/store/system/domain/_/createCredentials";
        Map<String,String> reqHeader = new HashMap<>(1);
        reqHeader.put(HttpHeaders.AUTHORIZATION,SpringUtils.getBean(BasicAuth.class).getAuth());
        Map<String,String> params = new HashMap<>(1);
        String id = StringUtils.isBlank(dto.getId()) ? IdUtil.randomUUID() : dto.getId();
        String description = StringUtils.isBlank(dto.getDescription()) ? dto.getUsername() + "/账号密码凭证" : dto.getDescription();
        String jsonOb = "{\"\":\"0\",\"credentials\":{\"scope\":\"GLOBAL\",\"username\":\""+dto.getUsername()+"\",\"usernameSecret\":false,\"password\":\""+dto.getPassword()+"\",\"$redact\":\"password\",\"id\":\""+dto.getId()+"\",\"description\":\""+description+"\",\"stapler-class\":\"com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl\",\"$class\":\"com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl\"}}";
        params.put("json",jsonOb);
        HttpResponseVo httpResponseVo = HttpUtils.post(pathJ, params,reqHeader);
        if (httpResponseVo.getCode() >= 400 || httpResponseVo.getCode() < 200){
            throw new ServiceException("凭证创建异常");
        }
        log.info("["+id+"]凭证创建成功");
    }

    /**
     * @description: 凭证更新
     * @author: FSL
     * @date: 2023/2/22
     * @param dto，id不能修改，保持原先的值(接口可以修改id值，但从业务角度来说不推荐)
     * @return: void
     */
    public void updateCredential(UsernamePasswordDTO dto) throws IOException {
        if (StringUtils.isBlank(dto.getUsername()) || StringUtils.isBlank(dto.getPassword()) || StringUtils.isBlank(dto.getId())) throw new ServiceException(ResultEnum.VALID_ERROR);
        String pathJ = jenkinsProperty.getUrl() + "/manage/credentials/store/system/domain/_/credential/"+dto.getId()+"/updateSubmit";
        Map<String,String> reqHeader = new HashMap<>(1);
        reqHeader.put(HttpHeaders.AUTHORIZATION,SpringUtils.getBean(BasicAuth.class).getAuth());
        Map<String,String> params = new HashMap<>(1);
        String jsonObject = "{\"stapler-class\":\"com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl\",\"scope\":\"GLOBAL\",\"username\":\""+dto.getUsername()+"\",\"usernameSecret\":false,\"password\":\""+dto.getPassword()+"\",\"$redact\":\"password\",\"id\":\""+dto.getId()+"\",\"description\":\""+dto.getDescription()+"\"}";
        params.put("json",jsonObject);
        HttpResponseVo httpResponseVo = HttpUtils.post(pathJ, params,reqHeader);
        if (httpResponseVo.getCode() >= 400 || httpResponseVo.getCode() < 200){
            throw new ServiceException("凭证更新异常");
        }
        log.info("["+dto.getId()+"]凭证更新成功");
    }

    /**
     * @description: 删除凭证
     * @author: FSL
     * @date: 2023/2/22
     * @param id
     * @return: void
     */
    public void deleteCredential(String id){
        String pathJ = jenkinsProperty.getUrl() + "/manage/credentials/store/system/domain/_/credential/"+id+"/doDelete";
        Map<String,String> reqHeader = new HashMap<>(1);
        reqHeader.put(HttpHeaders.AUTHORIZATION,SpringUtils.getBean(BasicAuth.class).getAuth());
        try {
            HttpResponseVo httpResponseVo = HttpUtils.post(pathJ, null, reqHeader);
            if (httpResponseVo.getCode() >= 400 || httpResponseVo.getCode() < 200){
                throw new ServiceException("凭证删除异常");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("["+id+"]凭证删除成功");
    }
}
