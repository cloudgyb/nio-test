package com.github.cloudgyb.http.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.ByteToMessageDecoder.MERGE_CUMULATOR;

/**
 * @author geng
 * @since 2023/03/29 16:24:05
 */
public class HttpReq implements HttpRequest {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final HttpRequest rawRequest;
    private final Map<String, Object> params;
    private final Map<String, List<String>> queryParams;
    private Map<String, UploadFile> uploadFiles;
    private ByteBuf body = Unpooled.EMPTY_BUFFER;

    public HttpReq(HttpRequest rawRequest) {
        this.rawRequest = rawRequest;
        this.params = new HashMap<>();
        this.queryParams = new HashMap<>();
        if (HttpPostRequestDecoder.isMultipart(rawRequest)) {
            uploadFiles = new HashMap<>();
        }
        parseQueryParams();
    }

    private void parseQueryParams() {
        String uri = rawRequest.uri();
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
        Map<String, List<String>> parameters = queryStringDecoder.parameters();
        addQueryParams(parameters);
    }

    public void addQueryParams(Map<String, List<String>> queryParams) {
        this.queryParams.putAll(queryParams);
    }

    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }

    public Map<String, UploadFile> getUploadFiles() {
        return uploadFiles;
    }

    @SuppressWarnings("all")
    public void addUploadFile(FileUpload upload) {
        try {
            File file = upload.getFile();
            File tempFile = new File(file.getParentFile(), file.getName() + ".tmp");
            file.renameTo(tempFile);
            uploadFiles.put(upload.getName(),
                    new UploadFile(tempFile, upload.getContentType()));
        } catch (IOException ignore) {
        }
    }

    public void addParameter(Attribute attribute) {
        try {
            addParameter(attribute.getName(), attribute.getValue());
        } catch (IOException ignore) {
        }
    }

    public void addParameter(String key, Object value) {
        params.put(key, value);
    }

    public Object getParameter(String name) {
        return params.get(name);
    }

    public void addBodyContent(HttpContent httpContent) {
        this.body = MERGE_CUMULATOR.cumulate(ByteBufAllocator.DEFAULT, this.body, httpContent.content());
    }

    public String bodyAsString() {
        return this.body.toString(StandardCharsets.UTF_8);
    }

    public byte[] body() {
        return this.body.array();
    }

    @Override
    public HttpMethod getMethod() {
        return rawRequest.method();
    }

    @Override
    public HttpMethod method() {
        return getMethod();
    }

    @Override
    public HttpRequest setMethod(HttpMethod method) {
        return rawRequest.setMethod(method);
    }

    @Override
    public String getUri() {
        return rawRequest.uri();
    }

    @Override
    public String uri() {
        return getUri();
    }

    @Override
    public HttpRequest setUri(String uri) {
        return rawRequest.setUri(uri);
    }

    @Override
    public HttpVersion getProtocolVersion() {
        return rawRequest.protocolVersion();
    }

    @Override
    public HttpVersion protocolVersion() {
        return rawRequest.protocolVersion();
    }

    @Override
    public HttpRequest setProtocolVersion(HttpVersion version) {
        return rawRequest.setProtocolVersion(version);
    }

    @Override
    public HttpHeaders headers() {
        return rawRequest.headers();
    }

    @Override
    public DecoderResult getDecoderResult() {
        return rawRequest.decoderResult();
    }

    @Override
    public DecoderResult decoderResult() {
        return rawRequest.decoderResult();
    }

    @Override
    public void setDecoderResult(DecoderResult result) {
        rawRequest.setDecoderResult(result);
    }

    public void release() {
        // 清理文件如果有
        if (getUploadFiles() != null) {
            uploadFiles.values().forEach(f -> {
                boolean delete = f.getFile().delete();
                if (delete) {
                    logger.debug("上传临时文件（{}）以删除", f.getFile().getAbsolutePath());
                } else {
                    logger.error("上传临时文件（{}）未成功删除", f.getFile().getAbsolutePath());
                }
            });
        }
        if (body != null) {
            ReferenceCountUtil.release(body, body.refCnt());
        }
    }
}
