package com.oaktree.core.data.sequence;

import com.oaktree.core.data.IDataReceiver;
import com.oaktree.core.utils.Text;

public class SequenceUtils {

    public static String print(IDataSequence source) {
        StringBuilder b = new StringBuilder("################################################################");
        b.append(print(source,0));
        b.append("\n################################################################");
        return b.toString();
    }
    public static String print(IDataSequence source, int pos) {
        StringBuilder b = new StringBuilder();

        b.append(Text.NEW_LINE);
        for (int i =0; i < pos;i++) {
            b.append(Text.TAB);
        }
        b.append(Text.LEFT_BRACKET);
        b.append(pos);
        b.append(Text.RIGHT_BRACKET);
        b.append(source.getName());
        b.append(Text.LEFT_SQUARE_BRACKET);
        b.append(source.getClass().getSimpleName());
        b.append(Text.RIGHT_SQUARE_BRACKET);
        for (Object rcv:source.getReceivers()) {
            IDataSequence seq = (IDataSequence)(rcv);
            b.append(print(seq,++pos));
        }
        return b.toString();
    }
}
