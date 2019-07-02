package com.zebra.scannercontrol.app.helpers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.zebra.scannercontrol.RMDAttributes.*;

/**
 * Created by BPallewela on 11/30/2017.
 */

public class SSASymbologyType implements Serializable {

    // member variables
    private String symbologyName;
    private String attrIDDecodeCountHexValue;
    private Integer attrIDDecodeCount;
    private Integer attrIDMinDecodeTime;
    private Integer attrIDMaxDecodeTime;
    private Integer attrIDAvgDecodeTime;
    private Integer attrIDSlowestDecodeData;
    private Integer attrIDScanSpeedHistogram;


    public SSASymbologyType(String symbologyName) {
        this.symbologyName = symbologyName;
    }

    public void setSymbologyName(String symbologyName) { this.symbologyName = symbologyName; }
    public String getSymbologyName() { return symbologyName; }

    public void setAttrIDDecodeCountHexValue(String attrIDDecodeCountHexValue) { this.attrIDDecodeCountHexValue = attrIDDecodeCountHexValue; }
    public String getAttrIDDecodeCountHexValue() { return attrIDDecodeCountHexValue; }

    public void setAttrIDDecodeCount(Integer attrIDDecodeCount) { this.attrIDDecodeCount = attrIDDecodeCount; }
    public Integer getAttrIDDecodeCount() { return attrIDDecodeCount; }

    public void setAttrIDMinDecodeTime(Integer attrIDMinDecodeTime) { this.attrIDMinDecodeTime = attrIDMinDecodeTime; }
    public Integer getAttrIDMinDecodeTime() { return attrIDMinDecodeTime; }

    public void setAttrIDMaxDecodeTime(Integer attrIDMaxDecodeTime) { this.attrIDMaxDecodeTime = attrIDMaxDecodeTime; }
    public Integer getAttrIDMaxDecodeTime() { return attrIDMaxDecodeTime; }

    public void setAttrIDAvgDecodeTime(Integer attrIDAvgDecodeTime) { this.attrIDAvgDecodeTime = attrIDAvgDecodeTime; }
    public Integer getAttrIDAvgDecodeTime() { return attrIDAvgDecodeTime; }

    public void setAttrIDSlowestDecodeData(Integer attrIDSlowestDecodeData) { this.attrIDSlowestDecodeData = attrIDSlowestDecodeData; }
    public Integer getAttrIDSlowestDecodeData() { return attrIDSlowestDecodeData; }

    public void setAttrIDScanSpeedHistogram(Integer attrIDScanSpeedHistogram) { this.attrIDScanSpeedHistogram = attrIDScanSpeedHistogram; }
    public Integer getAttrIDScanSpeedHistogram() { return attrIDScanSpeedHistogram; }

//    public static SSASymbologyType getUPCSymbplogyObject(){
//        SSASymbologyType retSSASymboType = new SSASymbologyType("UPC");
//        retSSASymboType.attrIDAvgDecodeTime = 1;
//        retSSASymboType.attrIDDecodeCount = 2;
//        return retSSASymboType;
//    }

    public static List<SSASymbologyType> getSSASymbologyList(List<Integer> supportedIDList) {
        List<SSASymbologyType> resultSymList = new ArrayList<SSASymbologyType>();
        for (Integer supportId: supportedIDList) {
            SSASymbologyType tempSymType;
            switch (supportId) {
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_UPC: { // UPC
                    tempSymType = new SSASymbologyType("UPC");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_UPC; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_DECODE_COUNT_UPC; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_UPC; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_UPC; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_UPC; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_UPC; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_UPC; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_EAN_JAN: { // EAN/JAN
                    tempSymType = new SSASymbologyType("EAN/JAN");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_EAN_JAN; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_DECODE_COUNT_EAN_JAN; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_EAN_JAN; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_EAN_JAN ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_EAN_JAN ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_EAN_JAN; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_EAN_JAN; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_2_OF_5: { // 2 of 5
                    tempSymType = new SSASymbologyType("2 of 5");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_2_OF_5; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_DECODE_COUNT_2_OF_5; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_2_OF_5; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_2_OF_5; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_2_OF_5; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_2_OF_5; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_2_OF_5; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_CODEBAR: { // Codebar
                    tempSymType = new SSASymbologyType("Codebar");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_CODEBAR; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_DECODE_COUNT_CODEBAR; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_CODEBAR; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_CODEBAR ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_CODEBAR ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_CODEBAR; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_CODEBAR; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_CODE_11: { // Code 11
                    tempSymType = new SSASymbologyType("Code 11");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_CODE_11; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_DECODE_COUNT_CODE_11; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_CODE_11; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_CODE_11 ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_CODE_11 ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_CODE_11; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_CODE_11; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_CODE_128: { // Code 128
                    tempSymType = new SSASymbologyType("Code 128");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_CODE_128; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_DECODE_COUNT_CODE_128; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_CODE_128; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_CODE_128 ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_CODE_128 ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_CODE_128; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_CODE_128; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_CODE_39: { // Code 39
                    tempSymType = new SSASymbologyType("Code 39");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_CODE_39; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_DECODE_COUNT_CODE_39; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_CODE_39; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_CODE_39 ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_CODE_39 ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_CODE_39; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_CODE_39; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_CODE_93: { // Code 93
                    tempSymType = new SSASymbologyType("Code 93");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_CODE_93; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_DECODE_COUNT_CODE_93; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_CODE_93; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_CODE_93 ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_CODE_93 ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_CODE_93; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_CODE_93; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_COMPOSITE: { // Composite
                    tempSymType = new SSASymbologyType("Composite");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_COMPOSITE; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_DECODE_COUNT_COMPOSITE; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_COMPOSITE; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_COMPOSITE ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_COMPOSITE ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_COMPOSITE; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_COMPOSITE; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_GS1_DATABAR: { // GS1 Databar
                    tempSymType = new SSASymbologyType("GS1 Databar");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_GS1_DATABAR; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_DECODE_COUNT_GS1_DATABAR; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_GS1_DATABAR; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_GS1_DATABAR ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_GS1_DATABAR ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_GS1_DATABAR; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_GS1_DATABAR; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_MSI: { // MSI
                    tempSymType = new SSASymbologyType("MSI");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_MSI; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_DECODE_COUNT_MSI; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_MSI; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_MSI ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_MSI ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_MSI; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_MSI; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_DATAMARIX: { // Datamatrix
                    tempSymType = new SSASymbologyType("Datamatrix");
                    tempSymType.attrIDDecodeCountHexValue= RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_DATAMARIX; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount        = RMD_ATTR_VALUE_SSA_DECODE_COUNT_DATAMARIX; // Decode count
                    tempSymType.attrIDMinDecodeTime      = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_DATAMARIX; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime      = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_DATAMARIX ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime      = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_DATAMARIX ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData  = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_DATAMARIX; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram = RMD_ATTR_VALUE_SSA_HISTOGRAM_DATAMARIX; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_PDF: { // PDF
                    tempSymType = new SSASymbologyType("PDF");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_PDF; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_DECODE_COUNT_PDF; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_PDF; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_PDF ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_PDF ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_PDF; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_PDF; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_POSTAL_CODES: { // Postal Codes
                    tempSymType = new SSASymbologyType("Postal Codes");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_POSTAL_CODES; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_DECODE_COUNT_POSTAL_CODES; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_POSTAL_CODES; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_POSTAL_CODES ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_POSTAL_CODES ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_POSTAL_CODES; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_POSTAL_CODES; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_QR: { // QR
                    tempSymType = new SSASymbologyType("QR");
                    tempSymType.attrIDDecodeCountHexValue= RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_QR; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount        = RMD_ATTR_VALUE_SSA_DECODE_COUNT_QR; // Decode count
                    tempSymType.attrIDMinDecodeTime      = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_QR; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime      = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_QR ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime      = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_QR ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData  = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_QR; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram = RMD_ATTR_VALUE_SSA_HISTOGRAM_QR; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_AZTEC: { // Aztec
                    tempSymType = new SSASymbologyType("Aztec");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_AZTEC; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_AZTEC; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_AZTEC; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_AZTEC ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_AZTEC ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_AZTEC; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_AZTEC; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_OCR: { // OCR
                    tempSymType = new SSASymbologyType("OCR");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_OCR; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_DECODE_COUNT_OCR; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_OCR; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_OCR ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_OCR ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_OCR; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_OCR; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_MAXICODE: { // Maxicode
                    tempSymType = new SSASymbologyType("Maxicode");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_MAXICODE; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_DECODE_COUNT_MAXICODE; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_MAXICODE; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_MAXICODE ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_MAXICODE ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_MAXICODE; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_MAXICODE; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_GS1_DATAMATRIX: { // GS1-Datamatrix
                    tempSymType = new SSASymbologyType("GS1-Datamatrix");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_GS1_DATAMATRIX; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_DECODE_COUNT_GS1_DATAMATRIX; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_GS1_DATAMATRIX; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_GS1_DATAMATRIX ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_GS1_DATAMATRIX ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_GS1_DATAMATRIX; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_GS1_DATAMATRIX; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_GS1_QR_CODE: { // GS1-QR Code
                    tempSymType = new SSASymbologyType("GS1-QR Code");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_GS1_QR_CODE; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_DECODE_COUNT_GS1_QR_CODE; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_GS1_QR_CODE; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_GS1_QR_CODE ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_GS1_QR_CODE ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_GS1_QR_CODE; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_GS1_QR_CODE; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_COUPON: { // Coupon
                    tempSymType = new SSASymbologyType("Coupon");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_COUPON; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_DECODE_COUNT_COUPON; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_COUPON; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_COUPON ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_COUPON ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_COUPON; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_COUPON; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_DIGIMARC_UPC: { // Digimarc UPC
                    tempSymType = new SSASymbologyType("Digimarc UPC");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_DIGIMARC_UPC; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_DECODE_COUNT_DIGIMARC_UPC; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_DIGIMARC_UPC; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_DIGIMARC_UPC ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_DIGIMARC_UPC ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_DIGIMARC_UPC; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_DIGIMARC_UPC; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_DIGIMARC_EAN_JAN: { // Digimarc EAN/JAN
                    tempSymType = new SSASymbologyType("Digimarc EAN/JAN");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_DIGIMARC_EAN_JAN; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_DECODE_COUNT_DIGIMARC_EAN_JAN; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_DIGIMARC_EAN_JAN; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_DIGIMARC_EAN_JAN ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_DIGIMARC_EAN_JAN ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_DIGIMARC_EAN_JAN; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_DIGIMARC_EAN_JAN; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_DIGIMARC_OTHER: { // Digimarc Other
                    tempSymType = new SSASymbologyType("Digimarc Other");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_DIGIMARC_OTHER; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_DECODE_COUNT_DIGIMARC_OTHER; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_DIGIMARC_OTHER; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_DIGIMARC_OTHER ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_DIGIMARC_OTHER ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_DIGIMARC_OTHER; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_DIGIMARC_OTHER; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_OTHER_1D: { // Other 1D
                    tempSymType = new SSASymbologyType("Other 1D");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_OTHER_1D; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_DECODE_COUNT_OTHER_1D; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_OTHER_1D; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_OTHER_1D ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_OTHER_1D ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_OTHER_1D; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_OTHER_1D; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_OTHER_2D: { // Other 2D
                    tempSymType = new SSASymbologyType("Other 2D");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_OTHER_2D; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_DECODE_COUNT_OTHER_2D; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_OTHER_2D; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_OTHER_2D ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_OTHER_2D ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_OTHER_2D; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_OTHER_2D; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_OTHER: { // Other
                    tempSymType = new SSASymbologyType("Other");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_OTHER; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_DECODE_COUNT_OTHER; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_OTHER; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_OTHER ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_OTHER ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_OTHER; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_OTHER; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
                case RMD_ATTR_VALUE_SSA_DECODE_COUNT_UNUSED_ID: { // UNUSED STATISTIC ID
                    tempSymType = new SSASymbologyType("UNUSED STATISTIC ID");
                    tempSymType.attrIDDecodeCountHexValue = RMD_ATTR_VALUE_SSA_DECODE_COUNT_HEX_UNUSED_ID; // Decode count Hex value (Little Endian)
                    tempSymType.attrIDDecodeCount         = RMD_ATTR_VALUE_SSA_DECODE_COUNT_UNUSED_ID; // Decode count
                    tempSymType.attrIDMinDecodeTime       = RMD_ATTR_VALUE_SSA_MIN_DECODE_TIME_UNUSED_ID; // Minimum Decode time
                    tempSymType.attrIDMaxDecodeTime       = RMD_ATTR_VALUE_SSA_MAX_DECODE_TIME_UNUSED_ID ; // Maximum(Slowest) Decode Time
                    tempSymType.attrIDAvgDecodeTime       = RMD_ATTR_VALUE_SSA_AVG_DECODE_TIME_UNUSED_ID ; // Average Decode Time
                    tempSymType.attrIDSlowestDecodeData   = RMD_ATTR_VALUE_SSA_SLOW_DECODE_DATA_UNUSED_ID; // Slowest Decode Data
                    tempSymType.attrIDScanSpeedHistogram  = RMD_ATTR_VALUE_SSA_HISTOGRAM_UNUSED_ID; // Scan Speed Histogram
                    resultSymList.add(tempSymType);
                    break;
                }
            }
        }
        return resultSymList;
    }

    @Override
    public String toString() {
        return symbologyName;
    }

}
