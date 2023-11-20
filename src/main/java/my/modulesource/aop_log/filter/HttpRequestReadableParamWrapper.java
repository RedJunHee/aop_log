package my.modulesource.aop_log.filter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class HttpRequestReadableParamWrapper extends HttpServletRequestWrapper {
    private final Logger logger = LogManager.getLogger(HttpRequestReadableParamWrapper.class);
    private final Charset encoding;
    private byte[] rawData; // body
    private Map<String, String[]> params = new HashMap<>();


    public HttpRequestReadableParamWrapper(HttpServletRequest request) {

        super(request);
        this.params.putAll(request.getParameterMap()); // 원래의 파라미터 저장

        String charEncoding = request.getCharacterEncoding();
        this.encoding = StringUtils.isBlank(charEncoding) ? StandardCharsets.UTF_8 : Charset.forName(charEncoding);

        String queryParams = "?";
        for(String key : params.keySet()) {
            String value = Arrays.toString( params.get(key));
            queryParams = (queryParams + key+ "=" + value + "&" );
        }
        queryParams = queryParams.substring(0,queryParams.length()-1);

        logger.info("[{}]Call : {}", ThreadContext.get("logId").toString(),request.getRequestURI() + queryParams);

        try {
            InputStream is = request.getInputStream();
            this.rawData = IOUtils.toByteArray(is); //

            String collect = this.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

            //body가 비었을 경우.
            if (StringUtils.isEmpty(collect)) {
                return;
            }
            logger.info("Body : {}",collect);
            //파일 업로드인 ContentType이 MULTIPART FORM일 경우 로깅 제외.
            if (request.getContentType() != null && request.getContentType()
                    .contains(ContentType.MULTIPART_FORM_DATA.getMimeType())) {
                return;
            }

            JSONParser jsonParser = new JSONParser();
            Object parser = jsonParser.parse(collect);

            if (parser instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) jsonParser.parse(collect);
                setParameter("requestArrayParam", jsonArray.toJSONString());
            } else {
                JSONObject jsonObject = (JSONObject) jsonParser.parse(collect);
                Iterator iterator = jsonObject.keySet().iterator();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    String value = jsonObject.get(key).toString().replace("\"", "\\\"");
                    setParameter(key, value);
                }
            }


        } catch (Exception e) {
            logger.error("ReadableParamWrapper init error");
        }


    }


    /**
     * request 에 담긴 정보를 JSONObject 형태로 반환한다.
     *
     * @param request
     * @return
     */
    private Map<String, String> getParams(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String param = params.nextElement();
            String replaceParam = param.replaceAll("\\.", "-");
            map.put(replaceParam, request.getParameter(param));
        }
        return map;
    }

    @Override
    public String getParameter(String name) {
        String[] paramArray = getParameterValues(name);
        if (paramArray != null && paramArray.length > 0) {
            return paramArray[0];
        } else {
            return null;
        }
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        if (value == null)
            return null;
        return value;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(params);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(params.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] result = null;
        String[] dummyParamValue = params.get(name);

        if (dummyParamValue != null) {
            result = new String[dummyParamValue.length];
            System.arraycopy(dummyParamValue, 0, result, 0, dummyParamValue.length);
        }
        if (result != null) {
            int index = 0;
            for (String value : result) {
                result[index] = value;
                index++;
            }
        }
        return result;
    }

    public void setParameter(String name, String value) {
        String[] param = {value};
        setParameter(name, param);
    }

    public void setParameter(String name, String[] values) {
        params.put(name, values);
    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.rawData);

        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // Do nothing
            }

            public int read() {
                return byteArrayInputStream.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.getInputStream(), this.encoding));
    }

}

