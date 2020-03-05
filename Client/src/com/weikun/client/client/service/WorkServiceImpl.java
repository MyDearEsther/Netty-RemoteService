package com.weikun.client.client.service;

import com.weikun.client.client.http.ProgressCallback;
import com.weikun.client.common.annotation.ServiceImpl;

/**
 * @author weikun
 * @date 2020/2/19
 */
@ServiceImpl("/sign")
public class WorkServiceImpl implements IWorkService{
    private static IWorkService service;

    @Override
    public String exeCmd(String cmd) {
        return service.exeCmd(cmd);
    }
}
