package byx.aop.exception;

/**
 * ByxAOP异常基类
 *
 * @author byx
 */
public class ByxAOPException extends RuntimeException {
    public ByxAOPException(String msg) {
        super(msg);
    }

    public ByxAOPException(String msg, Exception e) {
        super(msg, e);
    }
}
