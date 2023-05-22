package cu.monitor

import android.content.res.Resources
import android.graphics.Paint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cu.monitor.ui.theme.CuPerfMonitorTheme

class AnalysisActivity : ComponentActivity() {
    private val utils = Utils()

    private var timeMsList = listOf<Int>()
    private var cpuCurFreqList = listOf<List<Int>>()
    private var cpuUsageList = listOf<List<Int>>()
    private var cpuTempList = listOf<Int>()
    private var gpuCurFreqList = listOf<Int>()
    private var gpuUsageList = listOf<Int>()
    private var ddrCurFreqList = listOf<Int>()
    private var batteryPowerList = listOf<Int>()
    private var batteryTempList = listOf<Int>()
    private var batteryPercentList = listOf<Int>()
    private var fpsList = listOf<Int>()
    private var jankList = listOf<Int>()
    private var bigJankList = listOf<Int>()
    private var maxFrameTimeList = listOf<Int>()
    private var ramFreeList = listOf<Int>()

    private val dataColors = hashMapOf(
        0 to Color(0xFF1F77B4),
        1 to Color(0xFF2CA02C),
        2 to Color(0xFFFF7F0E),
        3 to Color(0xFFD62728),
        4 to Color(0xFF9467BD)
    )

    private val clusterToCpu = hashMapOf<Int, Int>()
    private val cpuToCluster = hashMapOf<Int, Int>()
    private var clusterNum = 0

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val recordPath = intent.getStringExtra("recordPath")!!
        val recordText = utils.ReadFile(recordPath)
        val csv = utils.ParseCuCsv(recordText)

        if (csv["appPkgName"] == null) {
            Toast.makeText(applicationContext, "无法载入有效的记录文件", Toast.LENGTH_LONG).show()
            finish()
        }
        if (csv["appPkgName"]!!.size < 5) {
            Toast.makeText(applicationContext, "记录数量过少", Toast.LENGTH_LONG).show()
            finish()
        }

        val timeMsDatas = csv["timeMs"]!!
        val cpuCurFreqDatas = csv["cpuCurFreq"]!!
        val cpuUsageDatas = csv["cpuUsage"]!!
        val cpuTempDatas = csv["cpuTemp"]!!
        val gpuCurFreqDatas = csv["gpuCurFreq"]!!
        val gpuUsageDatas = csv["gpuUsage"]!!
        val ddrCurFreqDatas = csv["ddrCurFreq"]!!
        val batteryPowerDatas = csv["batteryPower"]!!
        val batteryTempDatas = csv["batteryTemp"]!!
        val batteryPercentDatas = csv["batteryPercent"]!!
        val fpsDatas = csv["fps"]!!
        val jankDatas = csv["jank"]!!
        val bigJankDatas = csv["bigJank"]!!
        val maxFrameTimeDatas = csv["maxFrameTime"]!!
        val ramFreeDatas = csv["ramFree"]!!

        timeMsDatas.removeAt(0)
        cpuCurFreqDatas.removeAt(0)
        cpuUsageDatas.removeAt(0)
        cpuTempDatas.removeAt(0)
        gpuCurFreqDatas.removeAt(0)
        gpuUsageDatas.removeAt(0)
        ddrCurFreqDatas.removeAt(0)
        batteryPowerDatas.removeAt(0)
        batteryTempDatas.removeAt(0)
        batteryPercentDatas.removeAt(0)
        fpsDatas.removeAt(0)
        jankDatas.removeAt(0)
        bigJankDatas.removeAt(0)
        maxFrameTimeDatas.removeAt(0)
        ramFreeDatas.removeAt(0)

        timeMsList = timeMsDatas.map { it.toInt() }
        cpuTempList = cpuTempDatas.map { it.toInt() }
        gpuUsageList = gpuUsageDatas.map { it.toInt() }
        gpuCurFreqList = gpuCurFreqDatas.map { it.toInt() }
        ddrCurFreqList = ddrCurFreqDatas.map { it.toInt() }
        batteryPowerList = batteryPowerDatas.map { it.toInt() }
        batteryTempList = batteryTempDatas.map { it.toInt() }
        batteryPercentList = batteryPercentDatas.map { it.toInt() }
        fpsList = fpsDatas.map { it.toInt() }
        jankList = jankDatas.map { it.toInt() }
        bigJankList = bigJankDatas.map { it.toInt() }
        maxFrameTimeList = maxFrameTimeDatas.map { it.toInt() }
        ramFreeList = ramFreeDatas.map { it.toInt() }

        val cpuCurFreqMutableList = mutableListOf<List<Int>>()
        for (cpuCurFreqData in cpuCurFreqDatas) {
            val cpuCurFreqStr = cpuCurFreqData.split("/")
            val cpuCurFreq = cpuCurFreqStr.map { it.toInt() }
            cpuCurFreqMutableList.add(cpuCurFreq)
        }
        cpuCurFreqList = cpuCurFreqMutableList.toList()

        val cpuUsageMutableList = mutableListOf<List<Int>>()
        for (cpuUsageData in cpuUsageDatas) {
            val cpuUsageStr = cpuUsageData.split("/")
            val cpuUsage = cpuUsageStr.map { it.toInt() }
            cpuUsageMutableList.add(cpuUsage)
        }
        cpuUsageList = cpuUsageMutableList.toList()

        GetCpuInfo()

        setContent {
            CuPerfMonitorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        topBar = {
                            Row(
                                modifier = Modifier
                                    .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                                    .height(50.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { finish() },
                                    modifier = Modifier
                                        .height(50.dp)
                                        .width(50.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.mipmap.back),
                                        contentDescription = "GoBack",
                                        modifier = Modifier
                                            .height(30.dp)
                                            .width(30.dp)
                                    )
                                }
                                Text(
                                    modifier = Modifier
                                        .padding(start = 10.dp),
                                    text = "性能数据分析",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(top = it.calculateTopPadding(), start = 10.dp, end = 10.dp, bottom = 10.dp)
                                .fillMaxSize()
                                .padding(top = 10.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            DataMetricsBar()
                            FpsChart()
                            JankChart()
                            MaxFrameTimeChart()
                            CpuCurFreqChart()
                            CpuUsageChart()
                            GpuDataChart()
                            RamDataChart()
                            TempChart()
                            BatteryChart()
                        }
                    }
                }
            }
        }
    }

    override fun getResources(): Resources {
        val resources = super.getResources()
        val configContext = createConfigurationContext(resources.configuration)

        return configContext.resources.apply {
            configuration.fontScale = 1.0f
            displayMetrics.scaledDensity = displayMetrics.density * configuration.fontScale
        }
    }

    @Composable
    private fun DataMetricsBar() {
        var avgFps = 0
        for (fps in fpsList) {
            avgFps += fps
        }
        avgFps = avgFps / fpsList.size

        var fpsVariance = 0
        for (fps in fpsList) {
            val fpsDiff = fps - avgFps
            fpsVariance += fpsDiff * fpsDiff
        }
        fpsVariance = fpsVariance / fpsList.size

        var smoothnessPercentage = 0
        for (idx in 0..fpsList.lastIndex) {
            if (fpsList[idx] > 50 && jankList[idx] == 0 && bigJankList[idx] == 0) {
                smoothnessPercentage++
            }
        }
        smoothnessPercentage = smoothnessPercentage * 100 / fpsList.size

        var maxCpuTemp = 0
        for (cpuTemp in cpuTempList) {
            if (cpuTemp > maxCpuTemp) {
                maxCpuTemp = cpuTemp
            }
        }

        var maxBatteryTemp = 0
        for (batteryTemp in batteryTempList) {
            if (batteryTemp > maxBatteryTemp) {
                maxBatteryTemp = batteryTemp
            }
        }

        var avgBatteryPower = 0
        for (batteryPower in batteryPowerList) {
            avgBatteryPower += batteryPower
        }
        avgBatteryPower = avgBatteryPower / batteryPowerList.size

        Column(
            modifier = Modifier
                .padding(start = 5.dp, end = 5.dp)
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp)
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .height(40.dp)
                        .width(100.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Average FPS",
                        maxLines = 1,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = avgFps.toString(),
                        maxLines = 1,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(
                    modifier = Modifier
                        .height(40.dp)
                        .width(100.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FPS Variance",
                        maxLines = 1,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = fpsVariance.toString(),
                        maxLines = 1,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(
                    modifier = Modifier
                        .height(40.dp)
                        .width(100.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Smoothness",
                        maxLines = 1,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = smoothnessPercentage.toString() + "%",
                        maxLines = 1,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .height(40.dp)
                        .width(100.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Max CpuTemp",
                        maxLines = 1,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = maxCpuTemp.toString(),
                        maxLines = 1,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(
                    modifier = Modifier
                        .height(40.dp)
                        .width(100.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Max BattTemp",
                        maxLines = 1,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = maxBatteryTemp.toString(),
                        maxLines = 1,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(
                    modifier = Modifier
                        .height(40.dp)
                        .width(100.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Average Power",
                        maxLines = 1,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = avgBatteryPower.toString(),
                        maxLines = 1,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    @Composable
    private fun FpsChart() {
        var maxFps = 0
        for (fps in fpsList) {
            if (fps > maxFps) {
                maxFps = fps
            }
        }
        maxFps = maxFps / 10 * 10 + 10
        val maxTimeMs = timeMsList[timeMsList.lastIndex]
        Canvas(
            modifier = Modifier
                .padding(start = 5.dp, end = 5.dp, top = 10.dp)
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp)
                )
        ) {
            drawLine(
                color = Color.Black,
                strokeWidth = 2f,
                start = Offset(x = 50f, y = 50f),
                end = Offset(x = 50f, y = size.height - 50f)
            )
            drawLine(
                color = Color.Black,
                strokeWidth = 2f,
                start = Offset(x = size.width - 50f, y = 50f),
                end = Offset(x = size.width - 50f, y = size.height - 50f)
            )
            drawLine(
                color = Color.Black,
                strokeWidth = 2f,
                start = Offset(x = 50f, y = size.height - 50f),
                end = Offset(x = size.width - 50f, y = size.height - 50f)
            )
            for (i in 0..4) {
                val y = (size.height - 100f) * i / 5 + 50f
                drawLine(
                    color = Color.Gray,
                    strokeWidth = 2f,
                    start = Offset(x = 50f, y = y),
                    end = Offset(x = size.width - 50f, y = y),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f), 2f)
                )
            }
            for (i in 1..4) {
                val x = (size.width - 100f) * i / 5 + 50f
                drawLine(
                    color = Color.Gray,
                    strokeWidth = 2f,
                    start = Offset(x = x, y = 50f),
                    end = Offset(x = x, y = size.height - 50f),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f), 2f)
                )
            }
            val titlePaint = Paint().let {
                it.apply {
                    textSize = 20f
                    color = android.graphics.Color.GRAY
                }
            }
            drawContext.canvas.nativeCanvas.drawText(
                "FPS",
                50f,
                30f,
                titlePaint
            )
            val tickPaint = Paint().let {
                it.apply {
                    textSize = 16f
                    color = android.graphics.Color.GRAY
                }
            }
            for (i in 0..5) {
                val dataTick = (maxFps * i / 5).toString()
                val y = size.height - 50f - (size.height - 100f) * i / 5
                drawContext.canvas.nativeCanvas.drawText(
                    dataTick,
                    20f,
                    y,
                    tickPaint
                )
            }
            for (i in 1..5) {
                val timeVal = (maxTimeMs / 1000) * i / 5
                val timeMin = timeVal / 60
                val timeSec = timeVal % 60
                val timeTick = timeMin.toString() + "m" + timeSec.toString() + "s"
                val x = 30f + (size.width - 100f) * i / 5
                drawContext.canvas.nativeCanvas.drawText(
                    timeTick,
                    x,
                    size.height - 30f,
                    tickPaint
                )
            }
            for (i in 1..fpsList.lastIndex) {
                val startX = 50f + (size.width - 100f) * (i - 1) / fpsList.lastIndex
                val startY = size.height - 50f - (size.height - 100f) * fpsList[i - 1] / maxFps
                val endX = 50f + (size.width - 100f) * i / fpsList.lastIndex
                val endY = size.height - 50f - (size.height - 100f) * fpsList[i] / maxFps
                drawLine(
                    color = dataColors[0]!!,
                    strokeWidth = 2f,
                    start = Offset(x = startX, y = startY),
                    end = Offset(x = endX, y = endY)
                )
            }
        }
    }

    @Composable
    private fun JankChart() {
        val maxTimeMs = timeMsList[timeMsList.lastIndex]
        Column(
            modifier = Modifier
                .padding(top = 10.dp, start = 5.dp, end = 5.dp)
                .height(120.dp)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp)
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                drawLine(
                    color = Color.Black,
                    strokeWidth = 2f,
                    start = Offset(x = 50f, y = 50f),
                    end = Offset(x = 50f, y = size.height - 50f)
                )
                drawLine(
                    color = Color.Black,
                    strokeWidth = 2f,
                    start = Offset(x = size.width - 50f, y = 50f),
                    end = Offset(x = size.width - 50f, y = size.height - 50f)
                )
                drawLine(
                    color = Color.Black,
                    strokeWidth = 2f,
                    start = Offset(x = 50f, y = size.height - 50f),
                    end = Offset(x = size.width - 50f, y = size.height - 50f)
                )
                for (i in 0..4) {
                    val y = (size.height - 100f) * i / 5 + 50f
                    drawLine(
                        color = Color.Gray,
                        strokeWidth = 2f,
                        start = Offset(x = 50f, y = y),
                        end = Offset(x = size.width - 50f, y = y),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f), 2f)
                    )
                }
                for (i in 1..4) {
                    val x = (size.width - 100f) * i / 5 + 50f
                    drawLine(
                        color = Color.Gray,
                        strokeWidth = 2f,
                        start = Offset(x = x, y = 50f),
                        end = Offset(x = x, y = size.height - 50f),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f), 2f)
                    )
                }
                val titlePaint = Paint().let {
                    it.apply {
                        textSize = 20f
                        color = android.graphics.Color.GRAY
                    }
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "JANK",
                    50f,
                    30f,
                    titlePaint
                )
                val tickPaint = Paint().let {
                    it.apply {
                        textSize = 16f
                        color = android.graphics.Color.GRAY
                    }
                }
                for (i in 0..5) {
                    val dataTick = i.toString()
                    val y = size.height - 50f - (size.height - 100f) * i / 5
                    drawContext.canvas.nativeCanvas.drawText(
                        dataTick,
                        20f,
                        y,
                        tickPaint
                    )
                }
                for (i in 1..5) {
                    val timeVal = (maxTimeMs / 1000) * i / 5
                    val timeMin = timeVal / 60
                    val timeSec = timeVal % 60
                    val timeTick = timeMin.toString() + "m" + timeSec.toString() + "s"
                    val x = 30f + (size.width - 100f) * i / 5
                    drawContext.canvas.nativeCanvas.drawText(
                        timeTick,
                        x,
                        size.height - 30f,
                        tickPaint
                    )
                }
                for (i in 0..jankList.lastIndex) {
                    var jank = jankList[i]
                    if (jank > 5) {
                        jank = 5
                    }
                    val rectWidth = (size.width - 100f) / jankList.size
                    val rectHeight = (size.height - 100f) / 5 * jank
                    val rectSize = Size(width = rectWidth, height = rectHeight)
                    val rectX = 50f + (size.width - 100f) / jankList.size * i
                    val rectY = 50f + (size.height - 100f) - rectHeight
                    drawRect(
                        color = dataColors[0]!!,
                        topLeft = Offset(x = rectX, y = rectY),
                        size = rectSize
                    )
                }
                for (i in 0..bigJankList.lastIndex) {
                    var bigJank = bigJankList[i]
                    if (bigJank > 5) {
                        bigJank = 5
                    }
                    val rectWidth = (size.width - 100f) / bigJankList.size
                    val rectHeight = (size.height - 100f) / 5 * bigJank
                    val rectSize = Size(width = rectWidth, height = rectHeight)
                    val rectX = 50f + (size.width - 100f) / bigJankList.size * i
                    val rectY = 50f + (size.height - 100f) - rectHeight
                    drawRect(
                        color = dataColors[1]!!,
                        topLeft = Offset(x = rectX, y = rectY),
                        size = rectSize
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .padding(start = 5.dp, end = 5.dp),
                    text = "█ jank",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = dataColors[0]!!
                )
                Text(
                    modifier = Modifier
                        .padding(start = 5.dp, end = 5.dp),
                    text = "█ bigJank",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = dataColors[1]!!
                )
            }
        }
    }

    @Composable
    private fun MaxFrameTimeChart() {
        val maxTimeMs = timeMsList[timeMsList.lastIndex]
        Canvas(
            modifier = Modifier
                .padding(start = 5.dp, end = 5.dp, top = 10.dp)
                .fillMaxWidth()
                .height(160.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp)
                )
        ) {
            drawLine(
                color = Color.Black,
                strokeWidth = 2f,
                start = Offset(x = 50f, y = 50f),
                end = Offset(x = 50f, y = size.height - 50f)
            )
            drawLine(
                color = Color.Black,
                strokeWidth = 2f,
                start = Offset(x = size.width - 50f, y = 50f),
                end = Offset(x = size.width - 50f, y = size.height - 50f)
            )
            drawLine(
                color = Color.Black,
                strokeWidth = 2f,
                start = Offset(x = 50f, y = size.height - 50f),
                end = Offset(x = size.width - 50f, y = size.height - 50f)
            )
            for (i in 0..4) {
                val y = (size.height - 100f) * i / 5 + 50f
                drawLine(
                    color = Color.Gray,
                    strokeWidth = 2f,
                    start = Offset(x = 50f, y = y),
                    end = Offset(x = size.width - 50f, y = y),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f), 2f)
                )
            }
            for (i in 1..4) {
                val x = (size.width - 100f) * i / 5 + 50f
                drawLine(
                    color = Color.Gray,
                    strokeWidth = 2f,
                    start = Offset(x = x, y = 50f),
                    end = Offset(x = x, y = size.height - 50f),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f), 2f)
                )
            }
            val titlePaint = Paint().let {
                it.apply {
                    textSize = 20f
                    color = android.graphics.Color.GRAY
                }
            }
            drawContext.canvas.nativeCanvas.drawText(
                "maxFrameTime",
                50f,
                30f,
                titlePaint
            )
            val tickPaint = Paint().let {
                it.apply {
                    textSize = 16f
                    color = android.graphics.Color.GRAY
                }
            }
            for (i in 0..5) {
                val dataTick = (20 * i).toString()
                val y = size.height - 50f - (size.height - 100f) * i / 5
                drawContext.canvas.nativeCanvas.drawText(
                    dataTick,
                    20f,
                    y,
                    tickPaint
                )
            }
            for (i in 1..5) {
                val timeVal = (maxTimeMs / 1000) * i / 5
                val timeMin = timeVal / 60
                val timeSec = timeVal % 60
                val timeTick = timeMin.toString() + "m" + timeSec.toString() + "s"
                val x = 30f + (size.width - 100f) * i / 5
                drawContext.canvas.nativeCanvas.drawText(
                    timeTick,
                    x,
                    size.height - 30f,
                    tickPaint
                )
            }
            for (i in 0..maxFrameTimeList.lastIndex) {
                var maxFrameTime = maxFrameTimeList[i]
                if (maxFrameTime > 100) {
                    maxFrameTime = 100
                }
                val rectWidth = (size.width - 100f) / maxFrameTimeList.size
                val rectHeight = (size.height - 100f) / 100 * maxFrameTime
                val rectSize = Size(width = rectWidth, height = rectHeight)
                val rectX = 50f + (size.width - 100f) / maxFrameTimeList.size * i
                val rectY = 50f + (size.height - 100f) - rectHeight
                drawRect(
                    color = dataColors[0]!!,
                    topLeft = Offset(x = rectX, y = rectY),
                    size = rectSize
                )
            }
        }
    }

    @Composable
    private fun CpuCurFreqChart() {
        var maxCpuFreq = 0
        for (cpuFreqs in cpuCurFreqList) {
            for (cpuFreq in cpuFreqs) {
                if (cpuFreq > maxCpuFreq) {
                    maxCpuFreq = cpuFreq
                }
            }
        }
        maxCpuFreq = maxCpuFreq / 500 * 500 + 500
        val maxTimeMs = timeMsList[timeMsList.lastIndex]

        Column(
            modifier = Modifier
                .padding(top = 10.dp, start = 5.dp, end = 5.dp)
                .height(220.dp)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp)
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                drawLine(
                    color = Color.Black,
                    strokeWidth = 2f,
                    start = Offset(x = 50f, y = 50f),
                    end = Offset(x = 50f, y = size.height - 50f)
                )
                drawLine(
                    color = Color.Black,
                    strokeWidth = 2f,
                    start = Offset(x = size.width - 50f, y = 50f),
                    end = Offset(x = size.width - 50f, y = size.height - 50f)
                )
                drawLine(
                    color = Color.Black,
                    strokeWidth = 2f,
                    start = Offset(x = 50f, y = size.height - 50f),
                    end = Offset(x = size.width - 50f, y = size.height - 50f)
                )
                for (i in 0..4) {
                    val y = (size.height - 100f) * i / 5 + 50f
                    drawLine(
                        color = Color.Gray,
                        strokeWidth = 2f,
                        start = Offset(x = 50f, y = y),
                        end = Offset(x = size.width - 50f, y = y),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f), 2f)
                    )
                }
                for (i in 1..4) {
                    val x = (size.width - 100f) * i / 5 + 50f
                    drawLine(
                        color = Color.Gray,
                        strokeWidth = 2f,
                        start = Offset(x = x, y = 50f),
                        end = Offset(x = x, y = size.height - 50f),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f), 2f)
                    )
                }
                val titlePaint = Paint().let {
                    it.apply {
                        textSize = 20f
                        color = android.graphics.Color.GRAY
                    }
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "CpuCurFreq",
                    50f,
                    30f,
                    titlePaint
                )
                val tickPaint = Paint().let {
                    it.apply {
                        textSize = 16f
                        color = android.graphics.Color.GRAY
                    }
                }
                for (i in 0..5) {
                    val dataTick = (maxCpuFreq * i / 5).toString()
                    val y = size.height - 50f - (size.height - 100f) * i / 5
                    drawContext.canvas.nativeCanvas.drawText(
                        dataTick,
                        10f,
                        y,
                        tickPaint
                    )
                }
                for (i in 1..5) {
                    val timeVal = (maxTimeMs / 1000) * i / 5
                    val timeMin = timeVal / 60
                    val timeSec = timeVal % 60
                    val timeTick = timeMin.toString() + "m" + timeSec.toString() + "s"
                    val x = 30f + (size.width - 100f) * i / 5
                    drawContext.canvas.nativeCanvas.drawText(
                        timeTick,
                        x,
                        size.height - 30f,
                        tickPaint
                    )
                }
                for (cluster in 0 until clusterNum) {
                    for (i in 1..cpuCurFreqList.lastIndex) {
                        val startX = 50f + (size.width - 100f) * (i - 1) / cpuCurFreqList.lastIndex
                        val startY =
                            size.height - 50f - (size.height - 100f) * cpuCurFreqList[i - 1][clusterToCpu[cluster]!!] / maxCpuFreq
                        val endX = 50f + (size.width - 100f) * i / cpuCurFreqList.lastIndex
                        val endY =
                            size.height - 50f - (size.height - 100f) * cpuCurFreqList[i][clusterToCpu[cluster]!!] / maxCpuFreq
                        drawLine(
                            color = dataColors[cluster]!!,
                            strokeWidth = 2f,
                            start = Offset(x = startX, y = startY),
                            end = Offset(x = endX, y = endY)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (cluster in 0 until clusterNum) {
                    Text(
                        modifier = Modifier
                            .padding(start = 5.dp, end = 5.dp),
                        text = "█ cluster${cluster}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = dataColors[cluster]!!
                    )
                }
            }
        }
    }

    @Composable
    private fun CpuUsageChart() {
        val maxTimeMs = timeMsList[timeMsList.lastIndex]
        Column(
            modifier = Modifier
                .padding(top = 10.dp, start = 5.dp, end = 5.dp)
                .height(220.dp)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp)
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                drawLine(
                    color = Color.Black,
                    strokeWidth = 2f,
                    start = Offset(x = 50f, y = 50f),
                    end = Offset(x = 50f, y = size.height - 50f)
                )
                drawLine(
                    color = Color.Black,
                    strokeWidth = 2f,
                    start = Offset(x = size.width - 50f, y = 50f),
                    end = Offset(x = size.width - 50f, y = size.height - 50f)
                )
                drawLine(
                    color = Color.Black,
                    strokeWidth = 2f,
                    start = Offset(x = 50f, y = size.height - 50f),
                    end = Offset(x = size.width - 50f, y = size.height - 50f)
                )
                for (i in 0..4) {
                    val y = (size.height - 100f) * i / 5 + 50f
                    drawLine(
                        color = Color.Gray,
                        strokeWidth = 2f,
                        start = Offset(x = 50f, y = y),
                        end = Offset(x = size.width - 50f, y = y),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f), 2f)
                    )
                }
                for (i in 1..4) {
                    val x = (size.width - 100f) * i / 5 + 50f
                    drawLine(
                        color = Color.Gray,
                        strokeWidth = 2f,
                        start = Offset(x = x, y = 50f),
                        end = Offset(x = x, y = size.height - 50f),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f), 2f)
                    )
                }
                val titlePaint = Paint().let {
                    it.apply {
                        textSize = 20f
                        color = android.graphics.Color.GRAY
                    }
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "CpuUsage",
                    50f,
                    30f,
                    titlePaint
                )
                val tickPaint = Paint().let {
                    it.apply {
                        textSize = 16f
                        color = android.graphics.Color.GRAY
                    }
                }
                for (i in 0..5) {
                    val dataTick = (20 * i).toString()
                    val y = size.height - 50f - (size.height - 100f) * i / 5
                    drawContext.canvas.nativeCanvas.drawText(
                        dataTick,
                        20f,
                        y,
                        tickPaint
                    )
                }
                for (i in 1..5) {
                    val timeVal = (maxTimeMs / 1000) * i / 5
                    val timeMin = timeVal / 60
                    val timeSec = timeVal % 60
                    val timeTick = timeMin.toString() + "m" + timeSec.toString() + "s"
                    val x = 30f + (size.width - 100f) * i / 5
                    drawContext.canvas.nativeCanvas.drawText(
                        timeTick,
                        x,
                        size.height - 30f,
                        tickPaint
                    )
                }
                for (i in 1..cpuUsageList.lastIndex) {
                    for (cpu in 0..cpuUsageList[i].lastIndex) {
                        val startX = 50f + (size.width - 100f) * (i - 1) / cpuUsageList.lastIndex
                        val startY =
                            size.height - 50f - (size.height - 100f) * cpuUsageList[i - 1][cpu] / 100
                        val endX = 50f + (size.width - 100f) * i / cpuUsageList.lastIndex
                        val endY =
                            size.height - 50f - (size.height - 100f) * cpuUsageList[i][cpu] / 100
                        drawLine(
                            color = dataColors[cpuToCluster[cpu]]!!,
                            strokeWidth = 2f,
                            start = Offset(x = startX, y = startY),
                            end = Offset(x = endX, y = endY)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (cluster in 0 until clusterNum) {
                    Text(
                        modifier = Modifier
                            .padding(start = 5.dp, end = 5.dp),
                        text = "█ cluster${cluster}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = dataColors[cluster]!!
                    )
                }
            }
        }
    }

    @Composable
    private fun GpuDataChart() {
        var maxGpuFreq = 0
        for (gpuFreq in gpuCurFreqList) {
            if (gpuFreq > maxGpuFreq) {
                maxGpuFreq = gpuFreq
            }
        }
        maxGpuFreq = maxGpuFreq / 100 * 100 + 100
        val maxTimeMs = timeMsList[timeMsList.lastIndex]

        Column(
            modifier = Modifier
                .padding(top = 10.dp, start = 5.dp, end = 5.dp)
                .height(220.dp)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp)
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                drawLine(
                    color = Color.Black,
                    strokeWidth = 2f,
                    start = Offset(x = 50f, y = 50f),
                    end = Offset(x = 50f, y = size.height - 50f)
                )
                drawLine(
                    color = Color.Black,
                    strokeWidth = 2f,
                    start = Offset(x = size.width - 50f, y = 50f),
                    end = Offset(x = size.width - 50f, y = size.height - 50f)
                )
                drawLine(
                    color = Color.Black,
                    strokeWidth = 2f,
                    start = Offset(x = 50f, y = size.height - 50f),
                    end = Offset(x = size.width - 50f, y = size.height - 50f)
                )
                for (i in 0..4) {
                    val y = (size.height - 100f) * i / 5 + 50f
                    drawLine(
                        color = Color.Gray,
                        strokeWidth = 2f,
                        start = Offset(x = 50f, y = y),
                        end = Offset(x = size.width - 50f, y = y),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f), 2f)
                    )
                }
                for (i in 1..4) {
                    val x = (size.width - 100f) * i / 5 + 50f
                    drawLine(
                        color = Color.Gray,
                        strokeWidth = 2f,
                        start = Offset(x = x, y = 50f),
                        end = Offset(x = x, y = size.height - 50f),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f), 2f)
                    )
                }
                val titlePaint = Paint().let {
                    it.apply {
                        textSize = 20f
                        color = android.graphics.Color.GRAY
                    }
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "GpuCurFreq",
                    50f,
                    30f,
                    titlePaint
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "GpuUsage",
                    size.width - 145f,
                    30f,
                    titlePaint
                )
                val tickPaint = Paint().let {
                    it.apply {
                        textSize = 16f
                        color = android.graphics.Color.GRAY
                    }
                }
                for (i in 0..5) {
                    val dataTick = (maxGpuFreq * i / 5).toString()
                    val y = size.height - 50f - (size.height - 100f) * i / 5
                    drawContext.canvas.nativeCanvas.drawText(
                        dataTick,
                        10f,
                        y,
                        tickPaint
                    )
                }
                for (i in 1..5) {
                    val dataTick = (20 * i).toString()
                    val y = size.height - 50f - (size.height - 100f) * i / 5
                    drawContext.canvas.nativeCanvas.drawText(
                        dataTick,
                        size.width - 45f,
                        y,
                        tickPaint
                    )
                }
                for (i in 1..5) {
                    val timeVal = (maxTimeMs / 1000) * i / 5
                    val timeMin = timeVal / 60
                    val timeSec = timeVal % 60
                    val timeTick = timeMin.toString() + "m" + timeSec.toString() + "s"
                    val x = 30f + (size.width - 100f) * i / 5
                    drawContext.canvas.nativeCanvas.drawText(
                        timeTick,
                        x,
                        size.height - 30f,
                        tickPaint
                    )
                }
                for (i in 1..gpuCurFreqList.lastIndex) {
                    val startX = 50f + (size.width - 100f) * (i - 1) / gpuCurFreqList.lastIndex
                    val startY = size.height - 50f - (size.height - 100f) * gpuCurFreqList[i - 1] / maxGpuFreq
                    val endX = 50f + (size.width - 100f) * i / gpuCurFreqList.lastIndex
                    val endY = size.height - 50f - (size.height - 100f) * gpuCurFreqList[i] / maxGpuFreq
                    drawLine(
                        color = dataColors[0]!!,
                        strokeWidth = 2f,
                        start = Offset(x = startX, y = startY),
                        end = Offset(x = endX, y = endY)
                    )
                }
                for (i in 1..gpuUsageList.lastIndex) {
                    val startX = 50f + (size.width - 100f) * (i - 1) / gpuUsageList.lastIndex
                    val startY = size.height - 50f - (size.height - 100f) * gpuUsageList[i - 1] / 100
                    val endX = 50f + (size.width - 100f) * i / gpuUsageList.lastIndex
                    val endY = size.height - 50f - (size.height - 100f) * gpuUsageList[i] / 100
                    drawLine(
                        color = dataColors[1]!!,
                        strokeWidth = 2f,
                        start = Offset(x = startX, y = startY),
                        end = Offset(x = endX, y = endY)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .padding(start = 5.dp, end = 5.dp),
                    text = "█ GpuFreq",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = dataColors[0]!!
                )
                Text(
                    modifier = Modifier
                        .padding(start = 5.dp, end = 5.dp),
                    text = "█ GpuUsage",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = dataColors[1]!!
                )
            }
        }
    }

    @Composable
    private fun RamDataChart() {
        var maxDdrFreq = 0
        for (ddrFreq in ddrCurFreqList) {
            if (ddrFreq > maxDdrFreq) {
                maxDdrFreq = ddrFreq
            }
        }
        maxDdrFreq = maxDdrFreq / 500 * 500 + 500
        var maxRamFree = 0
        for (ramFree in ramFreeList) {
            if (ramFree > maxRamFree) {
                maxRamFree = ramFree
            }
        }
        maxRamFree = maxRamFree / 100 * 100 + 100
        val maxTimeMs = timeMsList[timeMsList.lastIndex]
        Column(
            modifier = Modifier
                .padding(top = 10.dp, start = 5.dp, end = 5.dp)
                .height(220.dp)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp)
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                drawLine(
                    color = Color.Black,
                    strokeWidth = 2f,
                    start = Offset(x = 50f, y = 50f),
                    end = Offset(x = 50f, y = size.height - 50f)
                )
                drawLine(
                    color = Color.Black,
                    strokeWidth = 2f,
                    start = Offset(x = size.width - 50f, y = 50f),
                    end = Offset(x = size.width - 50f, y = size.height - 50f)
                )
                drawLine(
                    color = Color.Black,
                    strokeWidth = 2f,
                    start = Offset(x = 50f, y = size.height - 50f),
                    end = Offset(x = size.width - 50f, y = size.height - 50f)
                )
                for (i in 0..4) {
                    val y = (size.height - 100f) * i / 5 + 50f
                    drawLine(
                        color = Color.Gray,
                        strokeWidth = 2f,
                        start = Offset(x = 50f, y = y),
                        end = Offset(x = size.width - 50f, y = y),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f), 2f)
                    )
                }
                for (i in 1..4) {
                    val x = (size.width - 100f) * i / 5 + 50f
                    drawLine(
                        color = Color.Gray,
                        strokeWidth = 2f,
                        start = Offset(x = x, y = 50f),
                        end = Offset(x = x, y = size.height - 50f),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f), 2f)
                    )
                }
                val titlePaint = Paint().let {
                    it.apply {
                        textSize = 20f
                        color = android.graphics.Color.GRAY
                    }
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "RamFree",
                    50f,
                    30f,
                    titlePaint
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "DdrCurFreq",
                    size.width - 160f,
                    30f,
                    titlePaint
                )
                val tickPaint = Paint().let {
                    it.apply {
                        textSize = 16f
                        color = android.graphics.Color.GRAY
                    }
                }
                for (i in 0..5) {
                    val dataTick = (maxRamFree * i / 5).toString()
                    val y = size.height - 50f - (size.height - 100f) * i / 5
                    drawContext.canvas.nativeCanvas.drawText(
                        dataTick,
                        10f,
                        y,
                        tickPaint
                    )
                }
                for (i in 1..5) {
                    val dataTick = (maxDdrFreq * i / 5).toString()
                    val y = size.height - 50f - (size.height - 100f) * i / 5
                    drawContext.canvas.nativeCanvas.drawText(
                        dataTick,
                        size.width - 45f,
                        y,
                        tickPaint
                    )
                }
                for (i in 1..5) {
                    val timeVal = (maxTimeMs / 1000) * i / 5
                    val timeMin = timeVal / 60
                    val timeSec = timeVal % 60
                    val timeTick = timeMin.toString() + "m" + timeSec.toString() + "s"
                    val x = 30f + (size.width - 100f) * i / 5
                    drawContext.canvas.nativeCanvas.drawText(
                        timeTick,
                        x,
                        size.height - 30f,
                        tickPaint
                    )
                }
                for (i in 1..ramFreeList.lastIndex) {
                    val startX = 50f + (size.width - 100f) * (i - 1) / ramFreeList.lastIndex
                    val startY = size.height - 50f - (size.height - 100f) * ramFreeList[i - 1] / maxRamFree
                    val endX = 50f + (size.width - 100f) * i / ramFreeList.lastIndex
                    val endY = size.height - 50f - (size.height - 100f) * ramFreeList[i] / maxRamFree
                    drawLine(
                        color = dataColors[0]!!,
                        strokeWidth = 2f,
                        start = Offset(x = startX, y = startY),
                        end = Offset(x = endX, y = endY)
                    )
                }
                for (i in 1..ddrCurFreqList.lastIndex) {
                    val startX = 50f + (size.width - 100f) * (i - 1) / ddrCurFreqList.lastIndex
                    val startY = size.height - 50f - (size.height - 100f) * ddrCurFreqList[i - 1] / maxDdrFreq
                    val endX = 50f + (size.width - 100f) * i / ddrCurFreqList.lastIndex
                    val endY = size.height - 50f - (size.height - 100f) * ddrCurFreqList[i] / maxDdrFreq
                    drawLine(
                        color = dataColors[1]!!,
                        strokeWidth = 2f,
                        start = Offset(x = startX, y = startY),
                        end = Offset(x = endX, y = endY)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .padding(start = 5.dp, end = 5.dp),
                    text = "█ RamFree",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = dataColors[0]!!
                )
                Text(
                    modifier = Modifier
                        .padding(start = 5.dp, end = 5.dp),
                    text = "█ DdrFreq",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = dataColors[1]!!
                )
            }
        }
    }

    @Composable
    private fun TempChart() {
        var maxTemp = 0
        for (batteryTemp in batteryTempList) {
            if (batteryTemp > maxTemp) {
                maxTemp = batteryTemp
            }
        }
        for (cpuTemp in cpuTempList) {
            if (cpuTemp > maxTemp) {
                maxTemp = cpuTemp
            }
        }
        maxTemp = maxTemp / 10 * 10 + 10
        val maxTimeMs = timeMsList[timeMsList.lastIndex]
        Column(
            modifier = Modifier
                .padding(top = 10.dp, start = 5.dp, end = 5.dp)
                .height(220.dp)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp)
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                drawLine(
                    color = Color.Black,
                    strokeWidth = 2f,
                    start = Offset(x = 50f, y = 50f),
                    end = Offset(x = 50f, y = size.height - 50f)
                )
                drawLine(
                    color = Color.Black,
                    strokeWidth = 2f,
                    start = Offset(x = size.width - 50f, y = 50f),
                    end = Offset(x = size.width - 50f, y = size.height - 50f)
                )
                drawLine(
                    color = Color.Black,
                    strokeWidth = 2f,
                    start = Offset(x = 50f, y = size.height - 50f),
                    end = Offset(x = size.width - 50f, y = size.height - 50f)
                )
                for (i in 0..4) {
                    val y = (size.height - 100f) * i / 5 + 50f
                    drawLine(
                        color = Color.Gray,
                        strokeWidth = 2f,
                        start = Offset(x = 50f, y = y),
                        end = Offset(x = size.width - 50f, y = y),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f), 2f)
                    )
                }
                for (i in 1..4) {
                    val x = (size.width - 100f) * i / 5 + 50f
                    drawLine(
                        color = Color.Gray,
                        strokeWidth = 2f,
                        start = Offset(x = x, y = 50f),
                        end = Offset(x = x, y = size.height - 50f),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f), 2f)
                    )
                }
                val titlePaint = Paint().let {
                    it.apply {
                        textSize = 20f
                        color = android.graphics.Color.GRAY
                    }
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "Temperature",
                    50f,
                    30f,
                    titlePaint
                )
                val tickPaint = Paint().let {
                    it.apply {
                        textSize = 16f
                        color = android.graphics.Color.GRAY
                    }
                }
                for (i in 0..5) {
                    val dataTick = (maxTemp * i / 5).toString()
                    val y = size.height - 50f - (size.height - 100f) * i / 5
                    drawContext.canvas.nativeCanvas.drawText(
                        dataTick,
                        20f,
                        y,
                        tickPaint
                    )
                }
                for (i in 1..5) {
                    val timeVal = (maxTimeMs / 1000) * i / 5
                    val timeMin = timeVal / 60
                    val timeSec = timeVal % 60
                    val timeTick = timeMin.toString() + "m" + timeSec.toString() + "s"
                    val x = 30f + (size.width - 100f) * i / 5
                    drawContext.canvas.nativeCanvas.drawText(
                        timeTick,
                        x,
                        size.height - 30f,
                        tickPaint
                    )
                }
                for (i in 1..batteryTempList.lastIndex) {
                    val startX = 50f + (size.width - 100f) * (i - 1) / batteryTempList.lastIndex
                    val startY = size.height - 50f - (size.height - 100f) * batteryTempList[i - 1] / maxTemp
                    val endX = 50f + (size.width - 100f) * i / batteryTempList.lastIndex
                    val endY = size.height - 50f - (size.height - 100f) * batteryTempList[i] / maxTemp
                    drawLine(
                        color = dataColors[0]!!,
                        strokeWidth = 2f,
                        start = Offset(x = startX, y = startY),
                        end = Offset(x = endX, y = endY)
                    )
                }
                for (i in 1..cpuTempList.lastIndex) {
                    val startX = 50f + (size.width - 100f) * (i - 1) / cpuTempList.lastIndex
                    val startY = size.height - 50f - (size.height - 100f) * cpuTempList[i - 1] / maxTemp
                    val endX = 50f + (size.width - 100f) * i / cpuTempList.lastIndex
                    val endY = size.height - 50f - (size.height - 100f) * cpuTempList[i] / maxTemp
                    drawLine(
                        color = dataColors[1]!!,
                        strokeWidth = 2f,
                        start = Offset(x = startX, y = startY),
                        end = Offset(x = endX, y = endY)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .padding(start = 5.dp, end = 5.dp),
                    text = "█ BatteryTemp",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = dataColors[0]!!
                )
                Text(
                    modifier = Modifier
                        .padding(start = 5.dp, end = 5.dp),
                    text = "█ CpuTemp",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = dataColors[1]!!
                )
            }
        }
    }

    @Composable
    private fun BatteryChart() {
        var maxPower = 0
        for (power in batteryPowerList) {
            if (power > maxPower) {
                maxPower = power
            }
        }
        maxPower = maxPower / 1000 * 1000 + 1000
        val maxTimeMs = timeMsList[timeMsList.lastIndex]

        Column(
            modifier = Modifier
                .padding(top = 10.dp, start = 5.dp, end = 5.dp)
                .height(220.dp)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp)
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                drawLine(
                    color = Color.Black,
                    strokeWidth = 2f,
                    start = Offset(x = 50f, y = 50f),
                    end = Offset(x = 50f, y = size.height - 50f)
                )
                drawLine(
                    color = Color.Black,
                    strokeWidth = 2f,
                    start = Offset(x = size.width - 50f, y = 50f),
                    end = Offset(x = size.width - 50f, y = size.height - 50f)
                )
                drawLine(
                    color = Color.Black,
                    strokeWidth = 2f,
                    start = Offset(x = 50f, y = size.height - 50f),
                    end = Offset(x = size.width - 50f, y = size.height - 50f)
                )
                for (i in 0..4) {
                    val y = (size.height - 100f) * i / 5 + 50f
                    drawLine(
                        color = Color.Gray,
                        strokeWidth = 2f,
                        start = Offset(x = 50f, y = y),
                        end = Offset(x = size.width - 50f, y = y),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f), 2f)
                    )
                }
                for (i in 1..4) {
                    val x = (size.width - 100f) * i / 5 + 50f
                    drawLine(
                        color = Color.Gray,
                        strokeWidth = 2f,
                        start = Offset(x = x, y = 50f),
                        end = Offset(x = x, y = size.height - 50f),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f), 2f)
                    )
                }
                val titlePaint = Paint().let {
                    it.apply {
                        textSize = 20f
                        color = android.graphics.Color.GRAY
                    }
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "BatteryPercentage",
                    50f,
                    30f,
                    titlePaint
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "BatteryPower",
                    size.width - 180f,
                    30f,
                    titlePaint
                )
                val tickPaint = Paint().let {
                    it.apply {
                        textSize = 16f
                        color = android.graphics.Color.GRAY
                    }
                }
                for (i in 0..5) {
                    val dataTick = (20 * i).toString()
                    val y = size.height - 50f - (size.height - 100f) * i / 5
                    drawContext.canvas.nativeCanvas.drawText(
                        dataTick,
                        10f,
                        y,
                        tickPaint
                    )
                }
                for (i in 1..5) {
                    val dataTick = (maxPower * i / 5).toString()
                    val y = size.height - 50f - (size.height - 100f) * i / 5
                    drawContext.canvas.nativeCanvas.drawText(
                        dataTick,
                        size.width - 45f,
                        y,
                        tickPaint
                    )
                }
                for (i in 1..5) {
                    val timeVal = (maxTimeMs / 1000) * i / 5
                    val timeMin = timeVal / 60
                    val timeSec = timeVal % 60
                    val timeTick = timeMin.toString() + "m" + timeSec.toString() + "s"
                    val x = 30f + (size.width - 100f) * i / 5
                    drawContext.canvas.nativeCanvas.drawText(
                        timeTick,
                        x,
                        size.height - 30f,
                        tickPaint
                    )
                }
                for (i in 1..batteryPercentList.lastIndex) {
                    val startX = 50f + (size.width - 100f) * (i - 1) / batteryPercentList.lastIndex
                    val startY = size.height - 50f - (size.height - 100f) * batteryPercentList[i - 1] / 100
                    val endX = 50f + (size.width - 100f) * i / batteryPercentList.lastIndex
                    val endY = size.height - 50f - (size.height - 100f) * batteryPercentList[i] / 100
                    drawLine(
                        color = dataColors[0]!!,
                        strokeWidth = 2f,
                        start = Offset(x = startX, y = startY),
                        end = Offset(x = endX, y = endY)
                    )
                }
                for (i in 1..batteryPowerList.lastIndex) {
                    val startX = 50f + (size.width - 100f) * (i - 1) / batteryPowerList.lastIndex
                    val startY = size.height - 50f - (size.height - 100f) * batteryPowerList[i - 1] / maxPower
                    val endX = 50f + (size.width - 100f) * i / batteryPowerList.lastIndex
                    val endY = size.height - 50f - (size.height - 100f) * batteryPowerList[i] / maxPower
                    drawLine(
                        color = dataColors[1]!!,
                        strokeWidth = 2f,
                        start = Offset(x = startX, y = startY),
                        end = Offset(x = endX, y = endY)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .padding(start = 5.dp, end = 5.dp),
                    text = "█ percent",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = dataColors[0]!!
                )
                Text(
                    modifier = Modifier
                        .padding(start = 5.dp, end = 5.dp),
                    text = "█ power",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = dataColors[1]!!
                )
            }
        }
    }

    private fun GetCpuInfo() {
        var cluster = 0
        for (cpu in 0..9) {
            if (utils.IsFileExist("/sys/devices/system/cpu/cpufreq/policy${cpu}")) {
                clusterToCpu[cluster] = cpu
                cluster++
            }
            cpuToCluster[cpu] = cluster - 1
        }
        clusterNum = cluster
    }
}
