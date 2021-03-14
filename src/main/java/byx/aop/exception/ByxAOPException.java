package byx.aop.exception;

public class ByxAOPException extends RuntimeException {
    public ByxAOPException(String msg) {
        super(msg);
    }

    public ByxAOPException(String msg, Exception e) {
        super(msg, e);
    }
}
