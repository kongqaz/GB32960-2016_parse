package com.example.gb32960;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * GB32960协议解码器，用于处理TCP粘包和半包问题
 */
public class Gb32960Decoder extends ByteToMessageDecoder {
    private static final Logger logger = LoggerFactory.getLogger(Gb32960Decoder.class);

    private static final byte START_DELIMITER_1 = 0x23; // 起始符 #
    private static final byte START_DELIMITER_2 = 0x23; // 起始符 #

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        while (in.readableBytes() > 0) {
            // 记录当前readerIndex位置，用于回退
            in.markReaderIndex();
            // 查找起始符
            if (in.readByte() != START_DELIMITER_1) {
                return;
            }

            // 检查是否还有数据可读
            if (in.readableBytes() < 1) {
                in.resetReaderIndex();
                return;
            }

            // 检查第二个起始符
            if (in.readByte() != START_DELIMITER_2) {
                // 回退一个字节，继续查找
                in.readerIndex(in.readerIndex() - 1);
                return;
            }

            // 找到起始符后，检查是否有足够的字节
            if (in.readableBytes() < 23) {
                // 数据不够，回退并等待更多数据
                in.resetReaderIndex();
                return;
            }

            // 跳过命令标识、应答标识、VIN码和加密方式字段，到达长度字段
            in.skipBytes(20); // 1+1+17+1

            // 读取数据单元长度
            int dataLength = in.readUnsignedShort();

            // 计算完整数据包长度（包括两个起始符、头部、数据单元和校验码）
            int totalLength = 25 + dataLength; // 起始符(2) + 头部(22) + 数据单元长度 + 校验码(1)

            // 检查是否有足够的数据构成完整包
            if (in.readableBytes() < dataLength + 1) {
                // 数据不够，回退并等待更多数据
                in.resetReaderIndex();
                return;
            }

            // 重置readerIndex到起始符位置
            in.resetReaderIndex();

            // 读取完整数据包
            byte[] packet = new byte[totalLength];
            in.readBytes(packet);

            // 输出数据包
            out.add(packet);
        }
    }
}
