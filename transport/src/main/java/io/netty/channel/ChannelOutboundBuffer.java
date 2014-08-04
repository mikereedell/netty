/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

import java.nio.ByteBuffer;


/**
 * (Transport implementors only) an internal data structure used by {@link Channel} to store its pending
 * outbound write requests.
 *
 * All the methods should only be called by the {@link EventLoop} of the {@link Channel}.
 */
public interface ChannelOutboundBuffer {

    /**
     * Add given message to this {@link ChannelOutboundBuffer}. The given {@link ChannelPromise} will be notified once
     * the message was written.
     */
    void addMessage(Object msg, ChannelPromise promise);

    /**
     * Add a flush to this {@link ChannelOutboundBuffer}. This means all previous added messages are marked as flushed
     * and so you will be able to handle them.
     */
    void addFlush();

    /**
     * Return the current message to write or {@code null} if nothing was flushed before and so is ready to be written.
     */
    Object current();

    /**
     * Return the current message to write or {@code null} if nothing was flushed before and so is ready to be written.
     * If {@code true} is specified a direct {@link ByteBuf} or {@link ByteBufHolder} is prefered and
     * so the current message may be copied into a direct buffer.
     */
    Object current(boolean preferDirect);

    /**
     * Replace the current msg with the given one.
     * {@link ReferenceCountUtil#release(Object)} will automatically be called on the replaced message.
     */
    void current(Object msg);

    /**
     * Notify the {@link ChannelPromise} of the current message about writing progress.
     */
    void progress(long amount);

    /**
     * Will remove the current message, mark its {@link ChannelPromise} as success and return {@code true}. If no
     * flushed message exists at the time this method is called it will return {@code false} to signal that no more
     * messages are ready to be handled.
     */
    boolean remove();

    /**
     * Will remove the current message, mark its {@link ChannelPromise} as failure using the given {@link Throwable}
     * and return {@code true}. If no   flushed message exists at the time this method is called it will return
     * {@code false} to signal that no more messages are ready to be handled.
     */
    boolean remove(Throwable cause);

    /**
     * Removes the fully written entries and update the reader index of the partially written entry.
     * This operation assumes all messages in this buffer is {@link ByteBuf}.
     */
    void removeBytes(long writtenBytes);

    /**
     * Returns an array of direct NIO buffers if the currently pending messages are made of {@link ByteBuf} only.
     * {@link #nioBufferCount()} and {@link #nioBufferSize()} will return the number of NIO buffers in the returned
     * array and the total number of readable bytes of the NIO buffers respectively.
     * <p>
     * Note that the returned array is reused and thus should not escape
     * {@link AbstractChannel#doWrite(ChannelOutboundBuffer)}.
     * Refer to {@link NioSocketChannel#doWrite(ChannelOutboundBuffer)} for an example.
     * </p>
     */
    ByteBuffer[] nioBuffers();

    /**
     * Returns the number of {@link ByteBuffer} that can be written out of the {@link ByteBuffer} array that was
     * obtained via {@link #nioBuffers()}. This method <strong>MUST</strong> be called after {@link #nioBuffers()}
     * was called.
     */
    int nioBufferCount();

    /**
     * Returns the number of bytes that can be written out of the {@link ByteBuffer} array that was
     * obtained via {@link #nioBuffers()}. This method <strong>MUST</strong> be called after {@link #nioBuffers()}
     * was called.
     */
    long nioBufferSize();

    /**
     * Returns the number of flushed messages in this {@link ChannelOutboundBuffer}.
     */
    int size();

    /**
     * Returns {@code true} if there are flushed messages in this {@link ChannelOutboundBuffer} or {@code false}
     * otherwise.
     */
    boolean isEmpty();

    @Deprecated
    void recycle();

    /**
     * Returns the total number of bytes that are pending by flushed messages in this {@link ChannelOutboundBuffer}.
     */
    long totalPendingWriteBytes();

    /**
     * Call {@link MessageProcessor#processMessage(Object)} for each flushed message
     * in this {@link DefaultChannelOutboundBuffer} until {@link MessageProcessor#processMessage(Object)}
     * returns {@code false} or there are no more flushed messages to process.
     */
    void forEachFlushedMessage(MessageProcessor processor) throws Exception;

    interface MessageProcessor {
        /**
         * Will be called for each flushed message until it either there are no more flushed messages or this
         * method returns {@code false}.
         */
        boolean processMessage(Object msg) throws Exception;
    }
}
