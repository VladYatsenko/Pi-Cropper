package com.yatsenko.imagepicker.ui.cropper.utils

import android.media.ExifInterface
import android.text.TextUtils
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

class ImageHeaderParser(private val inputStream: InputStream) {

    companion object {
        /**
         * A constant indicating we were unable to parse the orientation from the image either because
         * no exif segment containing orientation data existed, or because of an I/O error attempting to
         * read the exif segment.
         */
        const val UNKNOWN_ORIENTATION = -1

        private const val EXIF_MAGIC_NUMBER = 0xFFD8

        // "MM".
        private const val MOTOROLA_TIFF_MAGIC_NUMBER = 0x4D4D

        // "II".
        private const val INTEL_TIFF_MAGIC_NUMBER = 0x4949
        private const val JPEG_EXIF_SEGMENT_PREAMBLE = "Exif\u0000\u0000"
        private val JPEG_EXIF_SEGMENT_PREAMBLE_BYTES = JPEG_EXIF_SEGMENT_PREAMBLE.toByteArray(Charset.forName("UTF-8"))
        private const val SEGMENT_SOS = 0xDA
        private const val MARKER_EOI = 0xD9
        private const val SEGMENT_START_ID = 0xFF
        private const val EXIF_SEGMENT_TYPE = 0xE1
        private const val ORIENTATION_TAG_TYPE = 0x0112
        private val BYTES_PER_FORMAT = intArrayOf(0, 1, 1, 2, 4, 8, 1, 1, 2, 4, 8, 4, 8)

        fun handles(imageMagicNumber: Int): Boolean {
            return (imageMagicNumber and EXIF_MAGIC_NUMBER) == EXIF_MAGIC_NUMBER
                    || imageMagicNumber == MOTOROLA_TIFF_MAGIC_NUMBER
                    || imageMagicNumber == INTEL_TIFF_MAGIC_NUMBER
        }
    }

    private val reader = StreamReader(inputStream)

    @Throws(IOException::class)
    fun getOrientation(): Int {
        val magicNumber = reader.getUInt16()
        return if (!ImageHeaderParser.handles(magicNumber)) {
            UNKNOWN_ORIENTATION
        } else {
            val exifSegmentLength: Int = moveToExifSegmentAndGetLength()
            if (exifSegmentLength == -1) {
                return UNKNOWN_ORIENTATION
            }
            val exifData = ByteArray(exifSegmentLength)
            parseExifSegment(exifData, exifSegmentLength)
        }
    }

    @Throws(IOException::class)
    private fun parseExifSegment(tempArray: ByteArray, exifSegmentLength: Int): Int {
        val read = reader.read(tempArray, exifSegmentLength)
        if (read != exifSegmentLength) {
            return UNKNOWN_ORIENTATION
        }
        val hasJpegExifPreamble = hasJpegExifPreamble(tempArray, exifSegmentLength)
        return if (hasJpegExifPreamble) {
            parseExifSegment(RandomAccessReader(tempArray, exifSegmentLength))
        } else {
            UNKNOWN_ORIENTATION
        }
    }

    private fun hasJpegExifPreamble(exifData: ByteArray?, exifSegmentLength: Int): Boolean {
        var result = exifData != null && exifSegmentLength > JPEG_EXIF_SEGMENT_PREAMBLE_BYTES.size
        if (result) {
            for (i in JPEG_EXIF_SEGMENT_PREAMBLE_BYTES.indices) {
                if (exifData!![i] != JPEG_EXIF_SEGMENT_PREAMBLE_BYTES.get(i)) {
                    result = false
                    break
                }
            }
        }
        return result
    }

    /**
     * Moves reader to the start of the exif segment and returns the length of the exif segment or
     * `-1` if no exif segment is found.
     */
    @Throws(IOException::class)
    private fun moveToExifSegmentAndGetLength(): Int {
        var segmentId: Short
        var segmentType: Short
        var segmentLength: Int
        while (true) {
            segmentId = reader.getUInt8()
            if (segmentId.toInt() != SEGMENT_START_ID) {
                return -1
            }
            segmentType = reader.getUInt8()
            if (segmentType.toInt() == SEGMENT_SOS) {
                return -1
            } else if (segmentType.toInt() == MARKER_EOI) {
                return -1
            }

            // Segment length includes bytes for segment length.
            segmentLength = reader.getUInt16() - 2
            if (segmentType.toInt() != EXIF_SEGMENT_TYPE) {
                val skipped = reader.skip(segmentLength.toLong())
                if (skipped != segmentLength.toLong()) {
                    return -1
                }
            } else {
                return segmentLength
            }
        }
    }

    private fun parseExifSegment(segmentData: RandomAccessReader): Int {
        val headerOffsetSize: Int = JPEG_EXIF_SEGMENT_PREAMBLE.length
        val byteOrderIdentifier: Short = segmentData.getInt16(headerOffsetSize)
        val byteOrder: ByteOrder = when {
            byteOrderIdentifier.toInt() == MOTOROLA_TIFF_MAGIC_NUMBER -> ByteOrder.BIG_ENDIAN
            byteOrderIdentifier.toInt() == INTEL_TIFF_MAGIC_NUMBER -> ByteOrder.LITTLE_ENDIAN
            else -> ByteOrder.BIG_ENDIAN
        }
        segmentData.order(byteOrder)
        val firstIfdOffset: Int = segmentData.getInt32(headerOffsetSize + 4) + headerOffsetSize
        val tagCount: Int = segmentData.getInt16(firstIfdOffset).toInt()
        var tagOffset: Int
        var tagType: Int
        var formatCode: Int
        var componentCount: Int
        for (i in 0 until tagCount) {
            tagOffset = calcTagOffset(firstIfdOffset, i)
            tagType = segmentData.getInt16(tagOffset).toInt()

            // We only want orientation.
            if (tagType != ORIENTATION_TAG_TYPE) {
                continue
            }
            formatCode = segmentData.getInt16(tagOffset + 2).toInt()

            // 12 is max format code.
            if (formatCode < 1 || formatCode > 12) {
                continue
            }
            componentCount = segmentData.getInt32(tagOffset + 4)
            if (componentCount < 0) {
                continue
            }
            val byteCount: Int = componentCount + BYTES_PER_FORMAT[formatCode]
            if (byteCount > 4) {
                continue
            }
            val tagValueOffset = tagOffset + 8
            if (tagValueOffset < 0 || tagValueOffset > segmentData.length()) {
                continue
            }
            if (byteCount < 0 || tagValueOffset + byteCount > segmentData.length()) {
                continue
            }

            //assume componentCount == 1 && fmtCode == 3
            return segmentData.getInt16(tagValueOffset).toInt()
        }
        return -1
    }

    private fun calcTagOffset(ifdOffset: Int, tagIndex: Int): Int {
        return ifdOffset + 2 + 12 * tagIndex
    }

    fun copyExif(originalExif: ExifInterface, width: Int, height: Int, imageOutputPath: String?) {
        val attributes = arrayOf(
            ExifInterface.TAG_APERTURE,
            ExifInterface.TAG_DATETIME,
            ExifInterface.TAG_DATETIME_DIGITIZED,
            ExifInterface.TAG_EXPOSURE_TIME,
            ExifInterface.TAG_FLASH,
            ExifInterface.TAG_FOCAL_LENGTH,
            ExifInterface.TAG_GPS_ALTITUDE,
            ExifInterface.TAG_GPS_ALTITUDE_REF,
            ExifInterface.TAG_GPS_DATESTAMP,
            ExifInterface.TAG_GPS_LATITUDE,
            ExifInterface.TAG_GPS_LATITUDE_REF,
            ExifInterface.TAG_GPS_LONGITUDE,
            ExifInterface.TAG_GPS_LONGITUDE_REF,
            ExifInterface.TAG_GPS_PROCESSING_METHOD,
            ExifInterface.TAG_GPS_TIMESTAMP,
            ExifInterface.TAG_ISO,
            ExifInterface.TAG_MAKE,
            ExifInterface.TAG_MODEL,
            ExifInterface.TAG_SUBSEC_TIME,
            ExifInterface.TAG_SUBSEC_TIME_DIG,
            ExifInterface.TAG_SUBSEC_TIME_ORIG,
            ExifInterface.TAG_WHITE_BALANCE
        )
        try {
            val newExif = ExifInterface(imageOutputPath!!)
            var value: String?
            for (attribute in attributes) {
                value = originalExif.getAttribute(attribute)
                if (!TextUtils.isEmpty(value)) {
                    newExif.setAttribute(attribute, value)
                }
            }
            newExif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, width.toString())
            newExif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, height.toString())
            newExif.setAttribute(ExifInterface.TAG_ORIENTATION, "0")
            newExif.saveAttributes()
        } catch (e: IOException) {
        }
    }

}