package cu.monitor

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.topjohnwu.superuser.Shell
import cu.monitor.ui.theme.CuPerfMonitorTheme
import java.io.File

class MainActivity : ComponentActivity() {
    private val utils = Utils()
    private var monitorServiceRunning: Boolean by mutableStateOf(false)
    private var recordsList = mutableStateListOf<String>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appDataPath = applicationContext.filesDir.absolutePath

        Shell.cmd("su -c true").exec()
        val isRoot: Boolean? = Shell.isAppGrantedRoot()
        if (isRoot == null) {
            Toast.makeText(applicationContext, "libsu无返回值", Toast.LENGTH_LONG).show()
            finish()
        } else if (isRoot == false) {
            Toast.makeText(applicationContext, "未能获取root权限", Toast.LENGTH_LONG).show()
            finish()
        }

        utils.MakeDir("${appDataPath}/monitor")
        CopyAssetsFileToData("monitor/perf_monitor", "/monitor/perf_monitor")
        Shell.cmd("chmod 0777 ${appDataPath}/monitor/perf_monitor").exec()
        CopyAssetsFileToData("monitor/config.json", "/monitor/config.json")
        Shell.cmd("chmod 0666 ${appDataPath}/monitor/config.json").exec()
        CopyAssetsFileToData("monitor/start_monitor.sh", "/monitor/start_monitor.sh")
        Shell.cmd("chmod 0777 ${appDataPath}/monitor/start_monitor.sh").exec()
        CopyAssetsFileToData("monitor/stop_monitor.sh", "/monitor/stop_monitor.sh")
        Shell.cmd("chmod 0777 ${appDataPath}/monitor/stop_monitor.sh").exec()

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
                                Text(
                                    modifier = Modifier
                                        .padding(start = 10.dp),
                                    text = "# CuPerfMonitor",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(top = it.calculateTopPadding())
                                .fillMaxSize()
                                .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            MonitorStateBar()
                            RecordsContainer()
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

    override fun onStart() {
        super.onStart()
        monitorServiceRunning = MonitorService.IsServiceRunning()
        ReflashPerfRecordsList()
    }

    @Composable
    private fun MonitorStateBar() {
        var stateBarColor = Color(0xFF888888)
        if (monitorServiceRunning) {
            stateBarColor = Color(0xFF5858A8)
        }
        var stateBarIcon = R.mipmap.monitor_closed
        if (monitorServiceRunning) {
            stateBarIcon = R.mipmap.monitor_running
        }
        var stateBarTitle = "未启用"
        if (monitorServiceRunning) {
            stateBarTitle = "已启用"
        }
        var stateBarInfo = "点击启用监视器服务"
        if (monitorServiceRunning) {
            stateBarInfo = "点击禁用监视器服务"
        }

        TextButton(
            onClick = { ControlMonitorService() },
            modifier = Modifier
                .padding(start = 5.dp, end = 5.dp)
                .height(80.dp)
                .fillMaxWidth()
                .background(
                    shape = RoundedCornerShape(10.dp),
                    color = stateBarColor
                ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = stateBarIcon),
                    contentDescription = null,
                    Modifier
                        .padding(start = 20.dp)
                        .height(40.dp)
                        .width(40.dp)
                )
                Column(
                    Modifier
                        .padding(start = 20.dp)
                        .height(60.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = stateBarTitle,
                        color = Color(0xFFFFFFFF),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stateBarInfo,
                        color = Color(0xFFF8F8F8),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    @Composable
    private fun RecordsContainer() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp, bottom = 10.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            if (recordsList.size > 0) {
                for (record in recordsList) {
                    PerfRecordItem(record)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "暂无性能记录",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    @Composable
    private fun PerfRecordItem(recordPath: String) {
        val showOptionsBar = remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .padding(top = 5.dp, start = 5.dp, end = 5.dp)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp)
                )
        ) {
            TextButton(
                onClick = { showOptionsBar.value = !showOptionsBar.value },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 10.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = utils.GetPrevString(utils.GetRePostString(recordPath, "/"), ".csv"),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            AnimatedVisibility(visible = showOptionsBar.value) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .padding(bottom = 5.dp, end = 5.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Bottom
                ) {
                    TextButton(
                        onClick = {
                            val intent = Intent(applicationContext, AnalysisActivity::class.java)
                            intent.putExtra("recordPath", recordPath)
                            startActivity(intent)
                        },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .padding(start = 5.dp, end = 5.dp)
                            .height(30.dp)
                            .width(60.dp),
                        shape = RoundedCornerShape(5.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.mipmap.charts),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 5.dp)
                                    .height(25.dp)
                                    .width(25.dp)
                            )
                            Text(
                                text = "分析",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    TextButton(
                        onClick = { SaveRecord(recordPath) },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .padding(start = 5.dp, end = 5.dp)
                            .height(30.dp)
                            .width(60.dp),
                        shape = RoundedCornerShape(5.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.mipmap.download),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 5.dp)
                                    .height(25.dp)
                                    .width(25.dp)
                            )
                            Text(
                                text = "保存",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    TextButton(
                        onClick = { DeleteRecord(recordPath) },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .padding(start = 5.dp, end = 5.dp)
                            .height(30.dp)
                            .width(60.dp),
                        shape = RoundedCornerShape(5.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.mipmap.delete),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 5.dp)
                                    .height(25.dp)
                                    .width(25.dp)
                            )
                            Text(
                                text = "删除",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    private fun ReflashPerfRecordsList() {
        recordsList.clear()
        val recordsDir = File(applicationContext.filesDir.absolutePath + "/monitor/records")
        val records = recordsDir.listFiles()
        if (records != null) {
            for (record in records) {
                if (record.isFile()) {
                    recordsList.add(record.absolutePath)
                }
            }
        }
    }

    private fun DeleteRecord(recordPath: String) {
        Shell.cmd("rm -f ${recordPath}").exec()
        ReflashPerfRecordsList()
    }

    private fun SaveRecord(recordPath: String) {
        val savePath =
            Environment.getExternalStorageDirectory().absolutePath + "/Documents/" + utils.GetRePostString(recordPath, "/")
        Shell.cmd("cp -f ${recordPath} ${savePath}").exec()
        Toast.makeText(applicationContext, "文件已保存到${savePath}", Toast.LENGTH_LONG).show()
    }

    private fun ControlMonitorService() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(applicationContext, "请授权悬浮窗权限", Toast.LENGTH_LONG).show()
            val mIntent = Intent()
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            mIntent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
            mIntent.data = Uri.fromParts("package", applicationContext.packageName, null)
            startActivity(mIntent)
        } else {
            val serviceIntent = Intent(applicationContext, MonitorService::class.java)
            if (!monitorServiceRunning) {
                startService(serviceIntent)
                monitorServiceRunning = true
            } else {
                stopService(serviceIntent)
                monitorServiceRunning = false
            }
        }
    }

    private fun CopyAssetsFileToData(assetsPath: String, dataPath: String) {
        val file = File(applicationContext.filesDir.absolutePath + dataPath)
        if (file.exists()) {
            file.delete()
        }
        assets.open(assetsPath).use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
}