package transfer;

/**
 * Created by Administrator on 2017/4/26.
 */

public interface Transferable {



        /**
         *
         * @throws Exception
         */
        void init() throws Exception;


        /**
         *
         * @throws Exception
         */
        void parseHeader() throws Exception;


        /**
         *
         * @throws Exception
         */
        void parseBody() throws Exception;


        /**
         *
         * @throws Exception
         */
        void finish() throws Exception;
    }

