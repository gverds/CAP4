package com.tcb.ecol.core.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iisigroup.cap.utils.CapBeanUtil;
import com.iisigroup.cap.utils.CapString;

/**
 * <pre>
 * TCB MFT 傳送 (因弱掃搬到jar)
 * </pre>
 * 
 * @since 2025年6月11日
 * @author Leon
 * @version
 *          <ul>
 *          <li>2025年6月11日,Leon,new
 *          </ul>
 */
public class MftUtils {
	
    private static final Logger logger = LoggerFactory.getLogger(CapBeanUtil.class);
    private static final int BUFFER_CAPACITY = 5200;// 初始StringBuffer大小

	/**
	 * MFT 檔案傳送 (by shell)，將 command 以空白切割成 List 傳入，如果指令為取的檔案名稱(DIR)則 dirShow 為 true
	 * 
	 * @param cmdList
	 * @return
	 *     map key：returnCode、fileNameList
	 */
	public static Map<String, Object> sendMft(List<String> cmdList, Boolean dirShow) {
		Map<String, Object> map = new HashMap<String, Object>();
        List<String> fileNameList = new ArrayList<String>();

        try {
			ProcessBuilder pb = new ProcessBuilder(cmdList.toArray(new String[cmdList.size()]));
	        pb.redirectErrorStream(true);
	
	        Process p = pb.start();
	        StringBuilder rtnOutput = new StringBuilder(BUFFER_CAPACITY);
	
	        // 修復 Denial of Service
	        ExecutorService executor = Executors.newSingleThreadExecutor();
	        AtomicBoolean flag = new AtomicBoolean(dirShow);
	        Future<?> readerTask = executor.submit(() -> {
	            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), "MS950"))) {
	                // shell 回傳編碼為 BIG5 (MS950)
	                boolean getFileName = false;
	                String line;
	                while ((line = reader.readLine()) != null) {
	                    logger.debug("[Shell Output] " + line);
	                    if (flag.get()) {
	                        if (!CapString.isEmpty(line)) {
	                            if (line.contains("個檔案")) {
	                                getFileName = false;
	                            } else if (getFileName && !line.contains("<DIR>")) {
	                                // 排除掉資料夾
	                                // 回傳格式" yyyy/MM/dd 上午 hh:mm bytes filename"，所以要先trim前面的空白
	                                String[] fileDataArr = line.trim().split(" ");
	                                if (fileDataArr.length == 5) {
	                                    fileNameList.add(fileDataArr[4]);
	                                }
	                            } else if (line.contains("目錄")) {
	                                getFileName = true;
	                            } else if (line.contains("rtncode")) {
	                                rtnOutput.append(line.split("=")[1]);
	                            }
	                        }
	                    }
	                }
	            } catch (IOException e) {
	                logger.debug("sendToMFT IOException");
	            } catch (Exception e) {
	                logger.debug("sendToMFT Exception");
	            }
	
	        });
	        boolean finished = p.waitFor(10, TimeUnit.SECONDS); // 加上 timeout 控制
	        if (!finished) {
	            p.destroyForcibly(); // 強制結束，避免卡住
	            logger.debug("Process 執行超時，已強制終止");
	        } else {
	            int exitCode = p.exitValue();
	            logger.debug("Exit code: " + exitCode);
	        }
	
	        readerTask.get(5, TimeUnit.SECONDS); // 確保 reader 結束
	
	        String rtnCode = rtnOutput.toString();
	
	        logger.debug("Return Code: " + rtnCode);
	        
	        map.put("fileNameList", fileNameList);
	        map.put("returnCode", rtnCode);
        } catch (IOException e) {
            logger.debug("receiveFromMFT IOException");
        } catch (InterruptedException e) {
            logger.debug("receiveFromMFT InterruptedException");
        } catch (Exception e) {
            logger.debug("other Exception");
        }
        
		return map;
	}
	
	/**
	 * MFT 檔案接收 (by shell)，將 command 以空白切割成 List 傳入
	 * 
	 * @param cmdList
	 * @return
	 *     returnCode
	 */
	public static String receiveFromMFT(List<String> cmdList) {
		try {
			ProcessBuilder pb = new ProcessBuilder(cmdList.toArray(new String[cmdList.size()]));
            pb.redirectErrorStream(true);

            Process p = pb.start();
            // 修復 Denial of Service
            ExecutorService executor = Executors.newSingleThreadExecutor();
            StringBuilder rtnOutput = new StringBuilder(BUFFER_CAPACITY);
            Future<?> readerTask = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), "MS950"))) {
                    // shell 回傳編碼為 BIG5 (MS950)
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.debug("[Shell Output] " + line);
                        if (!CapString.isEmpty(line)) {
                            if (line.contains("rtncode")) {
                                rtnOutput.append(line.split("=")[1]);
                            }
                        }
                    }
                } catch (IOException e) {
                    logger.debug("receiveFromMFT IOException");
                } catch (Exception e) {
                    logger.debug("receiveFromMFT Exception");
                }
            });
            boolean finished = p.waitFor(10, TimeUnit.SECONDS); // 加上 timeout 控制
            if (!finished) {
                p.destroyForcibly(); // 強制結束，避免卡住
                logger.debug("Process 執行超時，已強制終止");
            } else {
                int exitCode = p.exitValue();
                logger.debug("Exit code: " + exitCode);
            }

            readerTask.get(5, TimeUnit.SECONDS); // 確保 reader 結束

            String rtnCode = rtnOutput.toString();

            logger.debug("Return Code: " + rtnCode);
	        return rtnCode;
		}catch (IOException e) {
            logger.debug("receiveFromMFT IOException");
        } catch (InterruptedException e) {
            logger.debug("receiveFromMFT InterruptedException");
        } catch (Exception e) {
            logger.debug("other Exception");
        }
		return null;
	}
	
}
