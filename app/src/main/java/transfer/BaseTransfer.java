package transfer;

/**
 * Created by Administrator on 2017/4/26.
 */

public abstract class BaseTransfer implements  Transferable{
    /**
     * 头部分割字符
     */
    public static final String SPERATOR = "::";

    /**
     * 字节数组长度
     */
    public static final int BYTE_SIZE_HEADER    = 1024 * 10;
    public static final int BYTE_SIZE_SCREENSHOT    = 1024 * 40;
    public static final int BYTE_SIZE_DATA      = 1024 * 4;

    /**
     * 传输文件类型
     */
    public static final int TYPE_FILE = 1; //文件类型
    public static final int TYPE_MSG = 2;  //消息类型FileSender

    /**
     * 传输字节类型
     */
    public static final String UTF_8 = "UTF-8";
}
