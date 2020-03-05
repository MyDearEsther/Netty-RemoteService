package com.weikun.client.client.service;
import com.weikun.client.client.http.ProgressCallback;
import com.weikun.client.client.http.upload.HttpClientUploadService;
import com.weikun.client.common.annotation.Service;
import com.weikun.client.client.http.download.HttpClientDownloadService;

/**
 * Apk签名服务
 * @author weikun
 * @date 2019/12/17
 */
@Service("/sign")
public class WorkService extends AbstractService {
    /**
     * 服务接口
     * */
    private static IWorkService service;

    /**
     * 设置代理对象
     * @param impl 代理对象
     * */
    @Override
    public void setService(Object impl){
        service = (IWorkService) impl;
    }

    public static String exeCmd(String cmd){
        return service.exeCmd(cmd);
    }

    public static void donwloadFile(String url, String dir, ProgressCallback<String> callback){
        HttpClientDownloadService.getInstance().startDownload(true,dir,url,callback);
    }
}
