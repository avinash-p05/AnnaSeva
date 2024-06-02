package com.techelites.annaseva

import android.content.Context
import android.net.Uri
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.UnsupportedEncodingException

class MultipartRequest(
    private val context: Context,
    url: String,
    private val headers: Map<String, String>,
    private val params: Map<String, String>,
    private val file: Uri?,
    private val fileKey: String,
    private val responseListener: Response.Listener<NetworkResponse>,
    errorListener: Response.ErrorListener
) : Request<NetworkResponse>(Method.POST, url, errorListener) {

    private val boundary = "apiclient-${System.currentTimeMillis()}"
    private val mimeType = "multipart/form-data;boundary=$boundary"

    override fun getHeaders(): MutableMap<String, String> {
        return headers.toMutableMap()
    }

    override fun getBodyContentType(): String {
        return mimeType
    }

    override fun getBody(): ByteArray {
        val bos = ByteArrayOutputStream()
        val dos = DataOutputStream(bos)
        try {
            // Populate text payload
            for (param in params.entries) {
                buildTextPart(dos, param.key, param.value)
            }
            // Populate file payload if file is not null
            file?.let {
                buildFilePart(dos, fileKey, it)
            }
            // Close multipart form data
            dos.writeBytes("--$boundary--\r\n")
            dos.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bos.toByteArray()
    }

    @Throws(IOException::class)
    private fun buildTextPart(dos: DataOutputStream, parameterName: String, parameterValue: String) {
        dos.writeBytes("--$boundary\r\n")
        dos.writeBytes("Content-Disposition: form-data; name=\"$parameterName\"\r\n\r\n")
        dos.writeBytes(parameterValue + "\r\n")
    }

    @Throws(IOException::class)
    private fun buildFilePart(dos: DataOutputStream, inputName: String, fileUri: Uri) {
        dos.writeBytes("--$boundary\r\n")
        dos.writeBytes("Content-Disposition: form-data; name=\"$inputName\"; filename=\"${fileUri.lastPathSegment}\"\r\n")
        dos.writeBytes("Content-Type: ${context.contentResolver.getType(fileUri)}\r\n\r\n")
        val inputStream: InputStream? = context.contentResolver.openInputStream(fileUri)
        val buffer = ByteArray(1024)
        var bytesRead = inputStream?.read(buffer) ?: -1
        while (bytesRead != -1) {
            dos.write(buffer, 0, bytesRead)
            bytesRead = inputStream?.read(buffer) ?: -1
        }
        dos.writeBytes("\r\n")
    }

    override fun parseNetworkResponse(response: NetworkResponse?): Response<NetworkResponse> {
        return try {
            Response.success(response, HttpHeaderParser.parseCacheHeaders(response))
        } catch (e: UnsupportedEncodingException) {
            Response.error(ParseError(e))
        }
    }

    override fun deliverResponse(response: NetworkResponse?) {
        responseListener.onResponse(response)
    }
}

