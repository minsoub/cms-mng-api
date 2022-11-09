package com.bithumbsystems.cms.api.util

import org.apache.commons.lang3.StringUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object PortCheckUtil {
    private val logger by Logger()

    @Throws(IOException::class)
    fun isRunning(port: Int): Boolean {
        return isRunning(executeGrepProcessCommand(port))
    }

    @Throws(IOException::class)
    fun findAvailablePort(): Int {
        for (port in 10000..65535) {
            val process = executeGrepProcessCommand(port)
            if (!isRunning(process)) {
                return port
            }
        }
        throw IllegalArgumentException("Not Found Available port: 10000 ~ 65535")
    }

    @Throws(IOException::class)
    private fun executeGrepProcessCommand(port: Int): Process {
        val command = "netstat -nat | grep LISTEN|grep $port"
        logger.info(command)
        val shell = arrayOf("/bin/sh", "-c", command)
        return Runtime.getRuntime().exec(shell)
    }

    private fun isRunning(process: Process): Boolean {
        var line: String?
        val pidInfo = StringBuilder()
        try {
            BufferedReader(InputStreamReader(process.inputStream)).use { input ->
                while (input.readLine().also {
                    line = it
                } != null
                ) {
                    pidInfo.append(line)
                }
            }
        } catch (e: IOException) {
            logger.error(e.localizedMessage)
        }
        return !StringUtils.isEmpty(pidInfo.toString())
    }
}
