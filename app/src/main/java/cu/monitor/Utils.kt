package cu.monitor

import java.io.File

typealias CuCsv = HashMap<String, MutableList<String>>

class Utils {
    fun IsFileExist(filePath: String): Boolean {
        val file = File(filePath)
        if (file.exists()) {
            return true
        }
        return false
    }

    fun ReadFile(filePath: String): String {
        var string = ""
        try {
            val file = File(filePath)
            if (file.exists()) {
                string = file.readText(Charsets.UTF_8)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return string
    }

    fun WriteFile(string: String, filePath: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                file.createNewFile()
            }
            if (file.exists()) {
                file.writeText(string, Charsets.UTF_8)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun DeleteFile(filePath: String) {
        try {
            val file = File(filePath)
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun MakeDir(dirPath: String) {
        try {
            val dir = File(dirPath)
            if (!dir.exists()) {
                dir.mkdir()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun GetPostString(str: String, delemeter: String): String {
        var postStr = str
        val delemeterIdx = str.indexOf(delemeter)
        if (delemeterIdx != -1) {
            postStr = str.substring(delemeterIdx + 1)
        }

        return postStr
    }

    fun GetRePostString(str: String, delemeter: String): String {
        var postStr = str
        val delemeterIdx = str.lastIndexOf(delemeter)
        if (delemeterIdx != -1) {
            postStr = str.substring(delemeterIdx + 1)
        }

        return postStr
    }

    fun GetPrevString(str: String, delemeter: String): String {
        var prevStr = str
        val delemeterIdx = str.indexOf(delemeter)
        if (delemeterIdx != -1) {
            prevStr = str.substring(0, delemeterIdx)
        }

        return prevStr
    }

    fun GetRePrevString(str: String, delemeter: String): String {
        var prevStr = str
        val delemeterIdx = str.lastIndexOf(delemeter)
        if (delemeterIdx != -1) {
            prevStr = str.substring(0, delemeterIdx)
        }

        return prevStr
    }


    fun ParseCuCsv(csvText: String): CuCsv {
        val csv = CuCsv()
        val lines = csvText.split("\n")
        val idxToColumnName = HashMap<Int, String>()
        val columnNames = lines[0].split(",")
        for (idx in 0..columnNames.lastIndex) {
            val columnName = columnNames[idx]
            csv[columnName] = mutableListOf<String>()
            idxToColumnName[idx] = columnName
        }
        for (line in lines) {
            val cells = line.split(",")
            if (cells.lastIndex == columnNames.lastIndex) {
                for (idx in 0..cells.lastIndex) {
                    csv[idxToColumnName[idx]]!!.add(cells[idx])
                }
            }
        }

        return csv
    }
}