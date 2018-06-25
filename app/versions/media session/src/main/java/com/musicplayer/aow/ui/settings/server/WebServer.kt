package com.musicplayer.aow.ui.settings.server

import java.io.*
import java.net.URLEncoder
import java.util.*

import fi.iki.elonen.InternalRewrite
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.ServerRunner
import fi.iki.elonen.WebServerPlugin
import fi.iki.elonen.WebServerPluginInfo


class WebServer : NanoHTTPD {
    private val rootDirs: MutableList<File>
    private val quiet: Boolean

    private val rootDir: File
        get() = rootDirs[0]

    constructor(host: String, port: Int, wwwroot: File, quiet: Boolean) : super(host, port) {
        this.quiet = quiet
        this.rootDirs = ArrayList()
        this.rootDirs.add(wwwroot)
    }

    constructor(host: String, port: Int, wwwroots: List<File>, quiet: Boolean) : super(host, port) {
        this.quiet = quiet
        this.rootDirs = ArrayList(wwwroots)
    }

    private fun getRootDirs(): List<File> {
        return rootDirs
    }

    private fun addWwwRootDir(wwwroot: File) {
        rootDirs.add(wwwroot)
    }

    public fun st(){
        this.closeAllConnections()
    }
    /**
     * URL-encodes everything between "/"-characters. Encodes spaces as '%20' instead of '+'.
     */
    private fun encodeUri(uri: String): String {
        var newUri = ""
        val st = StringTokenizer(uri, "/ ", true)
        while (st.hasMoreTokens()) {
            val tok = st.nextToken()
            if (tok == "/")
                newUri += "/"
            else if (tok == " ")
                newUri += "%20"
            else {
                try {
                    newUri += URLEncoder.encode(tok, "UTF-8")
                } catch (ignored: UnsupportedEncodingException) {
                }

            }
        }
        return newUri
    }

    override fun serve(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val header = session.headers
        val parms = session.parms
        val uri = session.uri

        if (!quiet) {
            println(session.method.toString() + " '" + uri + "' ")

            var e = header.keys.iterator()
            while (e.hasNext()) {
                val value = e.next()
                println("  HDR: '" + value + "' = '" + header[value] + "'")
            }
            e = parms.keys.iterator()
            while (e.hasNext()) {
                val value = e.next()
                println("  PRM: '" + value + "' = '" + parms[value] + "'")
            }
        }

        for (homeDir in getRootDirs()) {
            // Make sure we won't die of an exception later
            if (!homeDir.isDirectory) {
                return createResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                        "INTERNAL ERRROR: given path is not a directory ($homeDir).")
            }
        }
        return respond(Collections.unmodifiableMap(header), uri)
    }

    private fun respond(headers: Map<String, String>, uri: String): NanoHTTPD.Response {
        var uri = uri
        // Remove URL arguments
        uri = uri.trim { it <= ' ' }.replace(File.separatorChar, '/')
        if (uri.indexOf('?') >= 0) {
            uri = uri.substring(0, uri.indexOf('?'))
        }

        // Prohibit getting out of current directory
        if (uri.startsWith("src/main") || uri.endsWith("src/main") || uri.contains("../")) {
            return createResponse(NanoHTTPD.Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: Won't serve ../ for security reasons.")
        }

        var canServeUri = false
        var homeDir: File? = null
        val roots = getRootDirs()
        var i = 0
        while (!canServeUri && i < roots.size) {
            homeDir = roots[i]
            canServeUri = canServeUri(uri, homeDir)
            i++
        }
        if (!canServeUri) {
            return createResponse(NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Error 404, file not found.")
        }

        // Browsers get confused without '/' after the directory, send a redirect.
        val f = File(homeDir, uri)
        if (f.isDirectory && !uri.endsWith("/")) {
            uri += "/"
            val res = createResponse(NanoHTTPD.Response.Status.REDIRECT, NanoHTTPD.MIME_HTML, "<html><body>Redirected: <a href=\"" +
                    uri + "\">" + uri + "</a></body></html>")
            res.addHeader("Location", uri)
            return res
        }

        if (f.isDirectory) {
            // First look for index files (index.html, index.htm, etc) and if none found, list the directory if readable.
            val indexFile = findIndexFileInDirectory(f)
            return if (indexFile == null) {
                if (f.canRead()) {
                    // No index file, list the directory if it is readable
                    createResponse(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_HTML, listDirectory(uri, f))
                } else {
                    createResponse(NanoHTTPD.Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: No directory listing.")
                }
            } else {
                respond(headers, uri + indexFile)
            }
        }

        val mimeTypeForFile = getMimeTypeForFile(uri)
        val plugin = mimeTypeHandlers[mimeTypeForFile]
        var response: NanoHTTPD.Response? = null
        if (plugin != null) {
            response = plugin.serveFile(uri, headers, f, mimeTypeForFile)
            if (response != null && response is InternalRewrite) {
                val rewrite = response as InternalRewrite?
                return respond(rewrite!!.headers, rewrite.uri)
            }
        } else {
            response = serveFile(uri, headers, f, mimeTypeForFile)
        }
        return if (response != null)
            response
        else
            createResponse(NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Error 404, file not found.")
    }

    private fun canServeUri(uri: String, homeDir: File?): Boolean {
        var canServeUri: Boolean
        val f = File(homeDir, uri)
        canServeUri = f.exists()
        if (!canServeUri) {
            val mimeTypeForFile = getMimeTypeForFile(uri)
            val plugin = mimeTypeHandlers[mimeTypeForFile]
            if (plugin != null) {
                canServeUri = plugin.canServeUri(uri, homeDir)
            }
        }
        return canServeUri
    }

    /**
     * Serves file from homeDir and its' subdirectories (only). Uses only URI, ignores all headers and HTTP parameters.
     */
    internal fun serveFile(uri: String, header: Map<String, String>, file: File, mime: String): NanoHTTPD.Response {
        var res: NanoHTTPD.Response
        try {
            // Calculate etag
            val etag = Integer.toHexString((file.absolutePath + file.lastModified() + "" + file.length()).hashCode())

            // Support (simple) skipping:
            var startFrom: Long = 0
            var endAt: Long = -1
            var range: String? = header["range"]
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length)
                    val minus = range.indexOf('-')
                    try {
                        if (minus > 0) {
                            startFrom = java.lang.Long.parseLong(range.substring(0, minus))
                            endAt = java.lang.Long.parseLong(range.substring(minus + 1))
                        }
                    } catch (ignored: NumberFormatException) {
                    }

                }
            }

            // Change return code and add Content-Range header when skipping is requested
            val fileLen = file.length()
            if (range != null && startFrom >= 0) {
                if (startFrom >= fileLen) {
                    res = createResponse(NanoHTTPD.Response.Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "")
                    res.addHeader("Content-Range", "bytes 0-0/$fileLen")
                    res.addHeader("ETag", etag)
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1
                    }
                    var newLen = endAt - startFrom + 1
                    if (newLen < 0) {
                        newLen = 0
                    }

                    val dataLen = newLen
                    val fis = object : FileInputStream(file) {
                        @Throws(IOException::class)
                        override fun available(): Int {
                            return dataLen.toInt()
                        }
                    }
                    fis.skip(startFrom)

                    res = createResponse(NanoHTTPD.Response.Status.PARTIAL_CONTENT, mime, fis)
                    res.addHeader("Content-Length", "" + dataLen)
                    res.addHeader("Content-Range", "bytes $startFrom-$endAt/$fileLen")
                    res.addHeader("ETag", etag)
                }
            } else {
                if (etag == header["if-none-match"])
                    res = createResponse(NanoHTTPD.Response.Status.NOT_MODIFIED, mime, "")
                else {
                    res = createResponse(NanoHTTPD.Response.Status.OK, mime, FileInputStream(file))
                    res.addHeader("Content-Length", "" + fileLen)
                    res.addHeader("ETag", etag)
                }
            }
        } catch (ioe: IOException) {
            res = createResponse(NanoHTTPD.Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.")
        }

        return res
    }

    // Get MIME type from file name extension, if possible
    private fun getMimeTypeForFile(uri: String): String {
        val dot = uri.lastIndexOf('.')
        var mime: String? = null
        if (dot >= 0) {
            mime = MIME_TYPES[uri.substring(dot + 1).toLowerCase()]
        }
        return if (mime == null) MIME_DEFAULT_BINARY else mime
    }

    // Announce that the file server accepts partial content requests
    private fun createResponse(status: Response.Status, mimeType: String, message: InputStream): NanoHTTPD.Response {
        val res = NanoHTTPD.Response(status, mimeType, message)
        res.addHeader("Accept-Ranges", "bytes")
        return res
    }

    // Announce that the file server accepts partial content requests
    private fun createResponse(status: Response.Status, mimeType: String, message: String): NanoHTTPD.Response {
        val res = NanoHTTPD.Response(status, mimeType, message)
        res.addHeader("Accept-Ranges", "bytes")
        return res
    }

    private fun findIndexFileInDirectory(directory: File): String? {
        for (fileName in INDEX_FILE_NAMES) {
            val indexFile = File(directory, fileName)
            if (indexFile.exists()) {
                return fileName
            }
        }
        return null
    }

    private fun listDirectory(uri: String, f: File): String {
        val heading = "Directory $uri"
        val msg = StringBuilder("<html><head><title>" + heading + "</title><style><!--\n" +
                "span.dirname { font-weight: bold; }\n" +
                "span.filesize { font-size: 75%; }\n" +
                "// -->\n" +
                "</style>" +
                "</head><body><h1>" + heading + "</h1>")

        var up: String? = null
        if (uri.length > 1) {
            val u = uri.substring(0, uri.length - 1)
            val slash = u.lastIndexOf('/')
            if (slash >= 0 && slash < u.length) {
                up = uri.substring(0, slash + 1)
            }
        }

        val files = Arrays.asList(*f.list { dir, name -> File(dir, name).isFile })
        Collections.sort(files)
        val directories = Arrays.asList(*f.list { dir, name -> File(dir, name).isDirectory })
        Collections.sort(directories)
        if (up != null || directories.size + files.size > 0) {
            msg.append("<ul>")
            if (up != null || directories.size > 0) {
                msg.append("<section class=\"directories\">")
                if (up != null) {
                    msg.append("<li><a rel=\"directory\" href=\"").append(up).append("\"><span class=\"dirname\">..</span></a></b></li>")
                }
                for (directory in directories) {
                    val dir = "$directory/"
                    msg.append("<li><a rel=\"directory\" href=\"").append(encodeUri(uri + dir)).append("\"><span class=\"dirname\">").append(dir).append("</span></a></b></li>")
                }
                msg.append("</section>")
            }
            if (files.size > 0) {
                msg.append("<section class=\"files\">")
                for (file in files) {
                    msg.append("<li><a href=\"").append(encodeUri(uri + file)).append("\"><span class=\"filename\">").append(file).append("</span></a>")
                    val curFile = File(f, file)
                    val len = curFile.length()
                    msg.append("&nbsp;<span class=\"filesize\">(")
                    if (len < 1024) {
                        msg.append(len).append(" bytes")
                    } else if (len < 1024 * 1024) {
                        msg.append(len / 1024).append(".").append(len % 1024 / 10 % 100).append(" KB")
                    } else {
                        msg.append(len / (1024 * 1024)).append(".").append(len % (1024 * 1024) / 10 % 100).append(" MB")
                    }
                    msg.append(")</span></li>")
                }
                msg.append("</section>")
            }
            msg.append("</ul>")
        }
        msg.append("</body></html>")
        return msg.toString()
    }

    companion object {
        /**
         * Common mime type for dynamic content: binary
         */
        val MIME_DEFAULT_BINARY = "application/octet-stream"
        /**
         * Default Index file names.
         */
        val INDEX_FILE_NAMES: MutableList<String> = object : ArrayList<String>() {
            init {
                add("index.html")
                add("index.htm")
            }
        }
        /**
         * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
         */
        private val MIME_TYPES = object : HashMap<String, String>() {
            init {
                put("css", "text/css")
                put("htm", "text/html")
                put("html", "text/html")
                put("xml", "text/xml")
                put("java", "text/x-java-source, text/java")
                put("md", "text/plain")
                put("txt", "text/plain")
                put("asc", "text/plain")
                put("gif", "image/gif")
                put("jpg", "image/jpeg")
                put("jpeg", "image/jpeg")
                put("png", "image/png")
                put("mp3", "audio/mpeg")
                put("m3u", "audio/mpeg-url")
                put("mp4", "video/mp4")
                put("ogv", "video/ogg")
                put("flv", "video/x-flv")
                put("mov", "video/quicktime")
                put("swf", "application/x-shockwave-flash")
                put("js", "application/javascript")
                put("pdf", "application/pdf")
                put("doc", "application/msword")
                put("ogg", "application/x-ogg")
                put("zip", "application/octet-stream")
                put("exe", "application/octet-stream")
                put("class", "application/octet-stream")
            }
        }
        /**
         * The distribution licence
         */
        private val LICENCE = (
                "Copyright (c) 2012-2013 by Paul S. Hawke, 2001,2005-2013 by Jarno Elonen, 2010 by Konstantinos Togias\n"
                        + "\n"
                        + "Redistribution and use in source and binary forms, with or without\n"
                        + "modification, are permitted provided that the following conditions\n"
                        + "are met:\n"
                        + "\n"
                        + "Redistributions of source code must retain the above copyright notice,\n"
                        + "this list of conditions and the following disclaimer. Redistributions in\n"
                        + "binary form must reproduce the above copyright notice, this list of\n"
                        + "conditions and the following disclaimer in the documentation and/or other\n"
                        + "materials provided with the distribution. The name of the author may not\n"
                        + "be used to endorse or promote products derived from this software without\n"
                        + "specific prior written permission. \n"
                        + " \n"
                        + "THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR\n"
                        + "IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES\n"
                        + "OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.\n"
                        + "IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,\n"
                        + "INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT\n"
                        + "NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,\n"
                        + "DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY\n"
                        + "THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT\n"
                        + "(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE\n"
                        + "OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.")
        private val mimeTypeHandlers = HashMap<String, WebServerPlugin>()

        /**
         * Starts as a standalone file server and waits for Enter.
         */
//        @JvmStatic
//        fun main(args: Array<String>) {
//            // Defaults
//            var port = 8080
//
//            var host = "127.0.0.1"
//            val rootDirs = ArrayList<File>()
//            var quiet = false
//            val options = HashMap<String, String>()
//
//            // Parse command-line, with short and long versions of the options.
//            for (i in args.indices) {
//                if (args[i].equals("-h", ignoreCase = true) || args[i].equals("--host", ignoreCase = true)) {
//                    host = args[i + 1]
//                } else if (args[i].equals("-p", ignoreCase = true) || args[i].equals("--port", ignoreCase = true)) {
//                    port = Integer.parseInt(args[i + 1])
//                } else if (args[i].equals("-q", ignoreCase = true) || args[i].equals("--quiet", ignoreCase = true)) {
//                    quiet = true
//                } else if (args[i].equals("-d", ignoreCase = true) || args[i].equals("--dir", ignoreCase = true)) {
//                    rootDirs.add(File(args[i + 1]).absoluteFile)
//                } else if (args[i].equals("--licence", ignoreCase = true)) {
//                    println(LICENCE + "\n")
//                } else if (args[i].startsWith("-X:")) {
//                    val dot = args[i].indexOf('=')
//                    if (dot > 0) {
//                        val name = args[i].substring(0, dot)
//                        val value = args[i].substring(dot + 1, args[i].length)
//                        options[name] = value
//                    }
//                }
//            }
//
//            if (rootDirs.isEmpty()) {
//                rootDirs.add(File(".").absoluteFile)
//            }
//
//            options["host"] = host
//            options["port"] = "" + port
//            options["quiet"] = quiet.toString()
//            val sb = StringBuilder()
//            for (dir in rootDirs) {
//                if (sb.length > 0) {
//                    sb.append(":")
//                }
//                try {
//                    sb.append(dir.canonicalPath)
//                } catch (ignored: IOException) {
//                }
//
//            }
//            options["home"] = sb.toString()
//
//            val serviceLoader = ServiceLoader.load(WebServerPluginInfo::class.java)
//            for (info in serviceLoader) {
//                val mimeTypes = info.mimeTypes
//                for (mime in mimeTypes) {
//                    val indexFiles = info.getIndexFilesForMimeType(mime)
//                    if (!quiet) {
//                        print("# Found plugin for Mime type: \"$mime\"")
//                        if (indexFiles != null) {
//                            print(" (serving index files: ")
//                            for (indexFile in indexFiles) {
//                                print("$indexFile ")
//                            }
//                        }
//                        println(").")
//                    }
//                    registerPluginForMimeType(indexFiles, mime, info.getWebServerPlugin(mime), options)
//                }
//            }
//
//            ServerRunner.executeInstance(WebServer(host, port, rootDirs, quiet))
//        }

        private fun registerPluginForMimeType(indexFiles: Array<String>?, mimeType: String?, plugin: WebServerPlugin?, commandLineOptions: Map<String, String>) {
            if (mimeType == null || plugin == null) {
                return
            }

            if (indexFiles != null) {
                for (filename in indexFiles) {
                    val dot = filename.lastIndexOf('.')
                    if (dot >= 0) {
                        val extension = filename.substring(dot + 1).toLowerCase()
                        MIME_TYPES[extension] = mimeType
                    }
                }
                INDEX_FILE_NAMES.addAll(Arrays.asList(*indexFiles))
            }
            mimeTypeHandlers[mimeType] = plugin
            plugin.initialize(commandLineOptions)
        }
    }
}
