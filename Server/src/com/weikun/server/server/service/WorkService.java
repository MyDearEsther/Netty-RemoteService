package com.weikun.server.server.service;

import com.weikun.server.common.Cmd;
import com.weikun.server.common.MsgType;
import com.weikun.server.common.annotation.Service;

@Service(MsgType.SERVICE_WORK)
public class WorkService {

    public static void exeCmd(String cmd) throws Throwable{
        Cmd.execCmd(cmd,true);
    }
}
