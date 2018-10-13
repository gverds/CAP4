/* 
 * AbstractReportPdfService.java
 * 
 * Copyright (c) 2011 International Integrated System, Inc. 
 * All Rights Reserved.
 * 
 * Licensed Materials - Property of International Integrated System, Inc.
 * 
 * This software is confidential and proprietary information of 
 * International Integrated System, Inc. (&quot;Confidential Information&quot;).
 */
package com.iisigroup.cap.report;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.PDFEncryption;
import org.xhtmlrenderer.resource.XMLResource;

import com.iisigroup.cap.component.Request;
import com.iisigroup.cap.exception.CapException;
import com.iisigroup.cap.report.constants.ReportParamEnum;
import com.iisigroup.cap.report.factory.ItextFontFactory;
import com.iisigroup.cap.utils.CapString;
import com.iisigroup.cap.utils.CapSystemConfig;
import com.itextpdf.text.pdf.BaseFont;

import freemarker.template.Template;

/**
 * <pre>
 * Base Page of Report.
 * from freemark to pdf
 * </pre>
 * 
 * @since 2013/10/24
 * @author Sunkist Wang
 * @version
 *          <ul>
 *          <li>2013/10/24,Sunkist Wang,new
 *          </ul>
 */
public abstract class AbstractReportPdfService implements ReportService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final static String FIL_URL_PREFIX = "file:///";
    protected final static String REPORT_SUFFIX = ".ftl";
    protected final static String DEFAULT_ENCORDING = "utf-8";
    @Resource
    private FreeMarkerConfigurer fmConfg;
    @Resource
    private CapSystemConfig sysConfig;
    @Resource
    private ItextFontFactory fontFactory;

    @Resource
    private ServletContext servletContext;

    /*
     * (non-Javadoc)
     * 
     * @see com.iisigroup.cap.report.ReportService#generateReport(com.iisigroup.cap.component.Request)
     */
    @Override
    public ByteArrayOutputStream generateReport(Request request) throws CapException {
        ByteArrayOutputStream templateOut = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(templateOut, getSysConfig().getProperty(ReportParamEnum.DEFAULT_ENCODING.toString(), DEFAULT_ENCORDING)));) {
            Template t = getFmConfg().getConfiguration().getTemplate(getReportDefinition() + REPORT_SUFFIX);
            Map<String, Object> reportData = execute(request);
            t.process(reportData, writer);
            /**
             * 1.FOR 非使用 JDK 1.7 避免找不到TransformerFactoryImpl 所以指定org.apache.xalanz裡的實作 2.當使用 org.apache.xalan.processor.TransformerFactoryImpl 會發生org.w3c.dom.DOMException: NAMESPACE_ERR:
             */
            System.setProperty("javax.xml.transform.TransformerFactory", "org.apache.xalan.xsltc.trax.TransformerFactoryImpl");
            // process core-render
            Document document = XMLResource.load(new ByteArrayInputStream(templateOut.toByteArray())).getDocument();
            ITextRenderer iTextRenderer = new ITextRenderer();
            // 設定字型
            ITextFontResolver fontResolver = iTextRenderer.getFontResolver();
            fontResolver.addFont(getFontPath(), BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            PDFEncryption pdfEncryption = new PDFEncryption();
            // 設定加密
            if (reportData.containsKey(ReportParamEnum.ENCRYPT.toString())) {
                String password = (String) reportData.get(ReportParamEnum.ENCRYPT.toString());
                if (!CapString.isEmpty(password)) {
                    pdfEncryption.setUserPassword(password.getBytes());
                }
            }
            // 設定權限
            if (getAllowedPrivileges() != -1) {
                pdfEncryption.setAllowedPrivileges(getAllowedPrivileges());
            }
            iTextRenderer.setPDFEncryption(pdfEncryption);
            iTextRenderer.setDocument(document, FIL_URL_PREFIX + servletContext.getRealPath("").replace("\\", "/") + "/");
            iTextRenderer.layout();
            iTextRenderer.createPDF(out);
        } catch (Exception e) {
            if (e.getCause() != null) {
                throw new CapException(e.getCause(), e.getClass());
            } else {
                throw new CapException(e, e.getClass());
            }
        }
        return out;
    }

    public FreeMarkerConfigurer getFmConfg() {
        return fmConfg;
    }

    public CapSystemConfig getSysConfig() {
        return sysConfig;
    }

    public ItextFontFactory getFontFactory() {
        return fontFactory;
    }

    // 設定PDF權限
    protected int getAllowedPrivileges() {
        return -1;
        // return PdfWriter.ALLOW_ASSEMBLY; //全禁止
    }

    // 設定PDF權限
    protected String getFontPath() throws IOException {
        return getFontFactory().getFontPath(getSysConfig().getProperty(ReportParamEnum.DEFAULT_FONT.toString(), "MSJH.TTF"), "");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iisigroup.cap.report.ReportService#isWriteToFile()
     */
    @Override
    public boolean isWriteToFile() {
        return false; // PDF預設不寫檔
    }

}
