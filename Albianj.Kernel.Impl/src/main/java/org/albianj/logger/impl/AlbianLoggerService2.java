package org.albianj.logger.impl;

import org.albianj.io.Path;
import org.albianj.kernel.KernelSetting;
import org.albianj.loader.AlbianClassLoader;
import org.albianj.logger.AlbianLoggerLevel;
import org.albianj.logger.IAlbianLoggerService2;
import org.albianj.runtime.AlbianModuleType;
import org.albianj.runtime.AlbianRuntimeException;
import org.albianj.service.AlbianServiceException;
import org.albianj.service.AlbianServiceRouter;
import org.albianj.service.FreeAlbianService;
import org.albianj.verify.Validate;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Formatter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xuhaifeng on 17/2/9.
 */
@org.albianj.comment.Comments("新的logger日志类，解决log-v1中无法正确标识文件位置问题")
public class AlbianLoggerService2 extends FreeAlbianService implements
        IAlbianLoggerService2 {

    public String getServiceName(){
        return Name;
    }


    private static ConcurrentHashMap<String, Logger> loggers = null;

    public Logger getLogger(String name) {
        return AlbianServiceRouter.getLogger().getLogger(name);
    }

    @Override
    public void log(String filename, String methodName, int lineNumber,
                    String loggerName,Object sessionId, AlbianLoggerLevel level, String format, Object... values) {
        Logger logger = getLogger(loggerName);
        if(null != logger){
            flush(filename,methodName,lineNumber,logger,sessionId,level,null,format,values);
        }
    }

    @Override
    public void log(String filename, String methodName, int lineNumber,
                    String loggerName,Object sessionId,
                    AlbianLoggerLevel level, Throwable e, String format, Object... values) {
        Logger logger = getLogger(loggerName);
        if(null != logger){
            flush(filename,methodName,lineNumber,logger,sessionId,level,e,format,values);
        }
    }

    @Override
    public void logAndThrow(String filename, String methodName, int lineNumber,
                            String loggerName,Object sessionId,
                            AlbianLoggerLevel level, Throwable e,
                            AlbianModuleType module, String throwInfo, String format, Object... values) throws AlbianRuntimeException {
        Logger logger = getLogger(loggerName);
        String msg = null;
        if(null != logger){
            msg = flush(filename,methodName,lineNumber,logger,sessionId,level,e,format,values);
        }
        if(e instanceof AlbianRuntimeException){
            throw (AlbianRuntimeException) e;
        }
        if(Validate.isNullOrEmptyOrAllSpace(throwInfo)) {
            throw new AlbianRuntimeException(module,filename,lineNumber,methodName,msg,e);
        } else {
            throw new AlbianRuntimeException(module,filename,lineNumber,methodName,msg,throwInfo);
        }
    }

    @Override
    public void log(String loggerName, Object sessionId, AlbianLoggerLevel level, String format, Object... values) {
        StackTraceElement stack = Thread.currentThread().getStackTrace()[2];
        String filename = stack.getFileName();
        String method = stack.getMethodName();
        int line = stack.getLineNumber();
        log(filename,method,line,loggerName,sessionId,level,format,values);
    }

    @Override
    public void log(String loggerName, Object sessionId, AlbianLoggerLevel level, Throwable e, String format, Object... values) {
//        StackTraceElement stack = Thread.currentThread().getStackTrace()[2];
        StackTraceElement stack = e.getStackTrace()[1];
        String filename = stack.getFileName();
        String method = stack.getMethodName();
        int line = stack.getLineNumber();
        log(filename,method,line,loggerName,sessionId,level,e,format,values);
    }

    @Override
    public void logAndThrow(String loggerName,Object sessionId, AlbianLoggerLevel level,
                            Throwable e, AlbianModuleType module,String throwInfo,
                            String format, Object... values) throws AlbianRuntimeException {
//        StackTraceElement stack = Thread.currentThread().getStackTrace()[2];
        StackTraceElement stack = e.getStackTrace()[1];
        String filename = stack.getFileName();
        String method = stack.getMethodName();
        int line = stack.getLineNumber();
        logAndThrow(filename,method,line,loggerName,sessionId,level,e,module,throwInfo,format,values);
    }

    private String makeLogInfo( String filename, String methodName, int lineNumber,
                                Object sessionId, AlbianLoggerLevel level,
                                Throwable e, String format, Object... values
                               ){
        StringBuilder sb = new StringBuilder();

        if(null != sessionId) {
            sb.append("SessionId:").append(sessionId).append(",");
        }
        sb.append("File:").append(filename).append(",")
        .append("Line:").append(lineNumber).append(",")
        .append("Method:").append(methodName).append(",");
        if(null != e){
            if(e instanceof AlbianRuntimeException) {
                sb.append("Exception:").append(((AlbianRuntimeException) e).toInnerString()).append(",");
            } else {
                sb.append("Exception:").append(e.getMessage()).append(",");
            }
        }
        sb.append("Message:");
        Formatter f = new Formatter(sb);
        f.format(format,values);
        return f.toString();

    }

    private String  flush(String filename, String methodName, int lineNumber,
                                Logger logger,Object sessionId, AlbianLoggerLevel level,
                                Throwable e, String format, Object... values){
        switch (level){
            case Debug:
                 if(logger.isDebugEnabled()){
                     String info = makeLogInfo(filename,methodName,lineNumber,sessionId,level,e,format,values);
                     logger.debug(info);
                     return info;
                 }
            case Info:
                if(logger.isInfoEnabled()){
                    String info = makeLogInfo(filename,methodName,lineNumber,sessionId,level,e,format,values);
                    logger.info(info);
                    return info;
                }
            case Warn:
                if(logger.isWarnEnabled()){
                    String info = makeLogInfo(filename,methodName,lineNumber,sessionId,level,e,format,values);
                    logger.warn(info);
                    return info;
                }
            case Error:
                if(logger.isErrorEnabled()){
                    String info = makeLogInfo(filename,methodName,lineNumber,sessionId,level,e,format,values);
                    logger.error(info);
                    return info;
                }
            case Mark:
                if(logger.isTraceEnabled()){
                    String info = makeLogInfo(filename,methodName,lineNumber,sessionId,level,e,format,values);
                    logger.trace(info);
                    return info;
                }
            default:
                if(logger.isInfoEnabled()){
                    String info = makeLogInfo(filename,methodName,lineNumber,sessionId,level,e,format,values);
                    logger.info(info);
                    return info;
                }
        }
        return null;
    }

}
