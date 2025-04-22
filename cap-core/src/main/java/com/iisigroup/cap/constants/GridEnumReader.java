package com.iisigroup.cap.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GridEnumReader {

    @Value("${grid.type:jq}")
    private String gridType;

    public Enum<?> getPAGE() {
    	return "dt".equalsIgnoreCase(gridType)?GridEnum.PAGE:JQGridEnum.PAGE;
    }
    
    public Enum<?> getPAGEROWS() {
        return "dt".equalsIgnoreCase(gridType)?GridEnum.PAGEROWS:JQGridEnum.PAGEROWS;
    }
    
    public Enum<?> getTOTAL() {
        return "dt".equalsIgnoreCase(gridType)?GridEnum.TOTAL:JQGridEnum.TOTAL;
    }
    
    public Enum<?> getRECORDS() {
        return "dt".equalsIgnoreCase(gridType)?GridEnum.RECORDS:JQGridEnum.RECORDS;
    }
    
    public Enum<?> getSORTTYPE() {
        return "dt".equalsIgnoreCase(gridType)?GridEnum.SORTTYPE:JQGridEnum.SORTTYPE;
    }
    
    public Enum<?> getSORTASC() {
        return "dt".equalsIgnoreCase(gridType)?GridEnum.SORTASC:JQGridEnum.SORTASC;
    }
    
    public Enum<?> getSORTDESC() {
        return "dt".equalsIgnoreCase(gridType)?GridEnum.SORTDESC:JQGridEnum.SORTDESC;
    }
    
    public Enum<?> getSORTCOLUMN() {
        return "dt".equalsIgnoreCase(gridType)?GridEnum.SORTCOLUMN:JQGridEnum.SORTCOLUMN;
    }

    public Enum<?> getCELL() {
    	return "dt".equalsIgnoreCase(gridType)?GridEnum.CELL:JQGridEnum.CELL;
    }
    
    public Enum<?> getCOL_NAME() {
        return "dt".equalsIgnoreCase(gridType)?GridEnum.COL_NAME:JQGridEnum.COL_NAME;
    }
    
    public Enum<?> getCOL_INDEX() {
        return "dt".equalsIgnoreCase(gridType)?GridEnum.COL_INDEX:JQGridEnum.COL_INDEX;
    }
    
    public Enum<?> getCOL_PARAM() {
        return "dt".equalsIgnoreCase(gridType)?GridEnum.COL_PARAM:JQGridEnum.COL_PARAM;
    }
    
    public Enum<?> getSTART() {
        return "dt".equalsIgnoreCase(gridType)?GridEnum.START:JQGridEnum.START;
    }
    
    public String getGridType() {
    	return this.gridType;
    }
}
