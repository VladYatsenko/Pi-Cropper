package com.yatsenko.imagepicker.ui.cropper.utils

import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RandomAccessReader(data: ByteArray?, length: Int) {
    private val data: ByteBuffer = ByteBuffer.wrap(data)
        .order(ByteOrder.BIG_ENDIAN)
        .limit(length) as ByteBuffer

    fun order(byteOrder: ByteOrder?) {
        data.order(byteOrder)
    }

    fun length(): Int {
        return data.remaining()
    }

    fun getInt32(offset: Int): Int {
        return data.getInt(offset)
    }

    fun getInt16(offset: Int): Short {
        return data.getShort(offset)
    }

}

private interface Reader {
    @Throws(IOException::class)
    fun getUInt16(): Int

    @Throws(IOException::class)
    fun getUInt8(): Short

    @Throws(IOException::class)
    fun skip(total: Long): Long

    @Throws(IOException::class)
    fun read(buffer: ByteArray?, byteCount: Int): Int
}

class StreamReader (private val inputStream: InputStream) : Reader {

    override fun getUInt16(): Int {
        return inputStream.read() shl 8 and 0xFF00 or (inputStream.read() and 0xFF)
    }

    override fun getUInt8(): Short {
        return (inputStream.read() and 0xFF).toShort()
    }

    @Throws(IOException::class)
    override fun skip(total: Long): Long {
        if (total < 0) {
            return 0
        }
        var toSkip = total
        while (toSkip > 0) {
            val skipped = inputStream.skip(toSkip)
            if (skipped > 0) {
                toSkip -= skipped
            } else {
                // Skip has no specific contract as to what happens when you reach the end of
                // the stream. To differentiate between temporarily not having more data and
                // having finished the stream, we read a single byte when we fail to skip any
                // amount of data.
                val testEofByte = inputStream.read()
                if (testEofByte == -1) {
                    break
                } else {
                    toSkip--
                }
            }
        }
        return total - toSkip
    }

    @Throws(IOException::class)
    override fun read(buffer: ByteArray?, byteCount: Int): Int {
        var toRead = byteCount
        var read = 0
        while (toRead > 0 && inputStream.read(buffer, byteCount - toRead, toRead).also { read = it } != -1) {
            toRead -= read
        }
        return byteCount - toRead
    }
}
