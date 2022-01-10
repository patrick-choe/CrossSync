package io.github.patrick.crosssync;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ByteProcessor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

// Originally written by Mojang (net.minecraft.network.FriendlyByteBuf).
public class FriendlyByteBuf extends ByteBuf {
    private final ByteBuf source;
    private static final int MAX_VARINT_SIZE = 5;
    public static final short MAX_STRING_LENGTH = 32767;

    public FriendlyByteBuf(ByteBuf parent) {
        this.source = parent;
    }

    public byte[] readByteArray() {
        int i = this.readableBytes();
        int j = this.readVarInt();

        if (j > i) {
            throw new DecoderException("ByteArray with size " + j + " is bigger than allowed " + i);
        } else {
            byte[] dst = new byte[j];

            this.readBytes(dst);
            return dst;
        }
    }

    public FriendlyByteBuf writeByteArray(byte[] value) {
        this.writeVarInt(value.length);
        this.writeBytes(value);
        return this;
    }

    public int readVarInt() {
        int i = 0;
        int j = 0;

        byte b0;

        do {
            b0 = this.readByte();
            i |= (b0 & 127) << j++ * 7;
            if (j > MAX_VARINT_SIZE) {
                throw new RuntimeException("VarInt too big");
            }
        } while ((b0 & 128) == 128);

        return i;
    }

    @SuppressWarnings("UnusedReturnValue")
    public FriendlyByteBuf writeVarInt(int value) {
        while ((value & -128) != 0) {
            this.writeByte(value & 127 | 128);
            value >>>= 7;
        }

        this.writeByte(value);
        return this;
    }

    public String readUtf() {
        int j = this.readVarInt();

        if (j > MAX_STRING_LENGTH * 4) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + j + " > " + MAX_STRING_LENGTH * 4 + ")");
        } else if (j < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        } else {
            String s = this.toString(this.readerIndex(), j, StandardCharsets.UTF_8);

            this.readerIndex(this.readerIndex() + j);
            if (s.length() > MAX_STRING_LENGTH) {
                throw new DecoderException("The received string length is longer than maximum allowed (" + j + " > " + MAX_STRING_LENGTH + ")");
            } else {
                return s;
            }
        }
    }

    public FriendlyByteBuf writeUtf(String value) {
        byte[] src = value.getBytes(StandardCharsets.UTF_8);

        if (src.length > MAX_STRING_LENGTH) {
            throw new EncoderException("String too big (was " + src.length + " bytes encoded, max " + MAX_STRING_LENGTH + ")");
        } else {
            this.writeVarInt(src.length);
            this.writeBytes(src);
            return this;
        }
    }

    public int capacity() {
        return this.source.capacity();
    }

    public ByteBuf capacity(int newCapacity) {
        return this.source.capacity(newCapacity);
    }

    public int maxCapacity() {
        return this.source.maxCapacity();
    }

    public ByteBufAllocator alloc() {
        return this.source.alloc();
    }

    @SuppressWarnings("deprecation")
    public ByteOrder order() {
        return this.source.order();
    }

    @SuppressWarnings("deprecation")
    public ByteBuf order(ByteOrder endianness) {
        return this.source.order(endianness);
    }

    public ByteBuf unwrap() {
        return this.source.unwrap();
    }

    public boolean isDirect() {
        return this.source.isDirect();
    }

    public boolean isReadOnly() {
        return this.source.isReadOnly();
    }

    public ByteBuf asReadOnly() {
        return this.source.asReadOnly();
    }

    public int readerIndex() {
        return this.source.readerIndex();
    }

    public ByteBuf readerIndex(int readerIndex) {
        return this.source.readerIndex(readerIndex);
    }

    public int writerIndex() {
        return this.source.writerIndex();
    }

    public ByteBuf writerIndex(int writerIndex) {
        return this.source.writerIndex(writerIndex);
    }

    public ByteBuf setIndex(int readerIndex, int writerIndex) {
        return this.source.setIndex(readerIndex, writerIndex);
    }

    public int readableBytes() {
        return this.source.readableBytes();
    }

    public int writableBytes() {
        return this.source.writableBytes();
    }

    public int maxWritableBytes() {
        return this.source.maxWritableBytes();
    }

    public boolean isReadable() {
        return this.source.isReadable();
    }

    public boolean isReadable(int size) {
        return this.source.isReadable(size);
    }

    public boolean isWritable() {
        return this.source.isWritable();
    }

    public boolean isWritable(int size) {
        return this.source.isWritable(size);
    }

    public ByteBuf clear() {
        return this.source.clear();
    }

    public ByteBuf markReaderIndex() {
        return this.source.markReaderIndex();
    }

    public ByteBuf resetReaderIndex() {
        return this.source.resetReaderIndex();
    }

    public ByteBuf markWriterIndex() {
        return this.source.markWriterIndex();
    }

    public ByteBuf resetWriterIndex() {
        return this.source.resetWriterIndex();
    }

    public ByteBuf discardReadBytes() {
        return this.source.discardReadBytes();
    }

    public ByteBuf discardSomeReadBytes() {
        return this.source.discardSomeReadBytes();
    }

    public ByteBuf ensureWritable(int minWritableBytes) {
        return this.source.ensureWritable(minWritableBytes);
    }

    public int ensureWritable(int minWritableBytes, boolean force) {
        return this.source.ensureWritable(minWritableBytes, force);
    }

    public boolean getBoolean(int index) {
        return this.source.getBoolean(index);
    }

    public byte getByte(int index) {
        return this.source.getByte(index);
    }

    public short getUnsignedByte(int index) {
        return this.source.getUnsignedByte(index);
    }

    public short getShort(int index) {
        return this.source.getShort(index);
    }

    public short getShortLE(int index) {
        return this.source.getShortLE(index);
    }

    public int getUnsignedShort(int index) {
        return this.source.getUnsignedShort(index);
    }

    public int getUnsignedShortLE(int index) {
        return this.source.getUnsignedShortLE(index);
    }

    public int getMedium(int index) {
        return this.source.getMedium(index);
    }

    public int getMediumLE(int index) {
        return this.source.getMediumLE(index);
    }

    public int getUnsignedMedium(int index) {
        return this.source.getUnsignedMedium(index);
    }

    public int getUnsignedMediumLE(int index) {
        return this.source.getUnsignedMediumLE(index);
    }

    public int getInt(int index) {
        return this.source.getInt(index);
    }

    public int getIntLE(int index) {
        return this.source.getIntLE(index);
    }

    public long getUnsignedInt(int index) {
        return this.source.getUnsignedInt(index);
    }

    public long getUnsignedIntLE(int index) {
        return this.source.getUnsignedIntLE(index);
    }

    public long getLong(int index) {
        return this.source.getLong(index);
    }

    public long getLongLE(int index) {
        return this.source.getLongLE(index);
    }

    public char getChar(int index) {
        return this.source.getChar(index);
    }

    public float getFloat(int index) {
        return this.source.getFloat(index);
    }

    public double getDouble(int index) {
        return this.source.getDouble(index);
    }

    public ByteBuf getBytes(int index, ByteBuf dst) {
        return this.source.getBytes(index, dst);
    }

    public ByteBuf getBytes(int index, ByteBuf dst, int length) {
        return this.source.getBytes(index, dst, length);
    }

    public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
        return this.source.getBytes(index, dst, dstIndex, length);
    }

    public ByteBuf getBytes(int index, byte[] dst) {
        return this.source.getBytes(index, dst);
    }

    public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
        return this.source.getBytes(index, dst, dstIndex, length);
    }

    public ByteBuf getBytes(int index, ByteBuffer dst) {
        return this.source.getBytes(index, dst);
    }

    public ByteBuf getBytes(int index, OutputStream out, int length) throws IOException {
        return this.source.getBytes(index, out, length);
    }

    public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
        return this.source.getBytes(index, out, length);
    }

    public int getBytes(int index, FileChannel out, long position, int length) throws IOException {
        return this.source.getBytes(index, out, position, length);
    }

    public CharSequence getCharSequence(int index, int length, Charset charset) {
        return this.source.getCharSequence(index, length, charset);
    }

    public ByteBuf setBoolean(int index, boolean value) {
        return this.source.setBoolean(index, value);
    }

    public ByteBuf setByte(int index, int value) {
        return this.source.setByte(index, value);
    }

    public ByteBuf setShort(int index, int value) {
        return this.source.setShort(index, value);
    }

    public ByteBuf setShortLE(int index, int value) {
        return this.source.setShortLE(index, value);
    }

    public ByteBuf setMedium(int index, int value) {
        return this.source.setMedium(index, value);
    }

    public ByteBuf setMediumLE(int index, int value) {
        return this.source.setMediumLE(index, value);
    }

    public ByteBuf setInt(int index, int value) {
        return this.source.setInt(index, value);
    }

    public ByteBuf setIntLE(int index, int value) {
        return this.source.setIntLE(index, value);
    }

    public ByteBuf setLong(int index, long value) {
        return this.source.setLong(index, value);
    }

    public ByteBuf setLongLE(int index, long value) {
        return this.source.setLongLE(index, value);
    }

    public ByteBuf setChar(int index, int value) {
        return this.source.setChar(index, value);
    }

    public ByteBuf setFloat(int index, float value) {
        return this.source.setFloat(index, value);
    }

    public ByteBuf setDouble(int index, double value) {
        return this.source.setDouble(index, value);
    }

    public ByteBuf setBytes(int index, ByteBuf src) {
        return this.source.setBytes(index, src);
    }

    public ByteBuf setBytes(int index, ByteBuf src, int length) {
        return this.source.setBytes(index, src, length);
    }

    public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
        return this.source.setBytes(index, src, srcIndex, length);
    }

    public ByteBuf setBytes(int index, byte[] src) {
        return this.source.setBytes(index, src);
    }

    public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
        return this.source.setBytes(index, src, srcIndex, length);
    }

    public ByteBuf setBytes(int index, ByteBuffer src) {
        return this.source.setBytes(index, src);
    }

    public int setBytes(int index, InputStream in, int length) throws IOException {
        return this.source.setBytes(index, in, length);
    }

    public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
        return this.source.setBytes(index, in, length);
    }

    public int setBytes(int index, FileChannel in, long position, int length) throws IOException {
        return this.source.setBytes(index, in, position, length);
    }

    public ByteBuf setZero(int index, int length) {
        return this.source.setZero(index, length);
    }

    public int setCharSequence(int index, CharSequence sequence, Charset charset) {
        return this.source.setCharSequence(index, sequence, charset);
    }

    public boolean readBoolean() {
        return this.source.readBoolean();
    }

    public byte readByte() {
        return this.source.readByte();
    }

    public short readUnsignedByte() {
        return this.source.readUnsignedByte();
    }

    public short readShort() {
        return this.source.readShort();
    }

    public short readShortLE() {
        return this.source.readShortLE();
    }

    public int readUnsignedShort() {
        return this.source.readUnsignedShort();
    }

    public int readUnsignedShortLE() {
        return this.source.readUnsignedShortLE();
    }

    public int readMedium() {
        return this.source.readMedium();
    }

    public int readMediumLE() {
        return this.source.readMediumLE();
    }

    public int readUnsignedMedium() {
        return this.source.readUnsignedMedium();
    }

    public int readUnsignedMediumLE() {
        return this.source.readUnsignedMediumLE();
    }

    public int readInt() {
        return this.source.readInt();
    }

    public int readIntLE() {
        return this.source.readIntLE();
    }

    public long readUnsignedInt() {
        return this.source.readUnsignedInt();
    }

    public long readUnsignedIntLE() {
        return this.source.readUnsignedIntLE();
    }

    public long readLong() {
        return this.source.readLong();
    }

    public long readLongLE() {
        return this.source.readLongLE();
    }

    public char readChar() {
        return this.source.readChar();
    }

    public float readFloat() {
        return this.source.readFloat();
    }

    public double readDouble() {
        return this.source.readDouble();
    }

    public ByteBuf readBytes(int length) {
        return this.source.readBytes(length);
    }

    public ByteBuf readSlice(int length) {
        return this.source.readSlice(length);
    }

    public ByteBuf readRetainedSlice(int length) {
        return this.source.readRetainedSlice(length);
    }

    public ByteBuf readBytes(ByteBuf dst) {
        return this.source.readBytes(dst);
    }

    public ByteBuf readBytes(ByteBuf dst, int length) {
        return this.source.readBytes(dst, length);
    }

    public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length) {
        return this.source.readBytes(dst, dstIndex, length);
    }

    public ByteBuf readBytes(byte[] dst) {
        return this.source.readBytes(dst);
    }

    public ByteBuf readBytes(byte[] dst, int dstIndex, int length) {
        return this.source.readBytes(dst, dstIndex, length);
    }

    public ByteBuf readBytes(ByteBuffer dst) {
        return this.source.readBytes(dst);
    }

    public ByteBuf readBytes(OutputStream out, int length) throws IOException {
        return this.source.readBytes(out, length);
    }

    public int readBytes(GatheringByteChannel out, int length) throws IOException {
        return this.source.readBytes(out, length);
    }

    public CharSequence readCharSequence(int length, Charset charset) {
        return this.source.readCharSequence(length, charset);
    }

    public int readBytes(FileChannel out, long position, int length) throws IOException {
        return this.source.readBytes(out, position, length);
    }

    public ByteBuf skipBytes(int length) {
        return this.source.skipBytes(length);
    }

    public ByteBuf writeBoolean(boolean value) {
        return this.source.writeBoolean(value);
    }

    public ByteBuf writeByte(int value) {
        return this.source.writeByte(value);
    }

    public ByteBuf writeShort(int value) {
        return this.source.writeShort(value);
    }

    public ByteBuf writeShortLE(int value) {
        return this.source.writeShortLE(value);
    }

    public ByteBuf writeMedium(int value) {
        return this.source.writeMedium(value);
    }

    public ByteBuf writeMediumLE(int value) {
        return this.source.writeMediumLE(value);
    }

    public ByteBuf writeInt(int value) {
        return this.source.writeInt(value);
    }

    public ByteBuf writeIntLE(int value) {
        return this.source.writeIntLE(value);
    }

    public ByteBuf writeLong(long value) {
        return this.source.writeLong(value);
    }

    public ByteBuf writeLongLE(long value) {
        return this.source.writeLongLE(value);
    }

    public ByteBuf writeChar(int value) {
        return this.source.writeChar(value);
    }

    public ByteBuf writeFloat(float value) {
        return this.source.writeFloat(value);
    }

    public ByteBuf writeDouble(double value) {
        return this.source.writeDouble(value);
    }

    public ByteBuf writeBytes(ByteBuf src) {
        return this.source.writeBytes(src);
    }

    public ByteBuf writeBytes(ByteBuf src, int length) {
        return this.source.writeBytes(src, length);
    }

    public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length) {
        return this.source.writeBytes(src, srcIndex, length);
    }

    public ByteBuf writeBytes(byte[] src) {
        return this.source.writeBytes(src);
    }

    public ByteBuf writeBytes(byte[] src, int srcIndex, int length) {
        return this.source.writeBytes(src, srcIndex, length);
    }

    public ByteBuf writeBytes(ByteBuffer src) {
        return this.source.writeBytes(src);
    }

    public int writeBytes(InputStream in, int length) throws IOException {
        return this.source.writeBytes(in, length);
    }

    public int writeBytes(ScatteringByteChannel in, int ilength) throws IOException {
        return this.source.writeBytes(in, ilength);
    }

    public int writeBytes(FileChannel in, long position, int length) throws IOException {
        return this.source.writeBytes(in, position, length);
    }

    public ByteBuf writeZero(int length) {
        return this.source.writeZero(length);
    }

    public int writeCharSequence(CharSequence sequence, Charset charset) {
        return this.source.writeCharSequence(sequence, charset);
    }

    public int indexOf(int fromIndex, int toIndex, byte value) {
        return this.source.indexOf(fromIndex, toIndex, value);
    }

    public int bytesBefore(byte value) {
        return this.source.bytesBefore(value);
    }

    public int bytesBefore(int length, byte value) {
        return this.source.bytesBefore(length, value);
    }

    public int bytesBefore(int index, int length, byte value) {
        return this.source.bytesBefore(index, length, value);
    }

    public int forEachByte(ByteProcessor processor) {
        return this.source.forEachByte(processor);
    }

    public int forEachByte(int index, int length, ByteProcessor processor) {
        return this.source.forEachByte(index, length, processor);
    }

    public int forEachByteDesc(ByteProcessor processor) {
        return this.source.forEachByteDesc(processor);
    }

    public int forEachByteDesc(int index, int length, ByteProcessor processor) {
        return this.source.forEachByteDesc(index, length, processor);
    }

    public ByteBuf copy() {
        return this.source.copy();
    }

    public ByteBuf copy(int index, int length) {
        return this.source.copy(index, length);
    }

    public ByteBuf slice() {
        return this.source.slice();
    }

    public ByteBuf retainedSlice() {
        return this.source.retainedSlice();
    }

    public ByteBuf slice(int index, int length) {
        return this.source.slice(index, length);
    }

    public ByteBuf retainedSlice(int index, int length) {
        return this.source.retainedSlice(index, length);
    }

    public ByteBuf duplicate() {
        return this.source.duplicate();
    }

    public ByteBuf retainedDuplicate() {
        return this.source.retainedDuplicate();
    }

    public int nioBufferCount() {
        return this.source.nioBufferCount();
    }

    public ByteBuffer nioBuffer() {
        return this.source.nioBuffer();
    }

    public ByteBuffer nioBuffer(int index, int length) {
        return this.source.nioBuffer(index, length);
    }

    public ByteBuffer internalNioBuffer(int index, int length) {
        return this.source.internalNioBuffer(index, length);
    }

    public ByteBuffer[] nioBuffers() {
        return this.source.nioBuffers();
    }

    public ByteBuffer[] nioBuffers(int index, int length) {
        return this.source.nioBuffers(index, length);
    }

    public boolean hasArray() {
        return this.source.hasArray();
    }

    public byte[] array() {
        return this.source.array();
    }

    public int arrayOffset() {
        return this.source.arrayOffset();
    }

    public boolean hasMemoryAddress() {
        return this.source.hasMemoryAddress();
    }

    public long memoryAddress() {
        return this.source.memoryAddress();
    }

    public String toString(Charset charset) {
        return this.source.toString(charset);
    }

    public String toString(int index, int length, Charset charset) {
        return this.source.toString(index, length, charset);
    }

    public int hashCode() {
        return this.source.hashCode();
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object obj) {
        return this.source.equals(obj);
    }

    public int compareTo(ByteBuf buffer) {
        return this.source.compareTo(buffer);
    }

    public String toString() {
        return this.source.toString();
    }

    public ByteBuf retain(int increment) {
        return this.source.retain(increment);
    }

    public ByteBuf retain() {
        return this.source.retain();
    }

    public ByteBuf touch() {
        return this.source.touch();
    }

    public ByteBuf touch(Object hint) {
        return this.source.touch(hint);
    }

    public int refCnt() {
        return this.source.refCnt();
    }

    public boolean release() {
        return this.source.release();
    }

    public boolean release(int decrement) {
        return this.source.release(decrement);
    }
}
